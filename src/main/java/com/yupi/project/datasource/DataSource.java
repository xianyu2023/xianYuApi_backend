package com.yupi.project.datasource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**数据源接口（新接入的数据源必须实现它）
 * 定制数据源规范
 * @param <T>
 */
public interface DataSource<T> {
    /**
     * 支持关键词搜索、分页搜索功能
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText,long pageNum, long pageSize);
}
