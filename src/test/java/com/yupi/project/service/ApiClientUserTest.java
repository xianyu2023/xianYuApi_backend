package com.yupi.project.service;

import com.xianyu.apiClientUser.client.XianYuOpenApiClientUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 客户端SDK-用户版
 */
@SpringBootTest
public class ApiClientUserTest {

    @Resource
    private XianYuOpenApiClientUser xianYuOpenApiClient;

    @Test
    void test() {
        /**
         * {"username":"username123","type":"string"} json字符串形式
         * 类型string，属性名username，属性值username123
         */
        String params = "{\"username\":\"username123\",\"type\":\"string\"}";
        xianYuOpenApiClient.getApiService(9L,params);
    }
}
