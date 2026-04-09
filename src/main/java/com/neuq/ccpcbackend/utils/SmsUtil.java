package com.neuq.ccpcbackend.utils;

import com.alibaba.fastjson.JSONObject;
import com.neuq.ccpcbackend.utils.exception.BizException;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component  // 添加@Component以支持依赖注入
public class SmsUtil {

    private static String sendUrl;
    private static String account;
    private static String password;

    // Setter方法用于注入配置
    @Value("${sms.send-url}")
    public void setSendUrl(String sendUrl) {
        SmsUtil.sendUrl = sendUrl;
    }

    @Value("${sms.account}")
    public void setAccount(String account) {
        SmsUtil.account = account;
    }

    @Value("${sms.password}")
    public void setPassword(String password) {
        SmsUtil.password = password;
    }

    public static void sendMessageToPhone(String phone, String message) {
        Map<String, String> map = new HashMap<>();
        map.put("account", account);
        map.put("password", password);
        map.put("msg", message);
        map.put("phone", phone);
        map.put("report", "true");
        map.put("extend", "123");
        JSONObject js = (JSONObject) JSONObject.toJSON(map);
        String result = sendSmsByPost(sendUrl, js.toString());
        JSONObject resultJson = JSONObject.parseObject(result);
        String code = resultJson.getString("code");
        if (!"0".equals(code)) {
            throw new BizException(ErrorCode.SEND_SMS_FAILED.getErrCode(), "请求发送短信失败");
        }
    }

    private static String sendSmsByPost(String path, String postContent) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(postContent))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new BizException(
                        ErrorCode.SEND_SMS_FAILED.getErrCode(),
                        "请求发送短信失败");
            }
        } catch (Exception e) {
            throw new BizException(
                    ErrorCode.SEND_SMS_FAILED.getErrCode(),
                    e.getMessage());
        }
    }
}