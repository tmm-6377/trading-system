package com.trading.common.exception;

public class RemoteServiceException extends BusinessException {
    public RemoteServiceException(String message) {
        super("REMOTE_SERVICE_ERROR", message);
    }
}
