package com.weikey.liuguangapicommon.model.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  接口在Redis中的缓存对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceCacheDto {


    private Long id;

    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

}
