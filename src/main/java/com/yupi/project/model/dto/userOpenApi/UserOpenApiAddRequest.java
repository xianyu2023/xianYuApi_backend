package com.yupi.project.model.dto.userOpenApi;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class UserOpenApiAddRequest implements Serializable {

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}