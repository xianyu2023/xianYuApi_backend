package com.yupi.project.canal;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.ByteString;
import com.yupi.project.esdao.OpenApiEsDao;
import com.yupi.project.model.dto.openApiRequest.OpenApiEsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
/**
 * 有canal的监控处理器，就可以不用开启本类
 * 本类没有用canal和springbooy的整合starter
 */
@Slf4j
//@Component
public class CanalDemo implements CommandLineRunner {

    @Resource
    private OpenApiEsDao openApiEsDao;

    @Override
    public void run(String... args) throws Exception {
        //（一）canal从db获取变更数据
        //1.获取canal连接对象
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        //指定canal每次从日志抓取的信息数量
        int batchSize = 1000;
        try {
            //2.获取连接
            connector.connect();
            //3.指定要监控的数据库及表
            connector.subscribe("api.open_api");
            //?
            connector.rollback();
            //canal不断从数据库binlog日志抓取信息
            while (true) {
                //4.获取message（message：canal每次从日志抓取的信息。一个message可能包含多个sql执行的结果）【一个message：多个sql】
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                //从message里获取多个sql的执行结果（entries）【entry：一条sql的执行结果。一个sql或者一个entry的结果可能有多行数据变动】
                List<CanalEntry.Entry> entries = message.getEntries();
                //多条sql执行结果的数量
                int size = entries.size();
                //message没有数据时
                if (batchId == -1 || size == 0) {
                    System.out.println("没有数据，休息一会");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    //message有数据
                    System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    //将多条sql的执行结果entries（内含mysql变更数据）传入方法中
                    doEntries(entries);
                }
                // 提交确认?
                connector.ack(batchId);
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }
        } finally {
            connector.disconnect();
        }
    }
    private void doEntries(List<CanalEntry.Entry> entrys) {
        //遍历取出一条sql的执行结果
        for (CanalEntry.Entry entry : entrys) {
            //获取表名（该sql语句影响的表）
            String tableName = entry.getHeader().getTableName();
            //获取entry的类型
            CanalEntry.EntryType entryType = entry.getEntryType();
            if (entryType == CanalEntry.EntryType.TRANSACTIONBEGIN || entryType == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            //  判断 entryType 是否为 ROWDATA
            if (CanalEntry.EntryType.ROWDATA.equals(entryType)) {
                //获取该sql执行结果的具体数据【storeValue：被序列化的数据（二进制数据），无法直接使用】
                ByteString storeValue = entry.getStoreValue();
                CanalEntry.RowChange rowChage = null;
                try {
                    //需要反序列化【rowChage：可用的变更数据】
                    rowChage = CanalEntry.RowChange.parseFrom(storeValue);
                } catch (Exception e) {
                    throw new RuntimeException("一个entry的storeValue反序列化失败" + entry.toString(), e);
                }
                //获取变更数据的事件类型
                CanalEntry.EventType eventType = rowChage.getEventType();
                //获取变更数据的行数据列表（意义：该条sql导致变动的多条行数据）
                List<CanalEntry.RowData> rowDatasList = rowChage.getRowDatasList();
                //遍历取出一条变化的行数据
                doOneSqlChange(tableName, eventType, rowDatasList);
            }
        }
    }

    private void doOneSqlChange(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatasList) {
        for (CanalEntry.RowData rowData : rowDatasList) {
            System.out.println("表名：" + tableName + ";   操作类型：" + eventType);
            //变化前的字段列表（每个字段的名称+值，都有）
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            //变化后的字段列表（每个字段的名称+值，都有）
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            //本项目mysql暂没有物理删除，只有逻辑删除
            if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                System.out.println("改动后");
                printColumn(afterColumnsList);
                int isDeleted = Integer.parseInt(afterColumnsList.get(12).getValue());
                if (isDeleted == 1) {
                    //mysql逻辑删除。删除es中的数据
                    openApiEsDao.deleteById(Long.parseLong(afterColumnsList.get(0).getValue()));
                } else {
                    //修改、增加
                    OpenApiEsDTO openApiEsDTO = columnsToOpenApiEsDto(afterColumnsList);
                    openApiEsDao.save(openApiEsDTO);
                }
            }
        }
    }

    private void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

    private OpenApiEsDTO columnsToOpenApiEsDto(List<CanalEntry.Column> columns){
        OpenApiEsDTO openApiEsDTO = new OpenApiEsDTO();
        openApiEsDTO.setId(Long.parseLong(columns.get(0).getValue()));
        openApiEsDTO.setName(columns.get(1).getValue());
        openApiEsDTO.setDescription(columns.get(2).getValue());
        openApiEsDTO.setUrl(columns.get(3).getValue());
        openApiEsDTO.setMethod(columns.get(4).getValue());
        openApiEsDTO.setRequestParams(columns.get(5).getValue());
        openApiEsDTO.setRequestHeader(columns.get(6).getValue());
        openApiEsDTO.setResponseHeader(columns.get(7).getValue());
        openApiEsDTO.setStatus(Integer.parseInt(columns.get(8).getValue()));
        openApiEsDTO.setUserId(Long.parseLong(columns.get(9).getValue()));
        try {
            openApiEsDTO.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(columns.get(10).getValue()));
            openApiEsDTO.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(columns.get(11).getValue()));
        } catch (ParseException e) {
            log.error("时间格式转换异常");
        }
        openApiEsDTO.setIsDeleted(Integer.parseInt(columns.get(12).getValue()));
        openApiEsDTO.setPath(columns.get(13).getValue());
        openApiEsDTO.setOrigin(columns.get(14).getValue());
        return openApiEsDTO;
    }

}
