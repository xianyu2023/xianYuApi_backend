//package com.yupi.project.crawler;
//
//import cn.hutool.extra.spring.SpringUtil;
//import cn.hutool.http.HttpRequest;
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.xianyu.xianyucommon.model.entity.OpenApi;
//import com.yupi.project.common.ErrorCode;
//import com.yupi.project.exception.BusinessException;
//import com.yupi.project.model.entity.Picture;
//import com.yupi.project.model.entity.Post;
//import com.yupi.project.service.OpenApiService;
//import com.yupi.project.service.PostService;
//import org.aspectj.util.GenericSignature;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.DataNode;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import javax.annotation.Resource;
//import javax.swing.plaf.synth.SynthOptionPaneUI;
//import java.io.IOException;
//import java.util.*;
//
///**
// * 爬虫测试
// */
//@SpringBootTest
//public class CrawlerTest {
//
//    @Resource
//    private PostService postService;
//
//    @Resource
//    private OpenApiService openApiService;
//
//    /**
//     * 找不到接口：jsoup爬取html页面【实时爬取】
//     * @throws IOException
//     */
//    @Test
//    void testFetchPicture() throws IOException {
//        int current = 1;
//        String url= "https://cn.bing.com/images/search?q=小黑子&first="+current;
//        Document doc = Jsoup.connect(url).get();
////        System.out.println(doc);
//        Elements elements = doc.select(".iuscp.isv");
//        List<Picture> pictures = new ArrayList<>();
//        for (Element element : elements) {
//            //取图片地址（murl）
//            String m = element.select(".iusc").get(0).attr("m");
//            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
////            System.out.println(map);
//            if (map==null) {
//                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//            }
//            String murl = (String)map.get("murl");
//            //取标题
//            String title = element.select(".inflnk").get(0).attr("aria-label");
//            System.out.println(murl);
//            System.out.println(title);
//            Picture picture = new Picture();
//            picture.setTitle(title);
//            picture.setUrl(murl);
//            pictures.add(picture);
////            System.out.println(element);
//        }
//        System.out.println(pictures);
////        Elements newsHeadlines = doc.select("#mp-itn b a");
////        for (Element headline : newsHeadlines) {
////        }
//    }
//
//
//    /**
//     * 已找到接口：定时爬取/只爬取一次【离线爬取】
//     *
//     * 找得到接口的文章网站
//     * 爬虫：直接给接口发请求来抓取数据
//     *
//     * todo 数据入库后，为啥帖子的id是那种值呢，我明明没有设置id啊
//     * todo 帖子表post里，帖子的内容content会不会太多呢
//     */
//    @Test
//    void testFetchPassage() {
//        //1.获取数据
//        //这里的json：要发送的请求参数
//        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
//        String url = "https://www.code-nav.cn/api/post/search/page/vo";
//        //.post发送POST请求     .body发送给后端的内容/主体    .excute真正执行发送请求     .body获取响应的内容
//        String result2 = HttpRequest
//                .post(url)
//                .body(json)
//                .execute()
//                .body();
////        System.out.println(result2);
//        //2.处理数据（例：json转对象）
//        //两种方式：方式一（根据返回值result2，专门创建一个对象来接收）。方式二（统一用一个Map<String,Object>对象来接收）
//        Map<String, Object> map = JSONUtil.toBean(result2, Map.class);
//        System.out.println(map);
//        if (map == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        Integer code = (Integer) map.get("code");
//        if (code != 0) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        //获取的data对象可以转为JSONObject
//        JSONObject data = (JSONObject) map.get("data");
//        if (data == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        //获取的records是数组，可以转为JSONArray
//        JSONArray records = (JSONArray) data.get("records");
//        if (records == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//        List<Post> postList = new ArrayList<>();
//        //JSONArray里取出的单个值竟然也是Object对象
//        for (Object record : records) {
//            //Object对象=>JSONObject对象
//            JSONObject tempRecord = (JSONObject) record;
//            if (tempRecord == null) {
//                continue;
//            }
//            //将爬到的数据封装到Post对象中
//            Post post = new Post();
//            post.setTitle(tempRecord.getStr("title"));
//            post.setContent(tempRecord.getStr("content"));
//            JSONArray tags =(JSONArray) tempRecord.get("tags");
//            //tags是JSONArray，而setTags里tags是json字符串String。所以JSONArray=>java对象（List<String>）=>json字符串
//            if (tags == null) {
//                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//            }
//            List<String> tagsList = tags.toList(String.class);
//            post.setTags(JSONUtil.toJsonStr(tagsList));
//            post.setUserId(1686575387410194434L);
//            //将有数据的Post添加到List中
//            postList.add(post);
//        }
//        //根据postList中的content格式发现。
//        // 爬虫能抓到数据是一回事，抓取的数据能否使用、有没有意义是另外一回事
//        // （比如抓取的图片有防盗链，就使用不了。抓取的文章内容格式和我们想要的有些差别，还需要我们进一步去对数据内容的格式进行进一步处理）
//        System.out.println(postList);
//        //数据入库
//        boolean b = postService.saveBatch(postList, 1000);
//        Assertions.assertTrue(b);
//    }
//
//    /**
//     * 爬取搏天api（学习用）
//     * 有两种页面，首先是首页，有接口（需要address。https://api.btstu.cn/ajax.php?act=select）。其次是接口详情页，无接口（观察测试得。https://api.btstu.cn/doc/+address+.php）
//     *
//     * 进一步查看页面知，address也包含在页面的href里
//     */
//    @Test
//    void testFetchBoTianAPI() throws IOException {
//        //1.获取数据
//        //首先是首页，有接口（https://api.btstu.cn/ajax.php?act=select）。需要address。
//        String indexUrl = "https://api.btstu.cn/ajax.php?act=select";
//        String body = HttpRequest.get(indexUrl)
//                .execute()
//                .body();
//        String a = "{\"records\":";
//        String b = "}";
//        String s = a + body + b;
////        System.out.println(s);
//        Map<String,Object> map = JSONUtil.toBean(s, Map.class);
//        JSONArray records = (JSONArray)map.get("records");
//        ArrayList<OpenApi> apiList = new ArrayList<>();
//        for (Object record : records) {
////            System.out.println(record);
//            JSONObject tempRecord = (JSONObject) record;
//            if (tempRecord == null) {
//                continue;
//            }
//            String address = tempRecord.getStr("address");
////            System.out.println(address);
//            String theUrl = "https://api.btstu.cn/doc/"+address+".php";
//            //没有接口，只好从html页面取
//            Document doc = Jsoup.connect(theUrl).get();
//            System.out.println(doc);
//            Elements divs = doc.select(".container.header").select("div");
//            String name = divs.get(1).text();
//            String description = divs.get(2).text();
//            Elements ps = doc.select(".apiInfo").select("p");
//            String method = ps.get(0).getElementsByTag("span").text();
//            String url = ps.get(1).getElementsByTag("span").text();
//            //返回格式
//            String responseHeader = ps.get(2).getElementsByTag("span").text();
//            //用List<List<String>>来存表格数据
//            Elements tbodys = doc.select("table.api-table").select("tbody");
//            //请求参数表格
//            Element requestTable = tbodys.get(0);
//            Elements trs = requestTable.getElementsByTag("tr");
//            List<List<String>> lists = new ArrayList<>();
//            for (Element tr : trs) {
//                Elements ths = tr.getElementsByTag("th");
//                Elements tds = tr.getElementsByTag("td");
//                List<String> list = null;
//                if (ths.size() != 0) {
//                   list = Arrays.asList(ths.get(1).text(), ths.get(2).text(), ths.get(3).text(), ths.get(4).text());
//                }
//                if (tds.size() != 0) {
//                    list = Arrays.asList(tds.get(1).text(), tds.get(2).text(), tds.get(3).text(), tds.get(4).text());
//                }
//                lists.add(list);
//            }
//            System.out.println(lists);
//            //返回参数表格
//            Element responseTable = tbodys.get(1);
//            Elements trs2 = responseTable.getElementsByTag("tr");
//            List<List<String>> lists2 = new ArrayList<>();
//            for (Element tr : trs2) {
//                Elements ths = tr.getElementsByTag("th");
//                Elements tds = tr.getElementsByTag("td");
//                List<String> list = null;
//                if (ths.size() != 0) {
//                    list = Arrays.asList(ths.get(1).text(), ths.get(2).text(), ths.get(3).text());
//                }
//                if (tds.size() != 0) {
//                    list = Arrays.asList(tds.get(1).text(), tds.get(2).text(), tds.get(3).text());
//                }
//                lists2.add(list);
//            }
//            System.out.println(lists2);
//            //2.处理数据，入库
//            OpenApi openApi = new OpenApi();
//            openApi.setName(name);
//            openApi.setDescription(description);
//            openApi.setMethod(method);
//            openApi.setUrl(url);
//            String responseAll = "{\"key1\":"+responseHeader+",\"key2\":"+JSONUtil.toJsonStr(lists2)+"}";
//            openApi.setResponseHeader(responseAll);
//            openApi.setRequestParams(JSONUtil.toJsonStr(lists));
//            openApi.setRequestHeader("{\"Content-Type\": \"application/json\"}");
//            openApi.setPath(url.split(".cn")[1]);
//            openApi.setOrigin("botian");
//            apiList.add(openApi);
//        }
//        System.out.println(apiList.size());
//        if (apiList.size() > 100) {
//            return;
//        }
//        boolean result= openApiService.saveBatch(apiList);
//        System.out.println(result);
//    }
//
//    @Test
//    void testSplit() {
//        String url = "https://api.btstu.cn/yan/api.php";
//        String[] split = url.split(".cn");
//        String s1 = split[0];
//        String s2 = split[1];
//        System.out.println(s1);
//        System.out.println(s2);
//    }
//}
