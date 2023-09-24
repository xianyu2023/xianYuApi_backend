package com.yupi.project.model.vo;
import com.xianyu.xianyucommon.model.vo.OpenApiVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**聚合搜索
 * @author happyxianfish
 */
@Data
public class SearchAllVO implements Serializable {
    private List<OpenApiVO> localOpenApiVOList;

    private List<OpenApiVO> botianOpenApiVOList;
    /**
     * 只查询其中一种数据类型
     */
    private List<?> dataList;
    private static final long serialVersionUID = 1L;
}
