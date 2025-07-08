package com.project.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.nut00.CashuProtocolError;
import com.project.nut00.CashuProtocolException;
import com.project.nut01.GetKeysResponse;
import com.project.nut02.GetKeysetsResponse;
import com.project.nut03.PostSwapRequest;
import com.project.nut03.PostSwapResponse;
import com.project.nut06.GetInfoResponse;
import com.project.nut07.PostCheckStateRequest;
import com.project.nut07.PostCheckStateResponse;
import com.project.nut09.PostRestoreRequest;
import com.project.nut09.PostRestoreResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class CashuHttpClient  {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public CashuHttpClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(response, clazz));
    }

    private <T> CompletableFuture<T> sendGetRequest(String path, TypeReference<T> typeReference) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> handleResponse(response, typeReference));
    }

    private <TRequest, TResponse> CompletableFuture<TResponse> sendPostRequest(String path, TRequest requestObj, Class<TResponse> clazz) {
        try {
            String json = objectMapper.writeValueAsString(requestObj);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> handleResponse(response, clazz));

        } catch (IOException e) {
            CompletableFuture<TResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    // --- Response handling ---

    private <T> T handleResponse(HttpResponse<String> response, Class<T> clazz) {
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode == 400) {
            try {
                CashuProtocolError error = objectMapper.readValue(body, CashuProtocolError.class);
                throw new CashuProtocolException(error);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing error response", e);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("HTTP error: " + statusCode + " - " + body);
        }

        try {
            return objectMapper.readValue(body, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing response", e);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> typeReference) {
        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode == 400) {
            try {
                CashuProtocolError error = objectMapper.readValue(body, CashuProtocolError.class);
                throw new CashuProtocolException(error);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing error response", e);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("HTTP error: " + statusCode + " - " + body);
        }

        try {
            return objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing response", e);
        }
    }
}
