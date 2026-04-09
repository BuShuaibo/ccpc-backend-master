package com.neuq.ccpcbackend.utils;

import java.time.Instant;

public class TimeUtil {
    public static long getCurrentTimestamp() {
        return Instant.now().getEpochSecond();
    }
}
