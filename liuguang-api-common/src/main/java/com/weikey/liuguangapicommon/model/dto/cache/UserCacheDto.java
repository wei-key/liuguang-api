package com.weikey.liuguangapicommon.model.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  用户在Redis中的缓存对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDto {


    private Long userId;

    private String secretKey;

}
