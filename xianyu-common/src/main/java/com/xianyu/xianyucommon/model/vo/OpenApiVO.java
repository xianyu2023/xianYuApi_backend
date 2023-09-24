package com.xianyu.xianyucommon.model.vo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.reflect.TypeToken;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 接口信息封装视图
 *
 * @author happyxianfish
 * @TableName open_api
 */
@Data
public class OpenApiVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;


    /**
     * 请求类型
     */
    private String method;

    /**
     * 请求参数
     * 示例：
     * [
     * {"name":"username","type":"string"}
     * ]
     */
    private String requestParams;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
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
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * API请求路径
     */
    private String path;

    /**
     * 每个接口的被调用总次数
     */
    private Integer totalNums;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param openApiVO
     * @return
     */
    public static OpenApi voToObj(OpenApiVO openApiVO) {
        if (openApiVO == null) {
            return null;
        }
        OpenApi openApi = new OpenApi();
        BeanUtils.copyProperties(openApiVO, openApi);
        return openApi;
    }

    /**
     * 对象转包装类
     *
     * @param openApi
     * @return
     */
    public static OpenApiVO objToVo(OpenApi openApi) {
        if (openApi == null) {
            return null;
        }
        OpenApiVO openApiVO = new OpenApiVO();
        BeanUtils.copyProperties(openApi, openApiVO);;
        return openApiVO;
    }
}