package com.weikey.liuguangapicommon.service;


import com.weikey.liuguangapicommon.model.dto.cache.InterfaceCacheDto;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoGetRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoActivateRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeCountRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 接口服务
 */
@FeignClient(name = "liuguang-api-interface", path = "/api/interface/inner") // name:服务名
public interface InterfaceFeignClient {

    /**
     * 分配接口的调用次数
     *
     * @param userInterfaceInfoActivateRequest
     */
    @PostMapping("/add/count")
    void addCount(@RequestBody UserInterfaceInfoActivateRequest userInterfaceInfoActivateRequest);

    /**
     * 根据id查询
     *
     * @param interfaceId
     * @return
     */
    @GetMapping("/get/by/id")
    InterfaceInfo getById(@RequestParam("interfaceId") long interfaceId);

    /**
     * 根据请求路径和方式查询
     *
     * @param interfaceInfoGetRequest
     * @return
     */
    @PostMapping("/get/post")
    InterfaceCacheDto getInterface(@RequestBody InterfaceInfoGetRequest interfaceInfoGetRequest);

    /**
     * 调用接口次数统计
     *
     * @param userInterfaceInfoInvokeCountRequest
     * @return false表示接口剩余调用次数为0，接口调用失败
     */
    @PostMapping("/invoke/count")
    boolean invokeCount(@RequestBody UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest);

}
