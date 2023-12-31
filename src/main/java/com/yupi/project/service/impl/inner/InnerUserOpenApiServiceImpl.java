package com.yupi.project.service.impl.inner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.xianyu.xianyucommon.service.InnerUserOpenApiService;
import com.yupi.project.service.UserOpenApiService;
import org.apache.dubbo.config.annotation.DubboService;
import javax.annotation.Resource;

@DubboService
public class InnerUserOpenApiServiceImpl implements InnerUserOpenApiService {
    @Resource
    private UserOpenApiService userOpenApiService;
    @Override
    public boolean invokeCount(Long userId, Long openApiId) {
//      接口调用成功后，  用户调用接口次数+1（修改表）
        return userOpenApiService.invokeCount(userId,openApiId);
    }

    @Override
    public UserOpenApi judgeUserRight(Long userId, Long apiId) {
        QueryWrapper<UserOpenApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("openApiId",apiId);
        return userOpenApiService.getOne(queryWrapper);
    }

}
