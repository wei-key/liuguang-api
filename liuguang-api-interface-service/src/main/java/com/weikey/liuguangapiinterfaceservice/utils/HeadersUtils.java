package com.weikey.liuguangapiinterfaceservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HeadersUtils {

    private static String accessKey;

    private static String secretKey;

    @Value("${api.resou.access-key}")
    public void setAccessKey(String accessKey) {
        HeadersUtils.accessKey = accessKey;
    }

    @Value("${api.resou.secret-key}")
    public void setSecretKey(String secretKey) {
        HeadersUtils.secretKey = secretKey;
    }

    public static Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("access-key", accessKey);
        headers.put("secret-key", secretKey);
        return headers;
    }

}
