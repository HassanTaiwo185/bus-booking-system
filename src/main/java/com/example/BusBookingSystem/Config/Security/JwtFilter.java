package com.example.BusBookingSystem.Config.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate redisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/verify");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {



        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getCookie(request, "accessToken");
        String refreshToken = getCookie(request, "refreshToken");

        try {
            if (accessToken != null) {

                Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + accessToken);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String email = jwtService.getUsernameFromToken(accessToken);
                UserDetails user = userDetailsService.loadUserByUsername(email);

                if (jwtService.validateToken(accessToken, user)) {
                    authenticate(request, user);
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            if (refreshToken != null) {
                String email = jwtService.getUsernameFromToken(refreshToken);
                UserDetails user = userDetailsService.loadUserByUsername(email);

                if (jwtService.validateToken(refreshToken, user)) {
                    String newAccessToken =
                            jwtService.generateToken(
                                   user
                            );

                    Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
                    newAccessTokenCookie.setHttpOnly(true);
                    newAccessTokenCookie.setSecure(false);
                    newAccessTokenCookie.setPath("/");
                    newAccessTokenCookie.setMaxAge(60 * 30);
                    response.addCookie(newAccessTokenCookie);

                    authenticate(request, user);
                }
            }

        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void authenticate(HttpServletRequest request, UserDetails user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

        auth.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
