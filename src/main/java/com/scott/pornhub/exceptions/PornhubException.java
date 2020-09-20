package com.scott.pornhub.exceptions;

import java.io.IOException;

public class PornhubException extends IOException {
    private int code;

    public int getCode() {
        return code;
    }

    public PornhubException(String message) {
        super(message);
    }

    public PornhubException(int code, String message) {
        super(message);
        this.code = code;
    }
}
