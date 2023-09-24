package com.yupi.project.canal;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.yupi.project.esdao.OpenApiEsDao;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;
import javax.annotation.Resource;


/**
 * canal的监控处理器
 * @author happyxianfish
 */

@Component
@CanalTable(value = "open_api")
public class CanalListenHandler implements EntryHandler<OpenApi> {

    @Resource
    private OpenApiEsDao openApiEsDao;


    @Override
    public void insert(OpenApi openAPI) {
        System.err.println("添加：" + openAPI);
        //把mysql中新增的接口同步到ES
        OpenApiEsDTO openApiEsDTO = OpenApiEsDTO.objToDto(openAPI);
        openApiEsDao.save(openApiEsDTO);
        System.out.println("添加接口同步成功");
    }

    @Override
    public void update(OpenApi before, OpenApi after) {
        System.err.println("改前：" + before);
        System.err.println("改后：" + after);
        OpenApiEsDTO openApiEsDTO = OpenApiEsDTO.objToDto(after);
        if (after.getIsDeleted() == 1) {
            //删除
            delete(after);//异步的
            return;
        }
        //把mysql中修改后的接口同步到ES
        openApiEsDao.save(openApiEsDTO);
        System.out.println("更新接口同步成功");
    }
    @Override
    public void delete(OpenApi openApi) {
        //本业务的删除并非是直接物理删除delete。而是先修改字段isDeleted为1，然后为了不让逻辑删除的数据占用ES空间，之后还是得物理删除ES中isDeleted=1的数据
        OpenApiEsDTO openApiEsDTO = OpenApiEsDTO.objToDto(openApi);
        openApiEsDao.delete(openApiEsDTO);
        System.err.println("删除：" + openApi);
    }
}
