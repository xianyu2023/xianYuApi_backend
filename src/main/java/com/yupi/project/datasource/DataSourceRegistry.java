package com.yupi.project.datasource;
import com.yupi.project.enums.SearchTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源注册器
 * 注册器模式（本质也是单例模式）：设计模式的扩展
 */
@Component
public class DataSourceRegistry {

    @Resource
    private LocalApiDataSource localApiDataSource;

    @Resource
    private BoTianApiDataSource boTianApiDataSource;

    Map<String, DataSource<?>> dataSourceMap;
    @PostConstruct
    public void doInit() {
            dataSourceMap = new HashMap() {{
                //注册数据源
            put(SearchTypeEnum.LOCAL.getValue(),localApiDataSource);
            put(SearchTypeEnum.BOTIAN.getValue(),boTianApiDataSource);
        }};
    }

    public DataSource<?> getDataSourceByType(String type) {
        if (dataSourceMap==null) {
            return null;
        }
        return dataSourceMap.get(type);
    }

    //    /**
//     * 直接这样,dataSourceMap可能为空，在这里创建map的初始化有问题。
//     * 解决：使用postConstruct注解构建一个初始化init方法。
//     * 也可以使用单例模式，
//     * 可以使用反射（有个问题：不清楚创建的组件的情况）
//     * 可以使用静态代码块。只要能初始化一次
//     */
//    Map<String, DataSource<?>> dataSourceMap = new HashMap() {{
//        put(SearchTypeEnum.POST.getValue(),postDataSource);
//        put(SearchTypeEnum.PICTURE.getValue(),pictureDataSource);
//        put(SearchTypeEnum.USER.getValue(),userDataSource);
//    }};

}
