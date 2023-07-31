package com.yupi.project.model.dto.userOpenApi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.yupi.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserOpenApiQueryRequest extends PageRequest implements Serializable {

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
     * 总调用次数（范围查询）
     */
    private Integer totalNum;

    /**
     * 剩余调用次数（范围查询）
     */
    private Integer leftNum;

    /**
     * 调用权限（0-正常，1-禁止）
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}