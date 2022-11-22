package com.nabrothers.psl.core.exception;

public class TransactionException extends RuntimeException {
    public TransactionException() {
        super("交易失败");
    }

    public TransactionException(String message, Throwable cause) {
        super("交易失败：" + message, cause);
    }

    public TransactionException(String message) {
        super("交易失败：" + message);
    }
}
