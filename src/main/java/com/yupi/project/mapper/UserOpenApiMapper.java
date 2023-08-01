package com.yupi.project.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.yupi.project.common.OpenApiIdAndNums;

import java.util.List;

/**
* @author happyxianfish
* @description 针对表【user_open_api(用户调用接口关系表)】的数据库操作Mapper
* @createDate 2023-07-26 09:27:07
* @Entity com.yupi.project.model.entity.UserOpenApi
*/
public interface UserOpenApiMapper extends BaseMapper<UserOpenApi> {
    List<OpenApiIdAndNums> getTopOpenApiInvoke(int limit);

}




