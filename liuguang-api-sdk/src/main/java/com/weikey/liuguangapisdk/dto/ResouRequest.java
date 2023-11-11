package com.weikey.liuguangapisdk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 热搜请求
 *
 * @author wei-key
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResouRequest implements Serializable {

    /**
     * 热搜条数
     */
    private Integer size = 10;

    private static final long serialVersionUID = 1L;
}
