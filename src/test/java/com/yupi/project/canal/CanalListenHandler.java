package com.yupi.project.canal;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;


/**
 * canal的监控处理器
 */

@Component
@CanalTable(value = "open_api")
public class CanalListenHandler implements EntryHandler<OpenApi> {

    @Override
    public void insert(OpenApi openAPI) {
        System.err.println("添加：" + openAPI);
    }

    @Override
    public void update(OpenApi before, OpenApi after) {
        System.err.println("改前：" + before);
        System.err.println("改后：" + after);

    }
    @Override
    public void delete(OpenApi openApi) {
        System.err.println("删除：" + openApi);
    }
}
