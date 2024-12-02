package com.example.demo.config;


import com.example.demo.config.auth.exceptionHandler.CustomAccessDeniedHandler;
import com.example.demo.config.auth.exceptionHandler.CustomAuthenticationEntryPoint;
import com.example.demo.config.auth.jwt.JwtAuthorizationFilter;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import com.example.demo.config.auth.loginHandler.CustomAuthenticationFailureHandler;
import com.example.demo.config.auth.loginHandler.CustomLoginSuccessHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutHandler;
import com.example.demo.config.auth.logoutHandler.CustomLogoutSuccessHandler;
import com.example.demo.repository.UserRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private HikariDataSource dataSource;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain config(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());

        // 요청 URL별 접근 제한
        http.authorizeHttpRequests((auth) -> {
            auth.requestMatchers("/user/login", "/user/login**", "/user/**" ,"/js/**", "/css/**", "/assets/**", "/img/**","/forgot-password/**","/update-password" ).permitAll();
            auth.requestMatchers("/admin").hasRole("ADMIN");
            auth.requestMatchers("/board/**", "/content/**").permitAll();
            auth.requestMatchers("/register/**", "/region", "/error").permitAll();
            auth.anyRequest().authenticated();
        });

        // 로그인 설정
        http.formLogin((login) -> {
            login.loginPage("/user/login") // 사용자 정의 로그인 페이지
                    .loginProcessingUrl("/user/login") // 로그인 요청 경로 (HTML 폼의 action 경로와 일치)
                    .successHandler(new CustomLoginSuccessHandler(jwtTokenProvider)) // 성공 핸들러
                    .failureHandler(new CustomAuthenticationFailureHandler()) // 실패 핸들러
                    .permitAll(); // 로그인 페이지 및 처리 경로 허용
        });

        // 로그아웃 설정
        http.logout((logout) -> {
            logout.logoutUrl("/user/logout")
                    .logoutSuccessHandler(customLogoutSuccessHandler())
                    .addLogoutHandler(customLogoutHandler())
                    .deleteCookies("JSESSIONID", JwtProperties.COOKIE_NAME)
                    .invalidateHttpSession(true)
                    .permitAll();
        });

        // 예외처리 설정
        http.exceptionHandling((ex) -> {
            ex.accessDeniedHandler(new CustomAccessDeniedHandler());
            ex.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        });

        // REMEMBER_ME 설정
        http.rememberMe((rm) -> {
            rm.key("rememberMeKey")
                    .rememberMeParameter("remember-me")
                    .tokenValiditySeconds(60 * 60 * 24 * 7)
                    .tokenRepository(tokenRepository())
                    .alwaysRemember(false);
        });

        // SESSION 관리 정책 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 필터 추가
        http.addFilterBefore(new JwtAuthorizationFilter(userRepository, jwtTokenProvider), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomLogoutHandler customLogoutHandler(){
        return new CustomLogoutHandler();
    }

    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }
}
