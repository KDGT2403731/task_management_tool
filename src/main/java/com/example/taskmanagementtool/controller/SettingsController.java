package com.example.taskmanagementtool.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.taskmanagementtool.service.IntegrationService;

@Controller
@RequestMapping("/settings/integration")
public class SettingsController {

	private final IntegrationService integrationService;

	public SettingsController(IntegrationService integrationService) {
		this.integrationService = integrationService;
	}

	@GetMapping
	public String integrationSetting(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		model.addAttribute("integrations", integrationService.listForUser(userDetails.getUsername()));
		model.addAttribute("services", IntegrationService.VALID_SERVICES);
		return "settings/integration";
	}

	@PostMapping("/{serviceName}/connect")
	public String connect(@PathVariable("serviceName") String serviceName,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam(value = "apiEndpoint", required = false) String apiEndpoint,
			@RequestParam(value = "accessToken", required = false) String accessToken,
			@RequestParam(value = "refreshToken", required = false) String refreshToken,
			@RequestParam(value = "expiresAt", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
			RedirectAttributes redirectAttributes) {
		try {
			integrationService.connect(userDetails.getUsername(), serviceName, apiEndpoint, accessToken, refreshToken,
					expiresAt);
			return "redirect:/settings/integration";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("connectError", e.getMessage());
			return "redirect:/settings/integration";
		}
	}

	@PostMapping("/{id}/disconnect")
	public String disconnect(@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		try {
			integrationService.disconnect(userDetails.getUsername(), id);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("disconnectError", e.getMessage());
		}
		return "redirect:/settings/integration";
	}
}
