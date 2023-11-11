package com.weikey.liuguangapiinterfaceservice.controller;

import cn.hutool.http.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气接口
 *
 * @author wei-key
 */
@RestController
public class WeatherController {
    @Value("${api.weather.appid}")
    private String appid;

    @Value("${api.weather.appsecret}")
    private String appsecret;

    /**
     * 今日实况天气接口
     * @param city 城市名
     * @return
     */
    @GetMapping("/weather")
    public String getWeather(@RequestParam String city) {
        return HttpRequest.get("https://www.tianqiapi.com/free/day")
                .form("city", city).form("appid", appid).form("appsecret", appsecret)
                .execute().body();
    }
}
