package com.yupi.project.datasource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import com.yupi.project.model.dto.openApiRequest.OpenApiQueryRequest;
import com.yupi.project.service.OpenApiService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 来源于博天API
 * 采用适配器模式
 */
@Component
public class BoTianApiDataSource implements DataSource<OpenApiVO>{
    @Resource
    private OpenApiService openApiService;
    @Override
    public Page<OpenApiVO> doSearch(String searchText, long pageNum, long pageSize) {
        //适配器模式
        OpenApiQueryRequest openApiQueryRequest = new OpenApiQueryRequest();
        openApiQueryRequest.setSearchText(searchText);
        openApiQueryRequest.setCurrent(pageNum);
        openApiQueryRequest.setPageSize(pageSize);
        openApiQueryRequest.setOrigin("botian");
        //从ES查询
        Page<OpenApi> openApiPage = openApiService.searchFromEsBySearchText(openApiQueryRequest);
        return openApiService.getOpenApiVOPage(openApiPage);
    }
}
