package com.scott.pornhub.exceptions;

public class PornhubServiceErrorException extends PornhubException {
    public PornhubServiceErrorException(int code, String message) {
        super(code, message);
    }
}
