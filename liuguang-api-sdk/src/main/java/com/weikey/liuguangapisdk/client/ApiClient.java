package com.weikey.liuguangapisdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.weikey.liuguangapisdk.constant.HeaderConstant;
import com.weikey.liuguangapisdk.dto.ResouRequest;
import com.weikey.liuguangapisdk.dto.WeatherRequest;
import com.weikey.liuguangapisdk.exception.ApiError;
import com.weikey.liuguangapisdk.exception.ApiException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.weikey.liuguangapisdk.utils.SignUtils.getSign;


/**
 * 调用第三方接口的客户端
 *
 * @author wei-key
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiClient {

    private String accessKey;

    private String secretKey;

    private static final String PREFIX = "http://127.0.0.1:8401/api/interface-service";

    /**
     * 百度热搜接口
     * @param resouRequest 当参数不合法（resouRequest为null、属性size为null或者size小于等于0），默认接口条数为10
     * @return
     */
    public String getBaiduResou(ResouRequest resouRequest) {
        int size = 10;
        if (resouRequest != null && resouRequest.getSize() != null && resouRequest.getSize() > 0) {
            size = resouRequest.getSize();
        }
        HttpResponse httpResponse = HttpRequest.get(PREFIX + "/resou/baidu")
                .addHeaders(getHeaders())
                .form("size", size)
                .execute();
        return checkError(httpResponse);
    }

    /**
     * 知乎热搜接口
     * @return
     */
    public String getZhihuResou() {
        HttpResponse httpResponse = HttpRequest.get(PREFIX + "/resou/zhihu")
                .addHeaders(getHeaders())
                .execute();
        return checkError(httpResponse);
    }

    /**
     * 微博热搜接口
     * @param resouRequest 当参数不合法（resouRequest为null、属性size为null或者size小于等于0），默认接口条数为10
     * @return
     */
    public String getWeiboResou(ResouRequest resouRequest) {
        int size = 10;
        if (resouRequest != null && resouRequest.getSize() != null && resouRequest.getSize() > 0) {
            size = resouRequest.getSize();
        }
        HttpResponse httpResponse = HttpRequest.get(PREFIX + "/resou/weibo")
                .addHeaders(getHeaders())
                .form("size", size)
                .execute();
        return checkError(httpResponse);
    }

    /**
     * 今日实况天气接口
     * @return
     */
    public String getWeather(WeatherRequest weatherRequest) {
        String city = "北京";
        if (weatherRequest != null && StrUtil.isNotBlank(weatherRequest.getCity())) {
            city = weatherRequest.getCity();
        }
        HttpResponse httpResponse = HttpRequest.get(PREFIX + "/weather")
                .addHeaders(getHeaders())
                .form("city", city)
                .execute();
        return checkError(httpResponse);
    }

    /**
     * 历史上的今天发生的事件
     * @return
     */
    public String todayInHistory() {
        HttpResponse httpResponse = HttpRequest.get(PREFIX + "/event/today/in/history")
                .addHeaders(getHeaders())
                .execute();
        return checkError(httpResponse);
    }


    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        // 用于生成签名的参数
        String param = RandomUtil.randomString(10);
        headers.put("accessKey", accessKey);
        headers.put("param", param);
        headers.put("sign", getSign(param, secretKey));
        headers.put("nonce", RandomUtil.randomNumbers(10));
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return headers;
    }

    private String checkError(HttpResponse httpResponse) {
        String header = httpResponse.header(HeaderConstant.RESP_ERROR_NAME);
        String body = httpResponse.body();

        // 响应状态码为429，被限流
        if(httpResponse.getStatus() == 429) {
            throw new ApiException(ApiError.FLOW_CONTROL);
        }

        // 响应状态码不为200
        if(httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            throw new ApiException("CodeError", body);
        }

        // 正常响应，但是带有此响应头,表示接口调用出现异常
        if (header != null) {
            body = body.replace("\"", "");
            ApiError apiError = ApiError.valueOf(body);
            throw new ApiException(apiError);
        }
        return body;
    }

}