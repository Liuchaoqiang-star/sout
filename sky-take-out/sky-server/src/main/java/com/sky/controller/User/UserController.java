package com.sky.controller.User;


import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {

    // 用户登录的业务逻辑：负责用微信code换openid，并完成用户查询/注册
    @Autowired
    private UserService userService;

    // JWT相关配置：用户端密钥、过期时间、请求头名称都从application.yml读取
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录
     *
     * 前端小程序先调用wx.login()拿到code，再把code传给这个接口。
     * 后端登录成功后会返回token，前端后续请求需要把token放到authentication请求头中。
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录：{}", userLoginDTO.getCode());

        // 调用业务层完成微信登录：换openid、查用户、必要时自动注册
        User user = userService.wxLogin(userLoginDTO);

        // JWT中只放必要的用户身份信息，后续接口通过这个userId识别当前登录用户
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());

        // 使用用户端JWT配置生成token，和管理端员工token分开，避免互相混用
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        // VO是返回给前端的数据，只返回前端需要的id、openid、token
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }


}
