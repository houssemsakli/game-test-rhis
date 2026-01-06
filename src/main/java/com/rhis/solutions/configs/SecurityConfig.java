package com.rhis.solutions.configs;

import com.rhis.solutions.services.AuthService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Autowired
    private AuthService authService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**", "/ws/**").permitAll()
                        .requestMatchers("/api/games/**").permitAll()
                        .anyRequest().authenticated()
                );

        // allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(authTokenFilter(), org.springframework.web.filter.CorsFilter.class);

        return http.build();
    }

    private Filter authTokenFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                String token = req.getHeader("Auth-Token");
                if (token != null) {
                    String username = authService.getUsernameForToken(token);
                    if (username != null) {
                        var auth = new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
                chain.doFilter(request, response);
            }
        };
    }
}