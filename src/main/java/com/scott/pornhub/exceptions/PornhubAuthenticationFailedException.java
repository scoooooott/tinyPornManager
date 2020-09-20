package com.scott.pornhub.exceptions;

public class PornhubAuthenticationFailedException extends PornhubException {
    public PornhubAuthenticationFailedException(int code, String message) {
        super(code, message);
    }
}
