package com.yupi.project.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xianyu.apiClientAdmin.client.XianYuOpenApiClient;
import com.xianyu.apiClientAdmin.utils.SignUtils;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import com.yupi.project.common.ErrorCode;
import com.yupi.project.common.OpenApiIdAndNums;
import com.yupi.project.constant.CommonConstant;
import com.yupi.project.exception.BusinessException;
import com.yupi.project.mapper.OpenApiMapper;
import com.yupi.project.mapper.UserOpenApiMapper;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import com.yupi.project.model.dto.openApiRequest.OpenApiInvokeRequest;
import com.yupi.project.model.dto.openApiRequest.OpenApiQueryRequest;
import com.yupi.project.service.OpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.zookeeper.Op;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author happyxianfish
 * @description 针对表【open_api(开放接口信息)】的数据库操作Service实现
 * @createDate 2023-07-21 00:08:21
 */
@Slf4j
@Service
public class OpenApiServiceImpl extends ServiceImpl<OpenApiMapper, OpenApi>
        implements OpenApiService {


    @Resource
    private UserOpenApiMapper userOpenApiMapper;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private static final String GATEWAY_HOST = "http://localhost:8090/api";

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
            if (StringUtils.isAnyBlank(name, description, url, method, requestHeader, responseHeader)) {
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


    // region 参数校验
    @Override
    public boolean judgeApiById(Long id) {
        //接口是否存在
        OpenApi oldOpenApi = getById(id);
        if (oldOpenApi == null) {
            return false;
        }
        //接口是否开启
        Integer status = oldOpenApi.getStatus();
        if (status == 0) {
            return false;
        }
        //优势：调用接口的method、url（host、path）不需要用户考虑，我们API后台可根据接口id直接从数据库查询。
        // 在线调用只需要考虑传入的请求参数
        //离线+SDK客户端需设置好正确的ak、sk（sign）。考虑调用哪个id的接口、请求参数
        return true;
    }

    // endregion

    //region 业务方法
    @Override
    public String invokeApiByOnline(OpenApiInvokeRequest openApiInvokeRequest, OpenApi openApi, User loginUser) {
        //已登录+在线测试
        //可查询登录用户的ak、sk
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        if (StringUtils.isAnyBlank(accessKey, secretKey)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "用户的ak、sk异常");
        }
        //根据用户的ak、sk去new一个客户端SDK【注解自动注入创建的客户端SDK是根据平台的ak、sk创建的，不是用户的】
        String body = openApiInvokeRequest.getUserRequestParams();
        body = Optional.ofNullable(body).orElse("");
        String sign = SignUtils.genSign(body, secretKey);
        return getApiService(openApi, accessKey, body, sign);
    }

    private String getApiService(OpenApi openApi, String accessKey, String body, String sign) {
        XianYuOpenApiClient xianYuApiClient = new XianYuOpenApiClient(accessKey, sign);
        //使用客户端SDK(Admin版)的调用接口方法向网关发送请求
        //String result = xianYuApiClient.getUserNameByPost(user);【todo调用接口时。固定方法名=>根据接口的地址来调用】
        //根据被调用接口的请求方式，选择合适的调用接口方法
        String method = openApi.getMethod();
        String path = openApi.getPath();
        //String url = openApi.getUrl(); todo host给网关后，不不知道如何用网关路由动态转发
        if (StringUtils.isBlank(method)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "接口资源method错误");
        }
        String result = null;
        if ("GET".equals(method) || "get".equals(method) || "get/post".equals(method)) {
            result = xianYuApiClient.getApiServiceByGet(openApi.getId(), body, path, GATEWAY_HOST);
        }
        if ("POST".equals(method) || "post".equals(method)) {
            result = xianYuApiClient.getApiServiceByPost(openApi.getId(), body, path, GATEWAY_HOST);
        }
        if (StringUtils.isBlank(result)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "获取接口资源失败");
        }
        return result;
    }


    @Override
    public String invokeApiBySdk(OpenApiInvokeRequest openApiInvokeRequest, HttpServletRequest request, OpenApi openApi) {
        //前端在线测试(未登录) 或者 离线+SDK客户端ak、sk(以sign签名的形式)
        Long id = openApiInvokeRequest.getId();
        String body = openApiInvokeRequest.getUserRequestParams();
        //获取请求头中的ak、sign
        String accessKey = request.getHeader("accessKey");
        String sign = request.getHeader("sign");
        if (StringUtils.isAllBlank(accessKey, sign)) {
            //前端在线测试(未登录)
            //todo 考虑给未登录客户也给与一定次数的调用
            //思路：对于游客，我们去注册一个临时用户（id、ak、sk）【临时用户的账号要不一样（youke+请求的ip地址+随机数）（因为账号需要去生成ak、sk且要临时登录）】，
            // 立刻让游客账号临时登录，并且在他去调用接口时，给这个临时用户开通一定次数的调用权限，用户角色可设置为游客，以后可以统一删除游客以及游客id调用接口的关系
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return getApiService(openApi, accessKey, body, sign);
    }


    @Override
    public Page<OpenApi> searchFromEsBySearchText(OpenApiQueryRequest openApiQueryRequest) {
        //1.取参数
        String origin = openApiQueryRequest.getOrigin();
        Long id = openApiQueryRequest.getId();
        String searchText = openApiQueryRequest.getSearchText();
        String name = openApiQueryRequest.getName();
        String description = openApiQueryRequest.getDescription();
        Long userId = openApiQueryRequest.getUserId();
        // es 起始页为 0
        long current = openApiQueryRequest.getCurrent() - 1;
        long pageSize = openApiQueryRequest.getPageSize();
        String sortField = openApiQueryRequest.getSortField();
        String sortOrder = openApiQueryRequest.getSortOrder();
        //2.将参数组合成支持es的搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDeleted", 0));
        boolQueryBuilder.filter(QueryBuilders.termQuery("origin", origin));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("name", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(name)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("name", name));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(description)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", description));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<OpenApiEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, OpenApiEsDTO.class);
        //3.从返回值中取结果
        Page<OpenApi> page = new Page<>();
        page.setSize(openApiQueryRequest.getPageSize());
        page.setCurrent(openApiQueryRequest.getCurrent());
        page.setTotal(searchHits.getTotalHits());
        List<OpenApi> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<OpenApiEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            // 从数据库中取出更完整的数据
            List<OpenApi> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<OpenApi>> idPostMap = postList.stream().collect(Collectors.groupingBy(OpenApi::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), OpenApiEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public Page<OpenApiVO> getOpenApiVOPage(Page<OpenApi> openApiPage) {
        List<OpenApi> records = openApiPage.getRecords();
        Page<OpenApiVO> openApiVOPage = new Page<>(openApiPage.getCurrent(), openApiPage.getSize(), openApiPage.getTotal());
        if (CollectionUtils.isEmpty(records)) {
            return openApiVOPage;
        }
        List<OpenApiIdAndNums> idAndNums = userOpenApiMapper.getOpenApiInvoke();//接口不包含所有的接口，只包含被调用过的
        Map<Long, List<OpenApiIdAndNums>> map = idAndNums.stream().collect(Collectors.groupingBy(OpenApiIdAndNums::getOpenApiId));
        List<OpenApiVO> openApiVOList = records.stream().map(openApi -> {
            OpenApiVO openApiVO = OpenApiVO.objToVo(openApi);
//                if (map.get(openApi.getId())==null) {
//                    log.error("map.get(openApi.getId())是null");
//                }
            //注意，这里的openApi是从外界传入的，它的接口id在用户接口关系表里可能没有（有些接口可能从来没被调用过）
            Long openApiId = openApi.getId();
            if (map.get(openApiId)==null) {
                //接口从未被调用，totalNums设置为0
                openApiVO.setTotalNums(0);
                return openApiVO;
            }
            Integer totalNums = map.get(openApiId).get(0).getTotalNums();
            openApiVO.setTotalNums(totalNums);
            return openApiVO;
        }).collect(Collectors.toList());
        return openApiVOPage.setRecords(openApiVOList);

    }
}




