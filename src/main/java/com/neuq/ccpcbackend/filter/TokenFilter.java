package com.neuq.ccpcbackend.filter;

import com.alibaba.fastjson.JSON;
import com.neuq.ccpcbackend.properties.KeyProperties;
import com.neuq.ccpcbackend.utils.JwtUtil;
import com.neuq.ccpcbackend.utils.response.Response;
import com.neuq.ccpcbackend.utils.response.ErrorCode;
import com.neuq.ccpcbackend.utils.WebUtil;

import com.neuq.ccpcbackend.vo.UserCacheVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取jwt
 * */
@Component
public class TokenFilter extends OncePerRequestFilter {

    public static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 在请求到达控制器之前执行
        // System.out.println("Request URL: " + request.getRequestURL());
        // System.out.println("Request Method: " + request.getMethod());

        // 获取token
        String token = request.getHeader(KeyProperties.TOKEN_HEADER);
        // 没有带token直接过去
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token获得userid
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (ExpiredJwtException e) {
            Response errorInfo = Response.error(ErrorCode.TOKEN_EXPIRE.getErrCode(), "accessToken已经过期");
            WebUtil.renderString(response, JSON.toJSONString(errorInfo));
            return;
        } catch (Exception e) {
            Response errorInfo = Response.error(ErrorCode.TOKEN_PARSE_ERROR.getErrCode(), "accessToken解析失败");
            WebUtil.renderString(response, JSON.toJSONString(errorInfo));
            return;
        }
        UserCacheVo userCacheVo = JSON.parseObject(claims.getSubject(), UserCacheVo.class);

        // 将权限和用户id保存
        String userId = userCacheVo.getId();
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userCacheVo.getIdentities()) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        // 从redis获取用户信息
        String key = KeyProperties.TOKEN_PREFIX + userId;
        // todo

        // 存入SecurityContextHolder
        // 获取权限信息封装到authticationToken中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 继续执行过滤器链
        filterChain.doFilter(request, response);

        // 在响应返回客户端之前执行
        // System.out.println("Response Status: " + response.getStatus());
    }
}
