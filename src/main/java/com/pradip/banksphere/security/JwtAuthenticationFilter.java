package com.pradip.banksphere.security;

import com.pradip.banksphere.service.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

       String authHeader =  request.getHeader(HttpHeaders.AUTHORIZATION);
       if (authHeader != null && authHeader.startsWith("Bearer ")){
           String token = authHeader.substring(7);

           if (jwtService.validateJwt(token)){
               Claims claims = jwtService.extractClaims(token);
               String sub = claims.getSubject();
               List<String> authoritiesList = claims.get("authorities", List.class);
               List<SimpleGrantedAuthority> authorities =
                       authoritiesList.stream()
                               .map(SimpleGrantedAuthority::new)
                               .toList();

               Authentication authentication = new UsernamePasswordAuthenticationToken(sub, null, authorities);

               SecurityContextHolder.getContext().setAuthentication(authentication);
           }
       }
        filterChain.doFilter(request, response);

    }
}
