package com.neuq.ccpcbackend.utils;

import java.util.Random;

public class CodeUtil {
    public static String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 生成 0 到 9 之间的随机数字
        }
        return sb.toString();
    }
}
