package com.yupi.project.service.impl;
import java.util.Date;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.OpenApiMapper;
import com.yupi.project.service.OpenApiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author happyxianfish
* @description 针对表【open_api(开放接口信息)】的数据库操作Service实现
* @createDate 2023-07-21 00:08:21
*/
@Service
public class OpenApiServiceImpl extends ServiceImpl<OpenApiMapper, OpenApi>
    implements OpenApiService {
    @Override
    public void validOpenApi(OpenApi openApi, boolean add) {
        if (openApi == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = openApi.getId();
        String name = openApi.getName();
        String description = openApi.getDescription();
        String url = openApi.getUrl();
        String method = openApi.getMethod();
        String requestParams = openApi.getRequestParams();
        String requestHeader = openApi.getRequestHeader();
        String responseHeader = openApi.getResponseHeader();
        Integer status = openApi.getStatus();
        Long userId = openApi.getUserId();
        Date createTime = openApi.getCreateTime();
        Date updateTime = openApi.getUpdateTime();
        Integer isDeleted = openApi.getIsDeleted();
        // 创建时，所有参数必须非空
        if (add) {
            //|| ObjectUtils.anyNull(userId)
            if (StringUtils.isAnyBlank(name, description, url, method, requestHeader,responseHeader)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        //todo 校验不完整
        if (StringUtils.isNotBlank(name) && name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口名称过长");
        }
//        if (userId == null || userId <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建人id不符合要求");
//        }
    }
}




