package com.wms.config;

import com.wms.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && !jwtUtils.isTokenExpired(token)) {
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                // 检查是否需要刷新 Token
                if (jwtUtils.shouldTokenRefresh(token)) {
                    String newToken = jwtUtils.generateToken(userId, username, role);
                    response.setHeader("Authorization-Refresh", newToken);
                    response.setHeader("Access-Control-Expose-Headers", "Authorization-Refresh");
                }

                // 填充 Spring Security 上下文，防止 anyRequest().authenticated() 拦截
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                    java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = 
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {}
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
