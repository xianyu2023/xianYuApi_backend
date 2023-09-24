package com.yupi.project.crawler.once;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.yupi.project.service.OpenApiService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//开启@Component注释后，每次启动 springboot 项目时会执行一次 run 方法
//@Component
public class getBoTianApi implements CommandLineRunner {

    @Resource
    private OpenApiService openApiService;
    /**
     * 爬取搏天api（学习用）
     * 有两种页面，首先是首页，有接口（需要address。https://api.btstu.cn/ajax.php?act=select）。其次是接口详情页，无接口（观察测试得。https://api.btstu.cn/doc/+address+.php）
     *
     * 进一步查看页面知，address也包含在页面的href里
     */
    @Override
    public void run(String... args) throws IOException {
        //1.获取数据
        //首先是首页，有接口（https://api.btstu.cn/ajax.php?act=select）。需要address。
        String indexUrl = "https://api.btstu.cn/ajax.php?act=select";
        String body = HttpRequest.get(indexUrl)
                .execute()
                .body();
        String a = "{\"records\":";
        String b = "}";
        String s = a + body + b;
//        System.out.println(s);
        Map<String,Object> map = JSONUtil.toBean(s, Map.class);
        JSONArray records = (JSONArray)map.get("records");
        ArrayList<OpenApi> apiList = new ArrayList<>();
        for (Object record : records) {
//            System.out.println(record);
            JSONObject tempRecord = (JSONObject) record;
            if (tempRecord == null) {
                continue;
            }
            String address = tempRecord.getStr("address");
//            System.out.println(address);
            String theUrl = "https://api.btstu.cn/doc/"+address+".php";
            //没有接口，只好从html页面取
            Document doc = Jsoup.connect(theUrl).get();
            System.out.println(doc);
            Elements divs = doc.select(".container.header").select("div");
            String name = divs.get(1).text();
            String description = divs.get(2).text();
            Elements ps = doc.select(".apiInfo").select("p");
            String method = ps.get(0).getElementsByTag("span").text();
            String url = ps.get(1).getElementsByTag("span").text();
            //返回格式
            String responseHeader = ps.get(2).getElementsByTag("span").text();
            //用List<List<String>>来存表格数据
            Elements tbodys = doc.select("table.api-table").select("tbody");
            //请求参数表格
            Element requestTable = tbodys.get(0);
            Elements trs = requestTable.getElementsByTag("tr");
            List<List<String>> lists = new ArrayList<>();
            for (Element tr : trs) {
                Elements ths = tr.getElementsByTag("th");
                Elements tds = tr.getElementsByTag("td");
                List<String> list = null;
                if (ths.size() != 0) {
                    list = Arrays.asList(ths.get(1).text(), ths.get(2).text(), ths.get(3).text(), ths.get(4).text());
                }
                if (tds.size() != 0) {
                    list = Arrays.asList(tds.get(1).text(), tds.get(2).text(), tds.get(3).text(), tds.get(4).text());
                }
                lists.add(list);
            }
            System.out.println(lists);
            //返回参数表格
            Element responseTable = tbodys.get(1);
            Elements trs2 = responseTable.getElementsByTag("tr");
            List<List<String>> lists2 = new ArrayList<>();
            for (Element tr : trs2) {
                Elements ths = tr.getElementsByTag("th");
                Elements tds = tr.getElementsByTag("td");
                List<String> list = null;
                if (ths.size() != 0) {
                    list = Arrays.asList(ths.get(1).text(), ths.get(2).text(), ths.get(3).text());
                }
                if (tds.size() != 0) {
                    list = Arrays.asList(tds.get(1).text(), tds.get(2).text(), tds.get(3).text());
                }
                lists2.add(list);
            }
            System.out.println(lists2);
            //2.处理数据，入库
            OpenApi openApi = new OpenApi();
            openApi.setName(name);
            openApi.setDescription(description);
            openApi.setMethod(method);
            openApi.setUrl(url);
            String responseAll = "{\"key1\":"+responseHeader+",\"key2\":"+JSONUtil.toJsonStr(lists2)+"}";
            openApi.setResponseHeader(responseAll);
            openApi.setRequestParams(JSONUtil.toJsonStr(lists));
            openApi.setRequestHeader("{\"Content-Type\": \"application/json\"}");
            openApi.setPath(url.split(".cn")[1]);
            openApi.setOrigin("botian");
            apiList.add(openApi);
        }
        System.out.println(apiList.size());
        if (apiList.size() > 100) {
            return;
        }
        boolean result= openApiService.saveBatch(apiList);
        System.out.println(result);
    }
}
