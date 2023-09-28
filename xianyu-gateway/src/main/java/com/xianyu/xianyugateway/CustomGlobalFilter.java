package com.xianyu.xianyugateway;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xianyu.apiClientAdmin.utils.SignUtils;
import com.xianyu.xianyucommon.model.entity.OpenApi;
import com.xianyu.xianyucommon.model.entity.UserOpenApi;
import com.xianyu.xianyucommon.service.InnerOpenApiService;
import com.xianyu.xianyucommon.service.InnerRedisService;
import org.apache.commons.lang3.StringUtils;
import com.xianyu.xianyucommon.model.entity.User;
import com.xianyu.xianyucommon.service.InnerUserOpenApiService;
import com.xianyu.xianyucommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
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
import reactor.netty.ByteBufFlux;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 自定义全局过滤器
 *
 * @author xianyu
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

    @DubboReference
    private InnerRedisService innerRedisService;

    /**
     * todo 白名单
     * "127.0.0.1","62.234.28.188"
     * 实测0.0.0.0不包含127.0.0.1
     */
//    private static final List<String> IP_WHITE_LIST = Arrays.asList("0.0.0.0");

    /**
     * 1.用户发送请求到 API 网关
     *
     * 因为网关项目没引入MyBatis等操作数据库的类库，如果该操作较为复杂，可由xianyuApi-backend来提供增删改查接口。
     * 网关项目发送http请求或者rpc远程调用接口，不需要重复写逻辑。
     *
     * @param exchange 路由交换机exchange：能获取所有请求信息、响应信息、请求体、响应体
     * @param chain    chain：责任链模式。所有过滤器从上到下执行，串成一个链条。
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //todo 并发
        //2. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpMethod method = request.getMethod();
        RequestPath path = request.getPath();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求方法：" + method);
        log.info("请求路径：" + path);
        log.info("url请求参数：" + request.getQueryParams());
        String sourceAddress = request.getRemoteAddress().getHostString();
        log.info("请求来源地址："+sourceAddress);
        //3. 访问控制（--黑白名单）最好用白名单
//        if (!IP_WHITE_LIST.contains(sourceAddress)) {
//            return handleNoAuth(response);
//        }
        HttpHeaders headers = request.getHeaders();
        //流量染色（给请求添加特定的请求头标识）
        //存储header中
        ServerHttpRequest serverHttpRequest = exchange.getRequest().mutate().headers(httpHeaders -> {
            httpHeaders.add("gateway", "xianyu_gateway");
        }).build();
       //重置请求
        exchange.mutate().request(serverHttpRequest);
        //4. 用户鉴权（判断 accessKey, secretKey 是否合法）
        //API签名认证
        String accessKey = headers.getFirst("accessKey");
        //todo 注意：请求头里的取的body好像去掉了" {"name":"username","type":"string"} "引号内两侧的空格，所以前端传入的{"":"","":""}两侧不要加空格，否则sign会校验失败
        //todo body内中文乱码，body变化导致之后用于生成签名时产生一个错误的签名
        String body = headers.getFirst("body");//请求参数的校验最好不放在网关，放到模拟接口的业务层
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String apiId = headers.getFirst("id");
        //用户的校验：accessKey是否存在、用户是否正常、查询的正确密钥是否存在    todo在网关校验
        if (StringUtils.isAnyBlank(accessKey,nonce,timestamp,sign,apiId)) {
            return handleNoAuth(response);
        }
        User invokeUser = null;
        try {
            //网关不能因异常而停止，需捕获
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error");
        }
        //try内发生异常后，异常点后的代码不会执行，所以下面的代码得提取出来
        if (invokeUser == null||invokeUser.getStatus() != 0 || invokeUser.getSecretKey() == null) {
            return handleNoAuth(response);
        }
        //校验时间戳（签名认证多少s内有效）
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long TEN_MINUTES = 10 * 60L;
        //todo当前时间-平台后端给网关发送http请求的时间,不超过10分钟【可能要时间解析之类的】
        if ((currentTime - Long.parseLong(timestamp)) > TEN_MINUTES) {
            return handleNoAuth(response);
        }
        //校验随机数【防重放】，配合时间戳使用
        //todo key小概率相同。每正常调用一次接口，都会存一个key到redis中，如果用户并发量很大（几百万），redis内存是否足够？？？
        //用户每次调用接口时，平台后端会随机生成一个随机数设置到请求头。我们把（用户id+随机数）存放到redis中，
        // 每次签名认证时查询redis看这个key是否存在，如果存在，无法访问。如果key不存在，则可以访问并把（用户id+随机数）保存到redis中
        //redis分布式缓存（spring data redis实现redis）
        String key = invokeUser.getId()+":" + nonce;
        if (!innerRedisService.writeCache(key)) {
            //有可能因为redis连接的问题导致用户写缓存失败，然后用户请求被黑客拦截重放，黑客访问时刚好redis可连接，写入成功，但接口最多调用一次（只要redis随机数存在）。
            //黑客可等随机数11分钟失效后再去调用+要求时间戳可修改为最新。
            //或者黑客可以修改重放的随机数，在时间戳内可调用；时间戳也修改，黑客可随时访问。
            //总结：并非绝对安全
            return handleNoAuth(response);
        }
//        todo随机数需要额外存储到后端。弄个hashmap或者redis来存，比较麻烦。这里先对随机数进行简单校验来暂时代替
//        if (Long.parseLong(nonce) < 0||Long.parseLong(nonce) > 999999999) {
//            return handleNoAuth(response);
//        }

        //密钥是否正确(sign匹配)
        String secretKey = invokeUser.getSecretKey();
        //以相同的参数body、正确的密钥，再加上相同的签名生成算法，生成一个正确的签名
        //todo 耗费几秒
        String correctSign = SignUtils.genSign(body, secretKey);
        //这里为了方便，把hashmap换成了body，毕竟用hashmap麻烦，还得重新拼个hashmap，这个拼接的拼法根据实际情况决定
        if (!sign.equals(correctSign)) {
            return handleNoAuth(response);
        }
        //用户是否有权限调用该接口、是否还有调用次数 todo在网关校验
        UserOpenApi userOpenApi = innerUserOpenApiService.judgeUserRight(invokeUser.getId(), Long.parseLong(apiId));
        if (userOpenApi == null|| userOpenApi.getStatus() == 1||userOpenApi.getLeftNum() <= 0) {
            return handleNoAuth(response);
        }
        //判断调用的接口是否是第三方的接口
        OpenApi openApi = innerOpenApiService.getInvokeOpenApiById(apiId);
        if (!"local".equals(openApi.getOrigin())) {
            return handleInvokeThirdApi(invokeUser,apiId,openApi,body,response);
        }
        //5. 路由请求转发，调用模拟接口
        // 实测：chain.filter执行后，立刻返回，接着执行了接口次数统计（此时接口仍未调用）。filer返回后才开始调用模拟接口。
        //Mono<Void> filter = chain.filter(exchange);//根本原因：异步
        //统计接口调用次数
        //return filter;

        //5.路由请求转发，调用模拟接口。利用响应装饰器处理响应（来统计接口调用次数）
        return handleResponse(exchange,chain,invokeUser,Long.parseLong(apiId));
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
     * 处理调用第三方接口
     * @param openApi
     * @param body
     * @param response
     * @return
     */
    public Mono<Void> handleInvokeThirdApi(User invokeUser,String apiId,OpenApi openApi,String body,ServerHttpResponse response) {
        String result = null;
        HttpResponse httpResponse = null;
        String url = openApi.getUrl();
        String apiMethod = openApi.getMethod();
        if ("GET".equals(apiMethod) || "get".equals(apiMethod) || "get/post".equals(apiMethod)) {
             httpResponse = HttpRequest.get(url)
                    .header("Accept-Charset", "UTF-8")
                    .body(body)
                    .execute();
            System.out.println(httpResponse.getStatus());//200正常
            result = httpResponse.body();
        }
        if ("POST".equals(apiMethod) || "post".equals(apiMethod)) {
             httpResponse = HttpRequest.post(url)
                    .header("Accept-Charset", "UTF-8")
                    .body(body)
                    .execute();
            System.out.println(httpResponse.getStatus());//200正常
            result = httpResponse.body();
        }
        if (Objects.requireNonNull(httpResponse).getStatus() == 200) {
            try {
                //6. 调用成功，次数 + 1
                // todo调用次数+1 invokeCount
                innerUserOpenApiService.invokeCount(invokeUser.getId(), Long.parseLong(apiId));
            } catch (Exception e) {
                log.error("invokeCount error");
            }
        }
        //设置响应状态码，然后返回直接拦截掉
        response.setStatusCode(HttpStatus.OK);
        //设置响应头信息Content-Type类型
        response.getHeaders().add("Content-Type","application/json");
        //设置返回json数据
        return response.writeAndFlushWith(Flux.just(ByteBufFlux.just(response.bufferFactory().wrap(getWrapData(result)))));
        //直接返回（没有返回数据）
//        return response.setComplete().then();
        //设置返回的数据（非json格式）
//        return response.writeWith(Flux.just(response.bufferFactory().wrap("".getBytes())));
    }

    private byte[] getWrapData(String data) {
        Map<String,String> map = new HashMap<>();
        map.put("code","0");
        map.put("data",data);
        try {
            return new ObjectMapper().writeValueAsString(map).getBytes();
        } catch (JsonProcessingException e) {
            //
        }
        return "".getBytes();
    }


    /**
     * 处理响应
     * 【请求转发，调用模拟接口+打印响应日志+统计调用次数】
     * @param exchange
     * @param chain
     * @param invokeUser 调用接口的用户
     * @param apiId 调用的接口
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain,User invokeUser,Long apiId) {
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
                    // 等调用完模拟接口后才会执行（即响应的时候才会执行writeWith）
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                try {
                                    //6. 调用成功，次数 + 1
                                    // todo调用次数+1 invokeCount
                                    innerUserOpenApiService.invokeCount(invokeUser.getId(), apiId);
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
                            //8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //statusCode是Ok。设置 response 对象为装饰过的。
                //5.路由请求转发，调用模拟接口。利用响应装饰器处理响应（来统计接口调用次数）
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