package com.yupi.project.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import com.yupi.project.annotation.AuthCheck;
import com.yupi.project.common.BaseResponse;
import com.yupi.project.common.OpenApiIdAndNums;
import com.yupi.project.common.ResultUtils;
import com.yupi.project.constant.UserConstant;
import com.yupi.project.mapper.UserOpenApiMapper;
import com.yupi.project.service.OpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析控制器
 *仅管理员可用的统计分析功能
 * @author yupi
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Resource
    private UserOpenApiMapper userOpenApiMapper;
    @Resource
    private OpenApiService openApiService;

    /**
     *
     *获取总调用次数前N的接口信息
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/openApi/invoke/top")
    public BaseResponse<List<OpenApiVO>> getTopOpenApiInvoke() {
        //SQL查询topN
        List<OpenApiIdAndNums> topOpenApiList = userOpenApiMapper.getTopOpenApiInvoke(3);
        //给topOpenApiList里的每个元素添加一个id标识（通过流的分组功能，得到一个map，id作为key（标识））
        Map<Long, List<OpenApiIdAndNums>> map = topOpenApiList.stream().collect(Collectors.groupingBy(OpenApiIdAndNums::getOpenApiId));
        //编程式 关联查询open_api表（由于取的是topN，数据不多，所以关联查询效率还可以）
        QueryWrapper<OpenApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",map.keySet());
        List<OpenApi> openApiList = openApiService.list(queryWrapper);
        List<OpenApiVO> openApiVOList = openApiList.stream().map(openApi -> {
            OpenApiVO openApiVO = new OpenApiVO();
            BeanUtils.copyProperties(openApi, openApiVO);
            Integer totalNums = map.get(openApi.getId()).get(0).getTotalNums();
            openApiVO.setTotalNums(totalNums);
            return openApiVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(openApiVOList);
    }
}
