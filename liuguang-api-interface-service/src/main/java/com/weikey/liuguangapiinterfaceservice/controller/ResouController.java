package com.weikey.liuguangapiinterfaceservice.controller;

import cn.hutool.http.HttpRequest;
import com.weikey.liuguangapiinterfaceservice.config.ApiKeyConfig;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 热搜接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/resou")
public class ResouController {

    @Resource
    private ApiKeyConfig apiKeyConfig;

    /**
     * 百度热搜接口
     * @param size 热搜条数
     * @return
     */
    @GetMapping("/baidu")
    public String getBaiduResou(@RequestParam int size) {
        return HttpRequest.get("https://www.coderutil.com/api/resou/v1/baidu")
                .addHeaders(getHeaders())
                .form("size", size).execute().body();
    }

    /**
     * 知乎热搜接口
     * @return
     */
    @GetMapping("/zhihu")
    public String getZhihuResou() {
        return HttpRequest.get("https://www.coderutil.com/api/resou/v1/zhihu")
                .addHeaders(getHeaders()).execute().body();
    }

    /**
     * 微博热搜接口
     * @param size 热搜条数
     * @return
     */
    @GetMapping("/weibo")
    public String getWeiboResou(@RequestParam int size) {
        return HttpRequest.get("https://www.coderutil.com/api/resou/v1/weibo")
                .addHeaders(getHeaders())
                .form("size", size).execute().body();
    }

    private Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("access-key", apiKeyConfig.getAccessKey());
        headers.put("secret-key", apiKeyConfig.getSecretKey());
        return headers;
    }
}
