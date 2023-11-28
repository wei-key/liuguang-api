package com.weikey.liuguangapiinterface.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.weikey.liuguangapicommon.annotation.AuthCheck;
import com.weikey.liuguangapicommon.constant.UserConstant;
import com.weikey.liuguangapicommon.exception.BusinessException;
import com.weikey.liuguangapicommon.model.dto.common.DeleteRequest;
import com.weikey.liuguangapicommon.model.dto.common.IdRequest;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoAddRequest;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoUpdateRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.entity.User;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.enums.InterfaceInfoStatusEnum;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoInvokeVO;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoVO;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapicommon.utils.ThrowUtils;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import com.weikey.liuguangapisdk.client.ApiClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 接口信息接口
 *
 * @author wei-key
 */
@RestController
@RequestMapping("/interfaceInfo")
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private ApiClient apiClient;

    @Resource
    private UserFeignClient userFeignClient;

    private final static Gson GSON = new Gson();

    // region 增删改查 todo 这里的增删改查需要完善

    /**
     * 创建
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        // todo 校验是否发生异常：requestParamsRemark、responseParamsRemark是不同类型
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo); // 将参数对象转化为实体对象
        interfaceInfo.setRequestParamsRemark(GSON.toJson(interfaceInfoAddRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(GSON.toJson(interfaceInfoAddRequest.getResponseParamsRemark()));

        // todo 参数校验完善？
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true); // 参数校验
        interfaceInfo.setUserId(JWTUtils.getUidFromToken(request)); // 创建人为当前用户

        boolean result = interfaceInfoService.save(interfaceInfo); // 保存
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除（仅创建者或管理员可删除）
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅创建者或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(JWTUtils.getUidFromToken(request)) && !userFeignClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅创建者或管理员）
     * @param interfaceInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        // todo 校验是否发生异常：requestParamsRemark、responseParamsRemark是不同类型
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        interfaceInfo.setRequestParamsRemark(GSON.toJson(interfaceInfoUpdateRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(GSON.toJson(interfaceInfoUpdateRequest.getResponseParamsRemark()));

        // 参数校验
        // todo 参数校验完善？
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfoInvokeVO> getInterfaceInfoInvokeVOById(@RequestParam("id") long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.getInterfaceInfoInvokeVOById(id, request);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @return 接口信息
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // todo @AuthCheck怎么用的
    @PostMapping("/list/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVO(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.listInterfaceInfoVO(interfaceInfoQueryRequest);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return 接口信息及当前用户调用接口的情况
     */
    @PostMapping("/list/invoke/vo")
    public BaseResponse<Page<InterfaceInfoInvokeVO>> listInterfaceInfoInvokeVO(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.listInterfaceInfoInvokeVO(interfaceInfoQueryRequest, request);
    }

    /**
     * 分页获取列表（当前用户开通的接口）
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return 接口信息及当前用户调用接口的情况
     */
    @PostMapping("/list/my/invoke/vo")
    public BaseResponse<Page<InterfaceInfoInvokeVO>> listMyInterfaceInfoInvokeVO(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                               HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return interfaceInfoService.listMyInterfaceInfoInvokeVO(interfaceInfoQueryRequest, request);
    }

    // endregion

    /**
     * 发布接口
     * @param idRequest
     * @return
     *
     * 发布接口和下线接口只需要一个参数 id，但是仍然封装为对象，这样的好处是：统一，所有 post 接口都是统一在请求体中传 json 串
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 1.判断对应的接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 2.使用反射校验接口是否可以调用
        // todo apiClient的ak、sk是配置文件写死的、某个管理员的
        // todo 待完善：这里上线接口也要管理员有相应接口的调用次数，需要改进
        String name = oldInterfaceInfo.getName();
        Method method = getMethod(name); // 方法对象
        Class<?>[] parameterTypes = method.getParameterTypes(); // 方法参数类型

        Object result = null;
        if (parameterTypes.length == 0) { // 2.1接口方法没有参数，直接调用
            try {
                result = method.invoke(apiClient);
            } catch (Exception e) { // 接口调用失败的异常也会在这里被处理
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        } else { // 2.2接口方法有参数
            try {
                // 直接new对象即可，属性有默认值
                Object req = parameterTypes[0].newInstance();
                result = method.invoke(apiClient, req);
            } catch (Exception e) { // 接口调用失败的异常也会在这里被处理
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }
        // todo 待完善：什么情况下接口校验失败
        ThrowUtils.throwIf(result == null, ErrorCode.SYSTEM_ERROR, "接口校验失败");

        // 3.更新接口状态：将接口信息的status字段改为1
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean update = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(update);
    }

    /**
     * 下线接口
     * @param idRequest
     * @return
     *
     * 发布接口和下线接口只需要一个参数 id，但是仍然封装为对象，这样的好处是：统一，所有 post 接口都是统一在请求体中传 json 串
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新接口状态：将接口信息的status字段改为0
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 接口在线调用
     * @param interfaceInfoInvokeRequest
     * @param request
     * @return
     *
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                      HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = interfaceInfoInvokeRequest.getId();
        // 1.判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 2.校验接口状态
        Integer status = oldInterfaceInfo.getStatus();
        ThrowUtils.throwIf(status == InterfaceInfoStatusEnum.OFFLINE.getValue(), ErrorCode.SYSTEM_ERROR, "接口已关闭");
        // 3.通过反射调用sdk中接口对应的方法
        // todo 这里ak、sk是当前登录用户的，在线调用也会扣减用户的调用次数
        User loginUser = userFeignClient.getLoginUser(JWTUtils.getUidFromToken(request)); // 获取当前登录用户
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        ApiClient apiClient = new ApiClient(accessKey, secretKey);

        String name = oldInterfaceInfo.getName();
        Method method = getMethod(name); // 方法对象
        Class<?>[] parameterTypes = method.getParameterTypes(); // 方法参数
        Object result = null;
        if (parameterTypes.length == 0) { // 3.1接口方法没有参数，直接调用
            try {
                result = method.invoke(apiClient);
            } catch (IllegalAccessException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            } catch (InvocationTargetException e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getTargetException().getMessage());
            }
        } else { // 3.2接口方法有参数
            String json = interfaceInfoInvokeRequest.getRequestParams();
            // json转为java对象可能会出现异常，由全局异常处理器处理
            Object req = GSON.fromJson(json, parameterTypes[0]); // json转为req对象
            try {
                result = method.invoke(apiClient, req);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
            }
        }

        return ResultUtils.success(result);
    }

    /**
     * 根据接口名获取sdk中对应的方法对象
     * todo 可优化：做缓存，即name -> method的Map，就不需要每次都遍历数组；而是当缓存中找不到的时候，再去遍历数组（sdk中方法更新，而缓存中没有更新）
     * @param name
     * @return
     */
    private Method getMethod(String name) {
        Class<ApiClient> cls = ApiClient.class;
        // 获取指定的方法
        Method method = null;
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                method = m;
                break;
            }
        }
        // todo 异常完善
        if (method == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return method;
    }

}
