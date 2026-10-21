package com.ifam.amigopetfixo.repository;

public interface UsernameCheckCallback {
    void onResult(boolean isAvailable);
    void onError(String errorMessage);
}