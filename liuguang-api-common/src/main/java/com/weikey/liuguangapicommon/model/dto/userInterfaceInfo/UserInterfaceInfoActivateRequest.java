package com.weikey.liuguangapicommon.model.dto.userInterfaceInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 开通接口请求
 *
 * @author wei-key
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInterfaceInfoActivateRequest implements Serializable {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 调用次数
     */
    private Integer amount;

    /**
     * 用户id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
