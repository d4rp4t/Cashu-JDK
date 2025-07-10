package com.cashujdk.errors;

public class CashuExceptions extends RuntimeException {
    public CashuExceptions(String message) {
        super(message);
    }

    public static class ProofSelectionException extends CashuExceptions {
        public ProofSelectionException(String message) {
            super(message);
        }
    }
}
