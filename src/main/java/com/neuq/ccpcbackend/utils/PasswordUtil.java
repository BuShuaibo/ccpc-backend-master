package com.neuq.ccpcbackend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    private static final String SECRET_KEY = "ccpc";
    private static final String ALLOWED_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // 根据key生成包含数字+小写字母+大写字母的唯一长度为passwordLength的密码
    public static String generatePassword(int passwordLength, String key) {
        try {
            key = key + "|" + SECRET_KEY;

            // 1. 使用SHA-256哈希生成固定长度的摘要
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));

            // 2. 将哈希值转换为大整数
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            java.math.BigInteger bigInt = new java.math.BigInteger(hexString.toString(), 16);

            // 3. 将大整数映射到62进制(数字+大小写字母)
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < passwordLength; i++) {
                int index = bigInt.mod(new java.math.BigInteger(String.valueOf(ALLOWED_CHARS.length())))
                        .intValue();
                password.append(ALLOWED_CHARS.charAt(index));
                bigInt = bigInt.divide(new java.math.BigInteger(String.valueOf(ALLOWED_CHARS.length())));
            }

            return password.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate password", e);
        }
    }
}