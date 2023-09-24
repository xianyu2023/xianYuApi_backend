package com.yupi.project.manager;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.datasource.*;
import com.yupi.project.enums.SearchTypeEnum;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.exception.ThrowUtils;
import com.yupi.project.model.dto.search.SearchAllRequest;
import com.yupi.project.model.vo.SearchAllVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * 搜索门面(门面（柜台）设计模式)
 * @author happyxianfish
 */
@Component
@Slf4j
public class SearchFacade {
    @Resource
    private LocalApiDataSource localApiDataSource;

    @Resource
    private BoTianApiDataSource boTianApiDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchAllVO searchAll(SearchAllRequest searchAllRequest, HttpServletRequest request) {
        String type = searchAllRequest.getType();
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);

        String searchText = searchAllRequest.getSearchText();
        long current = searchAllRequest.getCurrent();
        long pageSize = searchAllRequest.getPageSize();

        //搜索出所有数据。todo优化，改为并发查询（线程池），可能提高查询速度
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        if (searchTypeEnum==null) {
            //（1）初次加载，type为null，查询一次所有数据

            //创建一个异步任务
            CompletableFuture<Page<OpenApiVO>> localOpenApiTask = CompletableFuture.supplyAsync(() -> {
                //查询本地服务器接口
                return localApiDataSource.doSearch(searchText, current, pageSize);//数据源接入规范+适配器模式//es搜索引擎
            });
            //创建一个异步任务
            CompletableFuture<Page<OpenApiVO>> botianOpenApiTask = CompletableFuture.supplyAsync(() -> {
                //查询博天API接口
                return boTianApiDataSource.doSearch(searchText, current, pageSize);
            });
            //同时执行所有异步任务（并发）,join阻塞后续代码立刻执行
            CompletableFuture.allOf(localOpenApiTask,botianOpenApiTask).join();
            try {
                Page<OpenApiVO> local = localOpenApiTask.get();
                Page<OpenApiVO> botian = botianOpenApiTask.get();
                SearchAllVO searchAllVO = new SearchAllVO();
                searchAllVO.setLocalOpenApiVOList(local.getRecords());
                searchAllVO.setBotianOpenApiVOList(botian.getRecords());
                return searchAllVO;
            } catch (Exception e) {
                log.error("查询异常" + e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查询异常");
            }
        } else {
            //（2）只查询本地/博天
            SearchAllVO searchAllVO = new SearchAllVO();
            //不用大量的switch、if-else。todo优化
//            switch (searchTypeEnum) {
//                case LOCAL:
//                    dataSource = postDataSource;
//                    break;
//                case BOTIAN:
//                    dataSource = userDataSource;
//                    break;
//                default:
//            }
            //注册器模式（单例模式的扩展）
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            ThrowUtils.throwIf(dataSource==null,ErrorCode.PARAMS_ERROR);
            Page<?> page = dataSource.doSearch(searchText, current, pageSize);
            searchAllVO.setDataList(page.getRecords());
            return searchAllVO;
        }
    }
}
