package com.yupi.project.model.dto.search;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import com.yupi.project.common.PageRequest;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchAllRequest extends PageRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;
    /**
     * 类型
     */
    private String type;

    private static final long serialVersionUID = 1L;
}