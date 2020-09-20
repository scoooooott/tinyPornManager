package com.scott.pornhub.exceptions;

public class PornhubNotFoundException extends PornhubException {
    public PornhubNotFoundException(int code, String message) {
        super(code, message);
    }
}
