package com.shijian.interceptor;

import com.shijian.utils.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS requests pass directly (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtils.getUserId(token) != null) {
                request.setAttribute("userId", jwtUtils.getUserId(token));
                request.setAttribute("username", jwtUtils.parseToken(token).get("username"));
                return true;
            }
        }
        // GET requests: allow anonymous browsing (controllers handle auth where needed)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // POST/PUT/DELETE: require authentication
        response.setStatus(401);
        return false;
    }
}
