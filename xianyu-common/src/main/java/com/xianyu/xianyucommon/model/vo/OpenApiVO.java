package com.xianyu.xianyucommon.model.vo;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import lombok.Data;


/**
 * 接口信息封装视图
 *
 * @TableName open_api
 */
@Data
public class OpenApiVO extends OpenApi {
    /**
     * 每个接口的被调用总次数
     */
    private Integer totalNums;
    private static final long serialVersionUID = 1L;
}