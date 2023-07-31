package com.xianyu.open_api;
import com.xianyu.xianyuopenapiclientsdk.client.XianYuOpenApiClient;
import com.xianyu.xianyuopenapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class OpenApiApplicationTests {

    /**
     * 自动注入的SDK客户端
     */
    @Resource
    private XianYuOpenApiClient xianYuOpenApiClient;
    @Test
    void contextLoads() {
        //测试自己创建的open_api SDK的starter
        String result = xianYuOpenApiClient.getNameByGet("xianyu123");
        String result2 = xianYuOpenApiClient.getNameByPost("xianyu555");
        User user = new User();
        user.setUsername("xianyu1");
        String result3 = xianYuOpenApiClient.getUserNameByPost(user);
    }
}
