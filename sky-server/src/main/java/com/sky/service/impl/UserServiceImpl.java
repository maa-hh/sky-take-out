package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.common.utils.HttpUtil;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserServiceImpl implements UserService {
    @Autowired
    WeChatProperties weChatProperties;
    @Autowired
    UserMapper userMapper;
    private static final String url="https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String openid=getOpenid(userLoginDTO);
        if(openid==null||openid.length()==0){
            throw new LoginFailedException("user is null");
        }
        User user=userMapper.getByOpenid(openid);
        if(user==null){
            User newUser=User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(newUser);
            return newUser;
        }
        return user;
    }
    private String getOpenid(UserLoginDTO userLoginDTO){
        Map<String,String> m=new HashMap<>();
        m.put("appid",weChatProperties.getAppid());
        m.put("secret",weChatProperties.getSecret());
        m.put("js_code",userLoginDTO.getCode());
        m.put("grant_type","authorization_code");
        String s = HttpClientUtil.doGet(url, m);
        System.out.println("WeChat response: " + s);
        JSONObject jsonObject= JSON.parseObject(s);
        String openid=jsonObject.getString("openid");
        return openid;
    }
}
