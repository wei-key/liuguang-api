package com.weikey.liuguangapiinterface.controller;

import com.weikey.liuguangapicommon.annotation.AuthCheck;
import com.weikey.liuguangapicommon.constant.UserConstant;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoCountVO;
import com.weikey.liuguangapiinterface.service.impl.AnalysisServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 接口统计分析
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Resource
    private AnalysisServiceImpl analysisService;

    /**
     * 统计接口总调用次数的topN
     *
     * @return 接口信息及总调用次数
     */
    @GetMapping("/top/invoke/interface")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<InterfaceInfoCountVO>> listTopInvokeInterface() {
        // todo 这里先写死了3个
        return analysisService.listTopInvokeInterface(3);
    }
}
