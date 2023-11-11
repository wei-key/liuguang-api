package com.weikey.liuguangapiinterface.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weikey.liuguangapicommon.model.entity.UserInterfaceInfo;
import com.weikey.liuguangapicommon.model.vo.InterfaceInfoCountVO;

import java.util.List;

/**
* @author wei-key
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-07-11 10:35:13
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    /**
     * 统计接口总调用次数的topN
     *
     * @param limit 统计前limit个接口
     * @return
     */
    List<InterfaceInfoCountVO> listTopInvokeCount(int limit);

}




