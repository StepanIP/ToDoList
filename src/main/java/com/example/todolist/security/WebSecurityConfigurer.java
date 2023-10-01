package com.example.todolist.security;

import com.example.todolist.repository.UserRepository;
import com.example.todolist.service.impl.security.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    private final MyUserDetailsService userDetailsService;
    private final WebAccessDeniedHandler webAccessDeniedHandler;

    private final UserRepository userRepository;

    public WebSecurityConfigurer(MyUserDetailsService userDetailsService, WebAccessDeniedHandler webAccessDeniedHandler, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.webAccessDeniedHandler = webAccessDeniedHandler;
        this.userRepository = userRepository;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/users/register").permitAll()
                .antMatchers("/", "/home", "/users/all").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String username = authentication.getName();
                    response.sendRedirect("/todos/all/users/" + userRepository.findByEmail(username).getId());
                })
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .deleteCookies("JSESSIONID");

        http.exceptionHandling().accessDeniedHandler(webAccessDeniedHandler);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }
}
