package com.xianyu.xianyucommon.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author happyxianfish
 */
@Data
public class UserOpenApiVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 调用者id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long openApiId;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 调用权限（0-正常，1-禁止）
     */
    private Integer status;

    /**
     * 接口名称
     */
    private String name;

    private static final long serialVersionUID = 1L;
}
