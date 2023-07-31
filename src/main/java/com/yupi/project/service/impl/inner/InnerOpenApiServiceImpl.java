package com.yupi.project.service.impl.inner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.service.InnerOpenApiService;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.OpenApiMapper;
import com.yupi.project.service.OpenApiService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import javax.annotation.Resource;

@DubboService
public class InnerOpenApiServiceImpl implements InnerOpenApiService {
    @Resource
    private OpenApiMapper openApiMapper;
    @Override
    public OpenApi getInvokeOpenApi(String url, String method) {
//        根据接口的url和请求方法获取接口
        if (StringUtils.isAnyBlank(url,method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<OpenApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url",url);
        queryWrapper.eq("method",method);
        return openApiMapper.selectOne(queryWrapper);
    }
}
