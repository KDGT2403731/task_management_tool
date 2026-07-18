package com.example.taskmanagementtool.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/dashboard", "/settings/**", "/projects/**").hasRole("MEMBER")
						.requestMatchers("/guest/**").hasRole("GUEST")
						.anyRequest().authenticated())
				.formLogin(login -> login
						.loginPage("/login")
						.successHandler((request, response, authentication) -> {
							Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
							if (roles.contains("ROLE_ADMIN")) {
								response.sendRedirect("/admin/dashboard");
							} else if (roles.contains("ROLE_GUEST")) {
								response.sendRedirect("/guest/dashboard");
							} else if (roles.contains("ROLE_MEMBER")) {
								response.sendRedirect("/dashboard");
							} else {
								response.sendRedirect("/"); // 想定外のロールはトップへ
							}
						})
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/")
						.permitAll());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		// 非機能要件にあるパスワードハッシュ化（BCrypt）の定義
		return new BCryptPasswordEncoder();
	}
}
