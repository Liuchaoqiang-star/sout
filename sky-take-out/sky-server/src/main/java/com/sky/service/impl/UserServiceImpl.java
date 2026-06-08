package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信官方接口：用小程序code换取openid和session_key
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    // 小程序appid和secret从配置文件读取，不能写在前端代码里
    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * C端微信登录
     *
     * 这里不直接相信前端传来的用户信息，而是用前端传来的code去微信服务器换openid。
     * openid是微信用户在当前小程序下的唯一标识，后端用它来判断用户是否已经注册。
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 1. 调用微信接口，用临时code换openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 微信接口没有返回openid，说明code无效、过期，或appid/secret配置不匹配
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 2. 根据openid查询用户是否已经存在
        User user = userMapper.getByOpenid(openid);

        // 3. 第一次登录的微信用户，自动往user表插入一条记录
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信登录凭证校验接口，获取当前微信用户的openid。
     */
    private String getOpenid(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信登录返回结果：{}", json);

        // 微信返回的是JSON字符串，这里解析出openid；如果失败，openid会是null
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
