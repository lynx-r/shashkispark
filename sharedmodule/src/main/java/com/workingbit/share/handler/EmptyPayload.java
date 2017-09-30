package com.workingbit.share.handler;

public class EmptyPayload implements Validable {
    @Override
    public boolean isValid() {
        return true;
    }
}