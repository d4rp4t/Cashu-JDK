package com.cashujdk.api;

import com.cashujdk.nut00.CashuProtocolError;
import com.cashujdk.nut00.CashuProtocolException;
import com.cashujdk.nut01.GetKeysResponse;
import com.cashujdk.nut02.GetKeysetsResponse;
import com.cashujdk.nut03.PostSwapRequest;
import com.cashujdk.nut03.PostSwapResponse;
import com.cashujdk.nut06.GetInfoResponse;
import com.cashujdk.nut07.PostCheckStateRequest;
import com.cashujdk.nut07.PostCheckStateResponse;
import com.cashujdk.nut09.PostRestoreRequest;
import com.cashujdk.nut09.PostRestoreResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class CashuHttpClient  {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public CashuHttpClient(OkHttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = createObjectMapper();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure mapper for better error messages
        mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        return mapper;
    }

    // --- GET Requests ---

    public CompletableFuture<GetKeysResponse> getKeys() {
        return sendGetRequest("v1/keys", new TypeReference<GetKeysResponse>() {});
    }

    public CompletableFuture<GetKeysetsResponse> getKeysets() {
        return sendGetRequest("v1/keysets", new TypeReference<GetKeysetsResponse>() {});
    }

    public CompletableFuture<GetKeysResponse> getKeys(String keysetId) {
        return sendGetRequest("v1/keys/" + keysetId, new TypeReference<GetKeysResponse>() {});
    }

    public CompletableFuture<GetInfoResponse> getInfo() {
        return sendGetRequest("v1/info", new TypeReference<GetInfoResponse>() {});
    }

    public <TResponse> CompletableFuture<TResponse> checkMeltQuote(String method, String quoteId, Class<TResponse> clazz) {
        return sendGetRequest("v1/melt/quote/" + method + "/" + quoteId, clazz);
    }

    public <TResponse> CompletableFuture<TResponse> checkMintQuote(String method, String quoteId, Class<TResponse> clazz) {
        return sendGetRequest("v1/mint/quote/" + method + "/" + quoteId, clazz);
    }

    // --- POST Requests ---

    public CompletableFuture<PostSwapResponse> swap(PostSwapRequest request) {
        return sendPostRequest("v1/swap", request, PostSwapResponse.class);
    }

    public <TResponse, TRequest> CompletableFuture<TResponse> createMintQuote(String method, TRequest request, Class<TResponse> clazz) {
        return sendPostRequest("v1/mint/quote/" + method, request, clazz);
    }

    public <TResponse, TRequest> CompletableFuture<TResponse> createMeltQuote(String method, TRequest request, Class<TResponse> clazz) {
        return sendPostRequest("v1/melt/quote/" + method, request, clazz);
    }

    public <TResponse, TRequest> CompletableFuture<TResponse> melt(String method, TRequest request, Class<TResponse> clazz) {
        return sendPostRequest("v1/melt/" + method, request, clazz);
    }

    public <TRequest, TResponse> CompletableFuture<TResponse> mint(String method, TRequest request, Class<TResponse> clazz) {
        return sendPostRequest("v1/mint/" + method, request, clazz);
    }

    public CompletableFuture<PostCheckStateResponse> checkState(PostCheckStateRequest request) {
        return sendPostRequest("v1/checkstate", request, PostCheckStateResponse.class);
    }

    public CompletableFuture<PostRestoreResponse> restore(PostRestoreRequest request) {
        return sendPostRequest("v1/restore", request, PostRestoreResponse.class);
    }


    private <T> CompletableFuture<T> sendGetRequest(String path, Class<T> clazz) {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                return handleResponse(response, clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <T> CompletableFuture<T> sendGetRequest(String path, TypeReference<T> typeReference) {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();

        return CompletableFuture.supplyAsync(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                return handleResponse(response, typeReference);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <TRequest, TResponse> CompletableFuture<TResponse> sendPostRequest(String path, TRequest requestObj, Class<TResponse> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = objectMapper.writeValueAsString(requestObj);
                RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(baseUrl + path)
                        .post(body)
                        .build();

                IOException lastException = null;
                for (int attempt = 0; attempt < 3; attempt++) {
                    try {
                        try (Response response = httpClient.newCall(request).execute()) {
                            return handleResponse(response, clazz);
                        }
                    } catch (IOException e) {
                        lastException = e;
                        if (attempt < 2) {  // Don't sleep on the last attempt
                            try {
                                Thread.sleep(1000 * (attempt + 1));  // Exponential backoff: 1s, 2s
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Interrupted during retry delay", ie);
                            }
                        }
                    }
                }
                throw new RuntimeException("Failed after 3 attempts", lastException);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // --- Response handling ---

    private <T> T handleResponse(Response response, Class<T> clazz) throws IOException {
        int statusCode = response.code();
        String body = response.body() != null ? response.body().string() : "";

        if (statusCode == 400) {
            try {
                CashuProtocolError error = objectMapper.readValue(body, CashuProtocolError.class);
                throw new CashuProtocolException(error);
            } catch (IOException e) {
                throw new RuntimeException(String.format(
                    "Error deserializing error response. Response body: '%s', Error: %s",
                    body, e.getMessage()
                ), e);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("HTTP error: " + statusCode + " - " + body);
        }

        try {
            return objectMapper.readValue(body, clazz);
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "Error deserializing response to type '%s'. Response body: '%s', Error: %s",
                clazz.getSimpleName(), body, e.getMessage()
            ), e);
        }
    }

    private <T> T handleResponse(Response response, TypeReference<T> typeReference) throws IOException {
        int statusCode = response.code();
        String body = response.body() != null ? response.body().string() : "";

        if (statusCode == 400) {
            try {
                CashuProtocolError error = objectMapper.readValue(body, CashuProtocolError.class);
                throw new CashuProtocolException(error);
            } catch (IOException e) {
                throw new RuntimeException(String.format(
                    "Error deserializing error response. Response body: '%s', Error: %s",
                    body, e.getMessage()
                ), e);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("HTTP error: " + statusCode + " - " + body);
        }

        try {
            return objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                "Error deserializing response to type '%s'. Response body: '%s', Error: %s",
                typeReference.getType().getTypeName(), body, e.getMessage()
            ), e);
        }
    }
}
