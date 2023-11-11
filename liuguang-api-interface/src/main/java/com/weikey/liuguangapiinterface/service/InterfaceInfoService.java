package com.weikey.liuguangapiinterface.service;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoInvokeVO;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;

/*
 *
 * @author wei-key
 * @description 针对表【interface_info(接口信息)】的数据库操作Service
 * @createDate 2023-07-11 10:29:28
 */


public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @return 接口信息及当前用户调用接口的情况
     */
    BaseResponse<Page<InterfaceInfoInvokeVO>> listInterfaceInfoInvokeVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                        HttpServletRequest request);

    /**
     * 分页获取列表（当前用户开通的接口）
     *
     * @param interfaceInfoQueryRequest
     * @return 接口信息及当前用户调用接口的情况
     */
    BaseResponse<Page<InterfaceInfoInvokeVO>> listMyInterfaceInfoInvokeVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                        HttpServletRequest request);

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @return 接口信息
     */
    BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * 根据id获取
     * @param id
     * @return
     */
    BaseResponse<InterfaceInfoInvokeVO> getInterfaceInfoInvokeVOById(long id, HttpServletRequest request);

    InterfaceInfo getInterface(String url, String method);
}
