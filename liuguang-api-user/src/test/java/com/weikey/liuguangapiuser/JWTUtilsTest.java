package com.weikey.liuguangapiuser;

import com.weikey.liuguangapicommon.model.dto.common.JwtUserDto;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class JWTUtilsTest {

    @Test
    void test() {
        String token = JWTUtils.generateToken(new JwtUserDto(123L, "admin"));
        System.out.println("token=" + token);

        // 'Bearer<空格>' + token
        String header = "Bearer " + token;
        boolean verify = JWTUtils.verify(header);
        Assertions.assertTrue(verify);

        header = header + "abc";
        verify = JWTUtils.verify(header);
        Assertions.assertFalse(verify);
    }

}
