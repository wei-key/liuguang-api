package com.weikey.liuguangapisdk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 天气请求
 *
 * @author wei-key
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRequest implements Serializable {

    /**
     * 城市名
     */
    private String city = "北京";

    private static final long serialVersionUID = 1L;
}
