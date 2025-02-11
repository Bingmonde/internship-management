package com.prose.security;

import com.prose.entity.users.UserApp;
import com.prose.repository.*;
import com.prose.security.exception.UserNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final UserAppRepository userAppRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserAppRepository userAppRepository) {
        this.tokenProvider = tokenProvider;
        this.userAppRepository = userAppRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = getJWTFromRequest(request);
        if (StringUtils.hasText(token)) {
        	token = token.startsWith("Bearer") ? token.substring(7) : token;
            try {
                tokenProvider.validateToken(token);
                String jwtUsername = tokenProvider.getJwtUsername(token);
                UserApp userApp = userAppRepository.findUserAppByEmail(jwtUsername).orElseThrow(UserNotFoundException::new);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userApp.getCredentials().getEmail(), null, userApp.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                logger.error("Could not set user authentication in security context", e);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

}
