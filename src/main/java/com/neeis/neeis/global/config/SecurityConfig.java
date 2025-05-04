package com.neeis.neeis.global.config;

import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(configurer -> configurer.configure(http))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/swagger-ui/**","/swagger-ui/index.html#/","/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/users/login", "/students/id", "/students/password", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teacherSubjects").permitAll()
                        .requestMatchers("/teachers/**", "/behavior/**" , "/counsel/**", "/attendances/**").hasAnyAuthority("ROLE_TEACHER")
                        .requestMatchers("/students/register","/subjects/**","/teacherSubjects/**").hasAnyAuthority("ROLE_TEACHER","ROLE_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                ;

        return http.build();
    }
}
