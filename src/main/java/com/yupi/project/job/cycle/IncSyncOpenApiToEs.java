package com.yupi.project.job.cycle;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.yupi.project.esdao.OpenApiEsDao;
import com.yupi.project.mapper.OpenApiMapper;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * logstash
 * 增量同步mysql数据到 es
 *
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncSyncOpenApiToEs {

    @Resource
    private OpenApiMapper openApiMapper;

    @Resource
    private OpenApiEsDao openApiEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内更改的数据
        Date fiveMinutesAgoDate = new Date(System.currentTimeMillis() - 5 * 60 * 1000L);
        List<OpenApi> openApiList = openApiMapper.listOpenApiWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(openApiList)) {
            log.info("no inc openApi");
            return;
        }
        List<OpenApiEsDTO> openApiEsDTOList = openApiList.stream()
                .map(OpenApiEsDTO::objToDto)
                .collect(Collectors.toList());
        final int pageSize = 500;
        int total = openApiEsDTOList.size();
        log.info("IncSyncOpenApiToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            openApiEsDao.saveAll(openApiEsDTOList.subList(i, end));
        }
        log.info("IncSyncOpenApiToEs end, total {}", total);
        //不知道为什么debug就是到不了这一步
    }
}
