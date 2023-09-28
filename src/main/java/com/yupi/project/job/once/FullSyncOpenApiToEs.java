package com.yupi.project.job.once;


import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.yupi.project.esdao.OpenApiEsDao;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import com.yupi.project.service.OpenApiService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * logstash
 * 全量同步mysql数据到 es
 *
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncOpenApiToEs implements CommandLineRunner {

    @Resource
    private OpenApiService openApiService;

    @Resource
    private OpenApiEsDao openApiEsDao;

    @Override
    public void run(String... args) {
        List<OpenApi> openApiList = openApiService.list();
        if (CollectionUtils.isEmpty(openApiList)) {
            return;
        }
        List<OpenApiEsDTO> openApiEsDTOList = openApiList.stream().map(OpenApiEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = openApiEsDTOList.size();
        log.info("FullSyncOpenApiToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            openApiEsDao.saveAll(openApiEsDTOList.subList(i, end));
        }
        log.info("FullSyncOpenApiToEs end, total {}", total);
    }
}
