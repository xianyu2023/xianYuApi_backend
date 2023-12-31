package com.yupi.project.esdao;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * OpenApi ES 操作
 *
 */

public interface OpenApiEsDao extends ElasticsearchRepository<OpenApiEsDTO,Long> {

    /**
     * 通过接口创建人id查询接口
     * @param userId
     * @return
     */
    List<OpenApiEsDTO> findByUserId(Long userId);

    /**
     * 通过接口名称查询接口
     * @param title
     * @return
     */
    List<OpenApiEsDTO> findByName(String title);
}