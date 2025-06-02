package com.neeis.neeis.global.config;

import com.google.api.Http;
import com.neeis.neeis.global.jwt.JwtAuthenticationFilter;
import com.neeis.neeis.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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
                        .requestMatchers("/users/login","/users/password", "/students/id", "/students/password", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teacherSubjects", "/subjects", "/users/fcm/register").permitAll()
                        // 학적
                        .requestMatchers(HttpMethod.GET, "/teachers/students/**").hasAnyAuthority( "ROLE_STUDENT", "ROLE_PARENT", "ROLE_TEACHER")
                        // 출결
                        .requestMatchers(HttpMethod.GET, "/attendances/student", "/attendances/summary", "/attendances/feedback").hasAnyAuthority("ROLE_STUDENT", "ROLE_PARENT", "ROLE_TEACHER")
                        // 행동
                        .requestMatchers(HttpMethod.GET, "/behavior").hasAnyAuthority("ROLE_STUDENT","ROLE_PARENT", "ROLE_TEACHER")
                        // 상담
                        .requestMatchers(HttpMethod.GET, "/counsel", "/counsel/**").hasAnyAuthority("ROLE_STUDENT","ROLE_PARENT", "ROLE_TEACHER")
                        // 성적
                        .requestMatchers(HttpMethod.GET, "/evaluation-methods").hasAnyAuthority("ROLE_STUDENT", "ROLE_PARENT", "ROLE_TEACHER")
                        .requestMatchers(HttpMethod.GET, "/score-summary", "/score-summary/feedback/**").hasAnyAuthority("ROLE_STUDENT", "ROLE_PARENT", "ROLE_TEACHER")

                        // 교사 권한
                        .requestMatchers("/teachers/**", "/attendances/**", "/behavior/**" , "/counsel/**", "/evaluation-methods/**","/scores","/scores/**", "/score-summary/**").hasAnyAuthority("ROLE_TEACHER")
                        .requestMatchers("/students/register","/students/**","/subjects/**","/teacherSubjects/**").hasAnyAuthority("ROLE_TEACHER","ROLE_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unAuthorizedEntryPoint()))
        ;

        return http.build();
    }

    private AuthenticationEntryPoint unAuthorizedEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }
}
