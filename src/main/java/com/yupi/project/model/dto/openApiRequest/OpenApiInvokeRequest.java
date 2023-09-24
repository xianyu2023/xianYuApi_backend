package com.yupi.project.model.dto.openApiRequest;

import com.baomidou.mybatisplus.annotation.TableField;
import com.yupi.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *在线测试接口调用请求
 *把前端用户输入的请求参数和要测试的接口id发给平台后端
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenApiInvokeRequest extends PageRequest implements Serializable {

    /**
     * 测试接口的id
     */
    private Long id;


    /**
     * 用户请求参数（json字符串）
     */
    private String userRequestParams;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}