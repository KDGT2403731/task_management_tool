package com.example.taskmanagementtool.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class AuthService {

	private static final List<String> ALLOWED_SIGNUP_ROLES = List.of("MEMBER", "GUEST");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User register(String name, String email, String rawPassword, String role) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new IllegalStateException("このメールアドレスは既に登録されています。");
		}

		String resolvedRole = ALLOWED_SIGNUP_ROLES.contains(role) ? role : "MEMBER";

		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		user.setRole(resolvedRole);

		return userRepository.save(user);
	}
}