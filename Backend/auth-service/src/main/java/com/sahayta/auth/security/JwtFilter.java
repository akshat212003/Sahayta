package com.sahayta.auth.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sahayta.auth.entity.User;
import com.sahayta.auth.repository.UserRepository;
import com.sahayta.auth.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
	
	@Autowired
	private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException 
    {

    	    String header = request.getHeader("Authorization");

    	    if (header != null && header.startsWith("Bearer ")) 
    	    {

    	        String token = header.substring(7);

    	        try {

    	            String email = jwtUtil.extractEmail(token);

    	            User user = userRepository.findByEmail(email).orElse(null);

    	            if (user != null) {

    	            	UsernamePasswordAuthenticationToken authentication =
    	            	        new UsernamePasswordAuthenticationToken(
    	            	                user,
    	            	                null,
    	            	                AuthorityUtils.createAuthorityList(
    	            	                        "ROLE_" + user.getRole().name()
    	            	                )
    	            	        );

    	                SecurityContextHolder.getContext().setAuthentication(authentication);
    	            }

    	        } catch (Exception e) {
    	            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    	            return;
    	        }
    	    }

    	    filterChain.doFilter(request, response);
    	}
    }
