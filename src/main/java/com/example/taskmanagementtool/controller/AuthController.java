package com.example.taskmanagementtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.taskmanagementtool.service.AuthService;

import lombok.Data;

@Controller
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Data
	public static class Signup {
		private String name;
		private String email;
		private String password;
		private String role;
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/login")
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			Model model) {
		if (error != null) {
			model.addAttribute("loginError", "メールアドレスまたはパスワードが正しくありません。");
		}
		if (logout != null) {
			model.addAttribute("message", "ログアウトしました。");
		}
		return "login";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("signup", new Signup());
		return "signup";
	}

	@PostMapping("/signup")
	public String register(@ModelAttribute Signup signup, Model model) {
		try {
			authService.register(signup.getName(), signup.getEmail(), signup.getPassword(), signup.getRole());
			return "redirect:/login?success";
		} catch (IllegalStateException e) {
			model.addAttribute("signup", signup);
			model.addAttribute("signupError", e.getMessage());
			return "signup";
		}
	}
}