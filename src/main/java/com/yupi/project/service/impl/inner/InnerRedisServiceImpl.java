package com.yupi.project.service.impl.inner;
import com.xianyu.xianyucommon.service.InnerRedisService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@DubboService
public class InnerRedisServiceImpl implements InnerRedisService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public boolean writeCache(String key) {
        //1.redis缓存数据结构：string
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //2.redis设计缓存key
        String redisKey = String.format("xianyu-gateway:apiSign:nonce:%s",key);
        //3.todo 预估内存 +设计过期时间+ todo 自定义内存淘汰策略
        //写缓存【需要：随机数的过期时间稍大于时间戳。即使随机数过期了，用户重放可以通过随机数，但时间戳会到期，无法访问。
        // 除非黑客可以修改拦截的重放里的时间戳为最新时间，然后等缓存过期后发送修改的重放请求】
        try {
            //缓存可能写入失败
            return Boolean.TRUE.equals(valueOperations.setIfAbsent(redisKey, "", 11, TimeUnit.MINUTES));
        } catch (Exception e) {
            return false;
        }
    }
}
