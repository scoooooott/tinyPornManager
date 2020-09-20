package com.scott.pornhub.exceptions;

public class PornhubDuplicateEntryException extends PornhubException {
    public PornhubDuplicateEntryException(int code, String message) {
        super(code, message);
    }
}
