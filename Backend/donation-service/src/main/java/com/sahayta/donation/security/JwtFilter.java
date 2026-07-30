package com.sahayta.donation.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sahayta.donation.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    public JwtFilter() {
        System.out.println("===== JwtFilter Bean Created =====");
    }

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

                Long userId = jwtUtil.extractUserId(token);
                String name = jwtUtil.extractName(token);
                
                
                System.out.println("========== JWT ==========");
                System.out.println("Email   : " + email);
                System.out.println("Role    : " + role);
                System.out.println("User ID : " + userId);
                System.out.println("Name    : " + name);
                System.out.println("=========================");

                UserPrincipal principal =
                        new UserPrincipal(userId, name, email);

                var authorities = AuthorityUtils.createAuthorityList("ROLE_" + role);

                System.out.println("Authorities = " + authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {

                e.printStackTrace();   // <-- add this

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(e.getMessage());

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}