package com.gct.cl.android;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String SHA512 (String string) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return string;
        }

        byte[] bytes = md.digest(string.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
