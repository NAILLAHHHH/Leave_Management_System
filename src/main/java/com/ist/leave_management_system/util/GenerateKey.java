package com.ist.leave_management_system.util;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class GenerateKey {
    public static void main(String[] args) {
        // Generate a key that's secure enough for HS512
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        String secureKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated secure key: " + secureKey);
        System.out.println("Copy this key to your application.properties file as app.jwtSecret");
    }
} 