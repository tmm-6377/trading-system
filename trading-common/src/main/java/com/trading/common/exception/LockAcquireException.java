package com.trading.common.exception;

public class LockAcquireException extends BusinessException {
    public LockAcquireException(String message) {
        super("LOCK_ACQUIRE_FAILED", message);
    }
}
