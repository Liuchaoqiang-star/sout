package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * C端用户jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 静态资源或非Controller方法不需要校验token
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 请求头名称来自配置：sky.jwt.user-token-name，当前是authentication
        String token = request.getHeader(jwtProperties.getUserTokenName());

        try {
            log.info("用户jwt校验：{}", token);

            // 用用户端密钥解析JWT，解析成功说明token没有被篡改且未过期
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);

            // 登录时把userId放进了JWT，这里取出来作为当前请求的用户身份
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());

            // 存入ThreadLocal，后续Service/Mapper层需要当前用户id时可以直接取
            BaseContext.setCurrentId(userId);
            log.info("当前用户id：{}", userId);
            return true;
        } catch (Exception ex) {
            // token缺失、过期、被篡改都会进这里，返回401表示未登录或登录失效
            response.setStatus(401);
            return false;
        }
    }
}
