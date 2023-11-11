package com.weikey.liuguangapiinterface.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.weikey.liuguangapicommon.constant.CommonConstant;
import com.weikey.liuguangapicommon.constant.MethodConstant;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.entity.UserInterfaceInfo;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.InterfaceInfoStatusEnum;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoInvokeVO;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoVO;
import com.weikey.liuguangapicommon.model.vo.RequestParamsRemarkVO;
import com.weikey.liuguangapicommon.model.vo.ResponseParamsRemarkVO;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapicommon.utils.SqlUtils;
import com.weikey.liuguangapicommon.utils.ThrowUtils;
import com.weikey.liuguangapiinterface.mapper.InterfaceInfoMapper;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import com.weikey.liuguangapiinterface.service.UserInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 肖
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 参数校验
     * 增加（add）时，增加所必须的参数都不能为空，并进行内容的校验；其他情况（不为add），哪些参数不为空，则校验哪些参数
     * => 增加（add）时，有必须传递的参数；其他情况（改、查），传参比较自由，没有必须要传递的
     *
     * @param interfaceInfo 用实体对象统一接收，调用此方法，需要先将参数对象转化为实体对象
     * @param add           判断是否为增加，根据布尔值参数add判断
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        Integer status = interfaceInfo.getStatus();
        String method = interfaceInfo.getMethod();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, url, method), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "命名过长");
        }
        if (StringUtils.isNotBlank(url) && url.length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "url过长");
        }
        if (StringUtils.isNotBlank(method) && !MethodConstant.validMethod(method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求类型错误");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
        if (status != null && status != InterfaceInfoStatusEnum.ONLINE.getValue()
                && status != InterfaceInfoStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态错误");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String searchText = interfaceInfoQueryRequest.getSearchText();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 拼接查询条件
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) { // 根据搜索文本对name和description做模糊查询
            queryWrapper.like("description", searchText).or().like("name", searchText);
        } else {
            queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
            queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        }
        queryWrapper.eq(status != null, "status", status);
        // mp自动过滤逻辑删除的数据，不需要自己手动加查询条件
        // queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public BaseResponse<Page<InterfaceInfoInvokeVO>> listInterfaceInfoInvokeVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                               HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        // todo 这里的限制爬虫是什么意思
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 1.分页查询接口信息
        Page<InterfaceInfo> interfaceInfoPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(interfaceInfoQueryRequest));

        // 2.查询当前用户调用接口的情况
        Long userId = userFeignClient.getLoginUser(request).getId();
        QueryWrapper<UserInterfaceInfo> userInterfaceInfoQueryWrapper = new QueryWrapper<>();
        userInterfaceInfoQueryWrapper.eq("userId", userId);
        Map<Long, List<UserInterfaceInfo>> map = userInterfaceInfoService.list(userInterfaceInfoQueryWrapper)
                .stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));

        // 3.将InterfaceInfo转为InterfaceInfoVo
        List<InterfaceInfo> records = interfaceInfoPage.getRecords();
        List<InterfaceInfoInvokeVO> interfaceInfoVoList = records.stream().map(interfaceInfo -> {
            InterfaceInfoInvokeVO interfaceInfoVo = new InterfaceInfoInvokeVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVo);

            // 设置requestParamsRemark、responseParamsRemark
            if (StrUtil.isNotBlank(interfaceInfo.getRequestParamsRemark())) { // 没有请求参数，requestParamsRemark为空
                Object requestParamsRemark = GSON.fromJson(interfaceInfo.getRequestParamsRemark(),
                        new TypeToken<List<RequestParamsRemarkVO>>() {
                        }.getType());
                interfaceInfoVo.setRequestParamsRemark((List<RequestParamsRemarkVO>) requestParamsRemark);
            }
            Object responseParamsRemark = GSON.fromJson(interfaceInfo.getResponseParamsRemark(),
                    new TypeToken<List<ResponseParamsRemarkVO>>() {
                    }.getType());
            interfaceInfoVo.setResponseParamsRemark((List<ResponseParamsRemarkVO>) responseParamsRemark);

            // 设置当前用户调用接口的情况
            List<UserInterfaceInfo> list = map.get(interfaceInfo.getId());
            if (CollectionUtil.isNotEmpty(list)) { // 用户开通了接口
                interfaceInfoVo.setIsOwnedByCurrentUser(true);
                UserInterfaceInfo userInterfaceInfo = list.get(0);
                interfaceInfoVo.setLeftNum(userInterfaceInfo.getLeftNum());
                interfaceInfoVo.setTotalNum(userInterfaceInfo.getTotalNum());
            } else { // 用户未开通接口
                interfaceInfoVo.setIsOwnedByCurrentUser(false);
            }

            return interfaceInfoVo;
        }).collect(Collectors.toList());

        Page<InterfaceInfoInvokeVO> voPage = new Page<>();
        BeanUtils.copyProperties(interfaceInfoPage, voPage);
        voPage.setRecords(interfaceInfoVoList);

        return ResultUtils.success(voPage);
    }

    @Override
    public BaseResponse<Page<InterfaceInfoInvokeVO>> listMyInterfaceInfoInvokeVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                                 HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫
        // todo 这里的限制爬虫是什么意思
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 1.查询接口信息
        List<InterfaceInfo> interfaceInfoList = this.list(this.getQueryWrapper(interfaceInfoQueryRequest));

        // 2.查询当前用户调用接口的情况
        Long userId = userFeignClient.getLoginUser(request).getId();
        QueryWrapper<UserInterfaceInfo> userInterfaceInfoQueryWrapper = new QueryWrapper<>();
        userInterfaceInfoQueryWrapper.eq("userId", userId);
        Map<Long, List<UserInterfaceInfo>> map = userInterfaceInfoService.list(userInterfaceInfoQueryWrapper)
                .stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));

        // 3.过滤当前用户未开通的接口
        // todo java8新特性
        Stream<InterfaceInfo> stream = interfaceInfoList.stream().filter(interfaceInfo -> map.get(interfaceInfo.getId()) != null);

        // 4.将InterfaceInfo转为InterfaceInfoVo
        List<InterfaceInfoInvokeVO> interfaceInfoVoList = stream.map(interfaceInfo -> {
            InterfaceInfoInvokeVO interfaceInfoVo = new InterfaceInfoInvokeVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVo);

            // 设置requestParamsRemark、responseParamsRemark
            if (StrUtil.isNotBlank(interfaceInfo.getRequestParamsRemark())) { // 没有请求参数，requestParamsRemark为空
                Object requestParamsRemark = GSON.fromJson(interfaceInfo.getRequestParamsRemark(),
                        new TypeToken<List<RequestParamsRemarkVO>>() {
                        }.getType());
                interfaceInfoVo.setRequestParamsRemark((List<RequestParamsRemarkVO>) requestParamsRemark);
            }
            Object responseParamsRemark = GSON.fromJson(interfaceInfo.getResponseParamsRemark(),
                    new TypeToken<List<ResponseParamsRemarkVO>>() {
                    }.getType());
            interfaceInfoVo.setResponseParamsRemark((List<ResponseParamsRemarkVO>) responseParamsRemark);

            // 设置当前用户调用接口的情况
            UserInterfaceInfo userInterfaceInfo = map.get(interfaceInfo.getId()).get(0);
            interfaceInfoVo.setIsOwnedByCurrentUser(true);
            interfaceInfoVo.setLeftNum(userInterfaceInfo.getLeftNum());
            interfaceInfoVo.setTotalNum(userInterfaceInfo.getTotalNum());

            return interfaceInfoVo;
        }).collect(Collectors.toList());

        // 5.构造page对象
        Page<InterfaceInfoInvokeVO> page = new Page<>();
        page.setCurrent(current);
        page.setSize(size);
        int total = interfaceInfoVoList.size();
        long pages = total / size + ((total % size > 0) ? 1 : 0);
        page.setTotal(total);
        page.setPages(pages);
        // 截取当前页的数据
        long start = (current - 1) * size;
        // todo java8新特性
        List<InterfaceInfoInvokeVO> sublist = interfaceInfoVoList.stream()
                .skip(start)
                .limit(size)
                .collect(Collectors.toList());
        page.setRecords(sublist);

        return ResultUtils.success(page);
    }

    @Override
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVO(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        // 限制爬虫: 一次性获取太多条数据，报错
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 1.分页查询接口信息
        Page<InterfaceInfo> interfaceInfoPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(interfaceInfoQueryRequest));

        // 2.将InterfaceInfo转为InterfaceInfoVo
        List<InterfaceInfo> records = interfaceInfoPage.getRecords();
        List<InterfaceInfoVO> interfaceInfoVoList = records.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVo = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVo);

            // 设置requestParamsRemark、responseParamsRemark
            if (StrUtil.isNotBlank(interfaceInfo.getRequestParamsRemark())) { // 没有请求参数，requestParamsRemark为空
                Object requestParamsRemark = GSON.fromJson(interfaceInfo.getRequestParamsRemark(),
                        new TypeToken<List<RequestParamsRemarkVO>>() {
                        }.getType());
                interfaceInfoVo.setRequestParamsRemark((List<RequestParamsRemarkVO>) requestParamsRemark);
            }
            Object responseParamsRemark = GSON.fromJson(interfaceInfo.getResponseParamsRemark(),
                    new TypeToken<List<ResponseParamsRemarkVO>>() {
                    }.getType());
            interfaceInfoVo.setResponseParamsRemark((List<ResponseParamsRemarkVO>) responseParamsRemark);

            return interfaceInfoVo;
        }).collect(Collectors.toList());

        Page<InterfaceInfoVO> voPage = new Page<>();
        BeanUtils.copyProperties(interfaceInfoPage, voPage);
        voPage.setRecords(interfaceInfoVoList);

        return ResultUtils.success(voPage);
    }

    @Override
    public BaseResponse<InterfaceInfoInvokeVO> getInterfaceInfoInvokeVOById(long id, HttpServletRequest request) {
        InterfaceInfo interfaceInfo = this.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        InterfaceInfoInvokeVO interfaceInfoInvokeVO = new InterfaceInfoInvokeVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoInvokeVO);

        // 设置requestParamsRemark、responseParamsRemark
        if (StrUtil.isNotBlank(interfaceInfo.getRequestParamsRemark())) { // 没有请求参数，requestParamsRemark为空
            Object requestParamsRemark = GSON.fromJson(interfaceInfo.getRequestParamsRemark(),
                    new TypeToken<List<RequestParamsRemarkVO>>() {
                    }.getType());
            interfaceInfoInvokeVO.setRequestParamsRemark((List<RequestParamsRemarkVO>) requestParamsRemark);
        }
        Object responseParamsRemark = GSON.fromJson(interfaceInfo.getResponseParamsRemark(),
                new TypeToken<List<ResponseParamsRemarkVO>>() {
                }.getType());
        interfaceInfoInvokeVO.setResponseParamsRemark((List<ResponseParamsRemarkVO>) responseParamsRemark);

        // 设置当前用户调用接口的情况
        Long userId = userFeignClient.getLoginUser(request).getId();
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("interfaceInfoId", id);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(queryWrapper);
        if (userInterfaceInfo != null) { // 用户开通了接口
            interfaceInfoInvokeVO.setIsOwnedByCurrentUser(true);
            interfaceInfoInvokeVO.setLeftNum(userInterfaceInfo.getLeftNum());
            interfaceInfoInvokeVO.setTotalNum(userInterfaceInfo.getTotalNum());
        } else { // 用户未开通接口
            interfaceInfoInvokeVO.setIsOwnedByCurrentUser(false);
        }

        return ResultUtils.success(interfaceInfoInvokeVO);
    }

    @Override
    public InterfaceInfo getInterface(String url, String method) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(url, method), ErrorCode.PARAMS_ERROR);
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

}




