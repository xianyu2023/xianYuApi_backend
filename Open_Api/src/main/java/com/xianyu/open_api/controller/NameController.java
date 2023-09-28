package com.xianyu.open_api.controller;
import cn.hutool.json.JSONUtil;
import com.xianyu.xianyuopenapiclientsdk.model.User;
import org.apache.commons.lang3.StringUtils;
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
     * @return
     */
    @PostMapping("/json")
    public String getUserNameByPost(HttpServletRequest request) {
        //网关有流量染色，模拟接口应进行相应的流量校验
        String gateway = request.getHeader("gateway");
        if (StringUtils.isBlank(gateway)) {
            return "";
        }
        if (!gateway.equals("xianyu_gateway")) {
            return "";
        }
        //api签名认证、接口统计次数在网关统一执行
        String body = request.getHeader("body");
        User user = JSONUtil.toBean(body, User.class);
        String result =  "Post 用户名字是"+user.getUsername();
        // todo调用次数+1 invokeCount
        return result;
    }
}
