package com.yupi.project.model.dto.openApiRequest;
import com.google.gson.Gson;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.util.Date;

/**
 * 帖子 ES 包装类
 *【就像数据库表需要一个实体类来接受一样。ES索引/文档/表也需要一个实体类来接受】
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 **/
// todo 取消注释开启 ES（须先配置 ES）
@Document(indexName = "openapi")
@Data
public class OpenApiEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口说明
     */
    private String description;

    /**
     * url
     */
    private String url;

    /**
     * method
     */
    private String method;

    /**
     * requestParams
     */
    private String requestParams;

    /**
     * requestHeader
     */
    private String requestHeader;

    /**
     * responseHeader
     */
    private String responseHeader;


    /**
     * 接口状态（0-关闭，1-开启）
     */
    private Integer status;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 创建时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除(0-未删, 1-已删)
     */
    private Integer isDeleted;

    /**
     * API请求路径
     */
    private String path;

    /**
     * API来源
     */
    private String origin;

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();

    /**
     * 对象转包装类
     *
     * @param openApi
     * @return
     */
    public static OpenApiEsDTO objToDto(OpenApi openApi) {
        if (openApi == null) {
            return null;
        }
        OpenApiEsDTO postEsDTO = new OpenApiEsDTO();
        BeanUtils.copyProperties(openApi, postEsDTO);
        return postEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param openApiEsDTO
     * @return
     */
    public static OpenApi dtoToObj(OpenApiEsDTO openApiEsDTO) {
        if (openApiEsDTO == null) {
            return null;
        }
        OpenApi openApi = new OpenApi();
        BeanUtils.copyProperties(openApiEsDTO, openApi);
        return openApi;
    }
}
