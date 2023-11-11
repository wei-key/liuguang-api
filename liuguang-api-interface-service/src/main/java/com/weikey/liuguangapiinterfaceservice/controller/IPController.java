package com.weikey.liuguangapiinterfaceservice.controller;

import cn.hutool.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ip接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/ip")
public class IPController {

    /**
     * 查询IP或者域名的归属地
     * @param ip IP或者域名
     * @return
     */
    @GetMapping("/address")
    public String getAddressByIP(@RequestParam String ip) {
        return HttpRequest.get("https://api.asilu.com/ip/")
                .form("ip", ip)
                .execute().body();
    }
}
