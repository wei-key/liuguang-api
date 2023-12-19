package com.weikey.liuguangapiinterface.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoCountVO;
import com.weikey.liuguangapicommon.model.vo.RankingVO;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import com.weikey.liuguangapiinterface.mapper.UserInterfaceInfoMapper;
import com.weikey.liuguangapiinterface.service.AnalysisService;
import com.weikey.liuguangapiinterface.service.InterfaceInfoService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.weikey.liuguangapicommon.constant.RedisKeyConstant.RANKING_KEY;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public BaseResponse<List<InterfaceInfoCountVO>> listTopInvokeInterface(int n) {
        // 只拿到InterfaceInfoCountVo的2个字段：id和totalCount
        List<InterfaceInfoCountVO> interfaceInfoCountVoList = userInterfaceInfoMapper.listTopInvokeCount(3);
        // todo java8新特性
        // 根据id分组
        Map<Long, List<InterfaceInfoCountVO>> map = interfaceInfoCountVoList.stream().collect(Collectors.groupingBy(InterfaceInfoCountVO::getId));
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

    @Override
    public BaseResponse<List<RankingVO>> interfaceRank(int n) {
        // zset降序排列后（次数从多到少），取出前 N 个
        Set<ZSetOperations.TypedTuple<String>> set = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0L, n - 1L); // 索引从0开始，左右都是闭区间

        // 根据接口id分组
        Map<String, List<ZSetOperations.TypedTuple<String>>> map = set.stream()
                .collect(Collectors.groupingBy(ZSetOperations.TypedTuple::getValue));

        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.listByIds(map.keySet());

        // 根据接口id分组
        Map<Long, List<InterfaceInfo>> longListMap = interfaceInfoList.stream()
                .collect(Collectors.groupingBy(InterfaceInfo::getId));

        // 必须是对redis取出的zset进行map操作（而不是interfaceInfoList），这样最终得到的结果才具备顺序
        List<RankingVO> rankingVOList = set.stream().map(tuple -> {
            InterfaceInfo interfaceInfo = longListMap.get(Long.parseLong(tuple.getValue())).get(0);

            RankingVO rankingVO = new RankingVO();
            rankingVO.setName(interfaceInfo.getName());
            rankingVO.setDescription(interfaceInfo.getDescription());
            rankingVO.setCount(tuple.getScore().intValue());
            return rankingVO;
        }).collect(Collectors.toList());

        return ResultUtils.success(rankingVOList);
    }
}
