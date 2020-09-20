package com.scott.pornhub.exceptions;

public class PornhubInvalidParametersException extends PornhubException {
    public PornhubInvalidParametersException(int code, String message) {
        super(code, message);
    }
}
