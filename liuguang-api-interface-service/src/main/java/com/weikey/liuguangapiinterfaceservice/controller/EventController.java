package com.weikey.liuguangapiinterfaceservice.controller;

import cn.hutool.http.HttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事件接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/event")
public class EventController {

    /**
     * 历史上的今天发生的事件
     * @return
     */
    @GetMapping("/today/in/history")
    public String todayInHistory() {
        return HttpRequest.get("https://query.asilu.com/today/list/")
                .execute().body();
    }
}
