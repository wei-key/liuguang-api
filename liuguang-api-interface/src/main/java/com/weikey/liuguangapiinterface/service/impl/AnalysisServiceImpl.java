package com.weikey.liuguangapiinterface.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoCountVO;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapiinterface.mapper.UserInterfaceInfoMapper;
import com.weikey.liuguangapiinterface.service.AnalysisService;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public BaseResponse<List<InterfaceInfoCountVO>> listTopInvokeInterface(int n) {
        // 只拿到InterfaceInfoCountVo的2个字段：id和totalCount
        List<InterfaceInfoCountVO> interfaceInfoCountVoList = userInterfaceInfoMapper.listTopInvokeCount(3);
        // todo java8新特性
        // 根据id分组
        Map<Long, List<InterfaceInfoCountVO>> map = interfaceInfoCountVoList.stream()
                .collect(Collectors.groupingBy(InterfaceInfoCountVO::getId));
        // 拿到接口的完整信息
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.listByIds(map.keySet());
        // 根据interfaceInfo补充interfaceInfoCountVo的属性值
        List<InterfaceInfoCountVO> result = interfaceInfoList.stream().map(interfaceInfo -> {
            InterfaceInfoCountVO interfaceInfoCountVo = map.get(interfaceInfo.getId()).get(0);
            BeanUtil.copyProperties(interfaceInfo, interfaceInfoCountVo);
            return interfaceInfoCountVo;
        }).collect(Collectors.toList());

        return ResultUtils.success(result);
    }
}
