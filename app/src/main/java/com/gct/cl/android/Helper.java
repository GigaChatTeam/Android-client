package com.gct.cl.android;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class Helper {
    static class LocalAccount {
        boolean main;
        String token;
        long id;
    }

    @NonNull
    static LocalAccount constructLocalAccount (String token, long id) {
        LocalAccount account = new LocalAccount();
        account.main = true;
        account.token = token;
        account.id = id;

        return account;
    }
}
