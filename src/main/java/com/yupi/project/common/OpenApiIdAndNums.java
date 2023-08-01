package com.yupi.project.common;
import lombok.Data;
import java.io.Serializable;

/**
 * @author happyxianfish
 */
@Data
public class OpenApiIdAndNums implements Serializable {

    /**
     * 接口id
     */
    private Long openApiId;
    /**
     * 每个接口的被调用总次数
     */
    private Integer totalNums;
    private static final long serialVersionUID = 1L;
}
