package com.sahayta.request.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sahayta.request.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            try {

                String token = header.substring(7);

                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                System.out.println("Email = " + email);
                System.out.println("Role = " + role);

                Long userId = jwtUtil.extractUserId(token);
                String name = jwtUtil.extractName(token);

                UserPrincipal principal =
                        new UserPrincipal(userId, name, email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                AuthorityUtils.createAuthorityList("ROLE_" + role)
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {

                e.printStackTrace();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}