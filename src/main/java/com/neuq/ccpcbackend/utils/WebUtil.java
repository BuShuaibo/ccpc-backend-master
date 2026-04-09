package com.neuq.ccpcbackend.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 即刻返回本次请求
 */
public class WebUtil {
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
