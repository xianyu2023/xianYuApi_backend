package com.yupi.project.service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class UserOpenApiServiceTest {
    @Resource
    private UserOpenApiService userOpenApiService;

    @Test
    void invokeCount() {
        boolean result = userOpenApiService.invokeCount(1L, 1L);
        System.out.println(result);
        Assertions.assertTrue(result);
    }
}