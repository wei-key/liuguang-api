package com.weikey.liuguangapiinterface.job;

import com.google.gson.Gson;
import com.weikey.liuguangapicommon.model.dto.cache.InterfaceCacheDto;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.weikey.liuguangapicommon.constant.RedisKeyConstant.INTERFACE_KEY_PREFIX;

/**
 *  项目启动预加载接口缓存
 */
//@Component
public class RedisCacheLoadJob implements CommandLineRunner {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private final Gson gson = new Gson();

    @Override
    public void run(String... args) {
        List<InterfaceInfo> list = interfaceInfoService.list();
        list.stream().forEach(interfaceInfo -> {
            InterfaceCacheDto interfaceCacheDto = new InterfaceCacheDto();
            BeanUtils.copyProperties(interfaceInfo, interfaceCacheDto);
            stringRedisTemplate.opsForValue().set(INTERFACE_KEY_PREFIX + interfaceInfo.getUrl(), gson.toJson(interfaceCacheDto));
        });
    }
}