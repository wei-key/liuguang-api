package com.weikey.liuguangapiinterface.service;


import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoCountVO;

import java.util.List;

public interface AnalysisService {

    /**
      * 统计接口总调用次数的topN
      *
      * @param n 统计前n个接口
      * @return 接口信息及总调用次数
      */
     BaseResponse<List<InterfaceInfoCountVO>> listTopInvokeInterface(int n);

}
