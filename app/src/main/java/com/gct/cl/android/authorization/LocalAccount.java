package com.gct.cl.android.authorization;

import androidx.annotation.NonNull;

class LocalAccount {
    boolean main;
    String token;
    long id;

    @NonNull
    static LocalAccount constructLocalAccount (String token, long id) {
        LocalAccount account = new LocalAccount();
        account.main = true;
        account.token = token;
        account.id = id;

        return account;
    }
}