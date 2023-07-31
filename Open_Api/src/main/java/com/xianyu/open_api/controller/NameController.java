package com.xianyu.open_api.controller;
import com.xianyu.xianyuopenapiclientsdk.model.User;
import com.xianyu.xianyuopenapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**名称API
 * 模拟的三个API接口
 * @author happyxianfish
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/")
    public String getNameByGet(String name) {
        return "GET 你的名字是"+name;
    }

    /**
     * 后端接收参数的方式是url params queryString
     * @param name
     * @return
     */
    @PostMapping("/url")
    public String getNameByPost(@RequestParam String name) {
        return "Post 你的名字是"+name;
    }

    /**
     * json传参。restful风格
     * @param user
     * @return
     */
    @PostMapping("/json")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
//        //API签名认证
//        String accessKey = request.getHeader("accessKey");
//        String body = request.getHeader("body");
//        String nonce = request.getHeader("nonce");
//        String timestamp = request.getHeader("timestamp");
//        String sign = request.getHeader("sign");
//        //todo实际情况应该是从数据库中查该ak是否已分配给用户（这个accessKey，看它是否存在以及拥有它的用户是否正常）
//        if (!accessKey.equals("xianyu")) {
//            //偷懒
//            throw new RuntimeException("没权限");
//        }
//        //todo 随机数需要额外到后端存储。弄个hashmap或者redis来存，比较麻烦。这里先对随机数进行简单校验来暂时代替
//        //将随机数字符串nonce转为整数
//        if (Long.parseLong(nonce) >= 100000) {
//            throw new RuntimeException("没权限");
//        }
////        //todo 该时间戳和当前时间不要差得太多。比较麻烦，还需写个时间解析之类的。
////        if (timestamp) {
////            throw new RuntimeException("没权限");
////        }
//        //todo body可校验，可不校验
//
//        //todo实际情况中，正确密钥secretKey（123456）应该是根据accessKey从数据库中查出来的
//        String serverSign = SignUtils.genSign(body, "123456");
//        //以同样的签名算法，将相同的参数body、正确的密钥secretKey生成一个正确的签名
//        //（这里为了方便，把hashmap换成了body，毕竟用hashmap麻烦，还得重新拼个hashmap）
//        if (!sign.equals(serverSign)) {
//            throw new RuntimeException("没权限");
//        }
        //上面权限校验结束后，进行调用，调用成功
        String result =  "Post 用户名字是"+user.getUsername();
        // todo调用次数+1 invokeCount
        return result;
    }
}
