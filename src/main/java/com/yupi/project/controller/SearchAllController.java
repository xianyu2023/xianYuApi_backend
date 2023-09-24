package com.yupi.project.controller;
import com.yupi.project.common.BaseResponse;
import com.yupi.project.common.ResultUtils;
import com.yupi.project.manager.SearchFacade;
import com.yupi.project.model.dto.search.SearchAllRequest;
import com.yupi.project.model.vo.SearchAllVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * 统一查询接口
 * 其实现可以提供两个功能，一种是点哪个tab栏就查该种类型的数据（根据tab字段来区分类别）
 * 另外一种是搞个聚合搜索，一次性查出多种类型的数据
 *
 * @author happyxianfish
 */
@RestController
@Slf4j
@RequestMapping("/search")
public class SearchAllController {
    @Resource
    private SearchFacade searchFacade;

    /**
     * 要支持json，得用requestBody注解
     * 把查询帖子、图片、用户做成并发，同时查询，但是可能有短板效应（短板影响整体）
     *
     * @return
     */
    @PostMapping("/all")
    public BaseResponse<SearchAllVO> searchAll(@RequestBody SearchAllRequest searchAllRequest, HttpServletRequest request) {
        return ResultUtils.success(searchFacade.searchAll(searchAllRequest, request));
    }
}
