package com.weikey.liuguangapiinterface;

import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class UserInterfaceInfoServiceTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Test
    void testRollbackCount() {
        long userId = 1L;
        long interfaceInfoId = 23L;
        boolean result = userInterfaceInfoService.rollbackCount(userId, interfaceInfoId);
        assertEquals(true, result);
    }
}
