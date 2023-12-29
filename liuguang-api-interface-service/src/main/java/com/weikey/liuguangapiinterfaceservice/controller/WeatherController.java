package com.weikey.liuguangapiinterfaceservice.controller;

import cn.hutool.http.HttpRequest;
import com.weikey.liuguangapiinterfaceservice.config.ApiKeyConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 天气接口
 *
 * @author wei-key
 */
@RestController
public class WeatherController {
    @Resource
    private ApiKeyConfig apiKeyConfig;

    /**
     * 今日实况天气接口
     * @param city 城市名
     * @return
     */
    @GetMapping("/weather")
    public String getWeather(@RequestParam String city) {
        return HttpRequest.get("https://www.tianqiapi.com/free/day")
                .form("city", city).form("appid", apiKeyConfig.getAppid()).form("appsecret", apiKeyConfig.getAppsecret())
                .execute().body();
    }
}
