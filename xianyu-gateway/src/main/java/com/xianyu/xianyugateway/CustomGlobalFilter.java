package com.xianyu.xianyugateway;

import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.service.InnerOpenApiService;
import com.xianyu.xianyucommon.service.InnerUserOpenApiService;
import com.xianyu.xianyucommon.service.InnerUserService;
import com.xianyu.xianyuopenapiclientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义全局过滤器
 *
 * @author happyxianfish
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    @DubboReference
    private InnerOpenApiService innerOpenApiService;

    @DubboReference
    private InnerUserOpenApiService innerUserOpenApiService;
    @DubboReference
    private InnerUserService innerUserService;

    /**
     *调用的接口的服务器地址（这里应该是从数据库获取该接口的服务器地址的。OpenApi表应该增加个host主机字段）
     * todo 这里为了先把整个流程跑通，先暂时写死
     */
    private static final String OPEN_API_HOST = "http://localhost:8123";
    /**
     * 白名单
     */
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    /**
     * 1.用户发送请求到 API 网关
     *
     * @param exchange 路由交换机exchange：能获取所有请求信息、响应信息、请求体、响应体
     * @param chain    chain：责任链模式。所有过滤器从上到下执行，串成一个链条。
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //2. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpMethod method = request.getMethod();
        RequestPath path = request.getPath();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求方法：" + method);
        log.info("请求路径：" + path);
        log.info("请求参数：" + request.getQueryParams());
        log.info("请求来源地址：" + request.getRemoteAddress());
        //3. 访问控制（--黑白名单）最好用白名单
        String sourceAddress = request.getRemoteAddress().getHostString();
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            return handleNoAuth(response);
        }
        //4. 用户鉴权（判断 accessKey, secretKey 是否合法）
        //API签名认证
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String body = headers.getFirst("body");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        //todo实际情况应该是从数据库中查该ak是否已分配给用户（这个accessKey，看它是否存在以及拥有它的用户是否正常）
        User invokeUser = null;
        try {
             invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error");
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        //todo 随机数需要额外到后端存储。弄个hashmap或者redis来存，比较麻烦。这里先对随机数进行简单校验来暂时代替
        //将随机数字符串nonce转为整数。获取的随机数nonce可能为空
        if (Long.parseLong(nonce) >= 100000) {
            return handleNoAuth(response);
        }
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long FIVE_MINUTES = 5 * 60L;
        //todo 获取的时间戳（发http请求调用接口时设置的系统时间）和当前时间不超过5分钟。比较麻烦，还需写个时间解析之类的。
        if ((currentTime - Long.parseLong(timestamp)) > 5 * 60) {
            return handleNoAuth(response);
        }
        //todo body可校验，可不校验
        //todo实际情况中，正确密钥secretKey（123456）应该是根据accessKey从数据库中查出来的
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.genSign(body, secretKey);
        //以同样的签名算法，将相同的参数body、正确的密钥secretKey生成一个正确的签名
        //（这里为了方便，把hashmap换成了body，毕竟用hashmap麻烦，还得重新拼个hashmap）
        if (!sign.equals(serverSign)) {
            return handleNoAuth(response);
        }
        //5. 请求的模拟接口是否存在
        //todo从数据库中查询请求的接口是否存在以及请求方法是否匹配（这种业务层面的 用户传入的请求参数 和 接口要求的 是否匹配的校验最好放在业务层去校验，而不是放在全局过滤器这里）
        //根据接口的完整url和method去查询
        OpenApi invokeOpenApi = null;
        String url = OPEN_API_HOST + path;
        try {
            invokeOpenApi = innerOpenApiService.getInvokeOpenApi(url, method.toString());
        } catch (Exception e) {
            log.error("getInvokeOpenApi error");
        }
        //try内，发生异常后，异常后的代码不会执行，所以下面的代码得提取出来
        if (invokeOpenApi == null) {
            return handleInvokeError(response);
        }
        //因为网关项目没引入 MyBatis 等操作数据库的类库，如果该操作较为复杂，可以由xianyuApi-backend 增删改查项目来提供接口，然后发http请求或者用rhp来直接调用该接口，不需要重复写逻辑。
        //6. 请求转发，调用模拟接口（先处理这个，把整个流程跑通）
//        Mono<Void> filter = chain.filter(exchange);
//        return filter;
        return handleResponse(exchange,chain,invokeUser,invokeOpenApi);
    }


    /**
     * 获取过滤器优先级
     * @return
     */
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 处理无权限
     *
     * @param response
     * @return
     */
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        //设置响应状态码，然后返回直接拦截掉
        response.setStatusCode(HttpStatus.FORBIDDEN);
        //setComplete设置响应完成/结束
        return response.setComplete();
    }

    /**
     * 处理调用失败
     *
     * @param response
     * @return
     */
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        //设置响应状态码，然后返回直接拦截掉
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        //setComplete设置响应完成/结束
        return response.setComplete();
    }

    /**
     * 处理响应
     * 【请求转发，调用模拟接口+打印响应日志+统计调用次数】
     * @param exchange
     * @param chain
     * @param invokeUser 调用接口的用户
     * @param invokeOpenApi 调用的接口
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,User invokeUser,OpenApi invokeOpenApi) {
        try {
            //原始响应
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if(statusCode == HttpStatus.OK){
                // 装饰，增强原始响应的能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行（即响应的时候才会执行writeWith）
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                try {
                                    //8. 调用成功，次数 + 1
                                    // todo调用次数+1 invokeCount
                                    innerUserOpenApiService.invokeCount(invokeUser.getId(), invokeOpenApi.getId());
                                } catch (Exception e) {
                                    log.error("invokeCount error");
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                //7. 打印响应日志
                                log.info("响应结果："+data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            //9. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //statusCode是Ok。设置 response 对象为装饰过的。请求转发，调用模拟接口
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            //statusCode不是Ok。降级处理返回数据
            return chain.filter(exchange);
        }catch (Exception e){
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }
}