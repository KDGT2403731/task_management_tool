package com.example.taskmanagementtool.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.example.taskmanagementtool.entity.Team;
import com.example.taskmanagementtool.service.TeamService;
import com.example.taskmanagementtool.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private final UserService userService;
	private final TeamService teamService;

	public AdminController(UserService userService, TeamService teamService) {
		this.userService = userService;
		this.teamService = teamService;
	}

	// ========== UC01: ユーザー管理 ==========

	@GetMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userService.listAllUsers());
		return "admin/users/list";
	}

	@GetMapping("/users/{id}")
	public String userDetail(@PathVariable("id") Long id, Model model) {
		model.addAttribute("user", userService.getUserById(id));
		model.addAttribute("teams", teamService.listAllTeams());
		model.addAttribute("roles", UserService.VALID_ROLES);
		return "admin/users/detail";
	}

	@PostMapping("/users/{id}/role")
	public String updateUserRole(@PathVariable("id") Long id, @RequestParam("role") String role) {
		userService.updateRole(id, role);
		return "redirect:/admin/users/" + id;
	}

	@PostMapping("/users/{id}/team")
	public String updateUserTeam(@PathVariable("id") Long id,
			@RequestParam(value = "teamId", required = false) Long teamId) {
		userService.updateTeam(id, teamId);
		return "redirect:/admin/users/" + id;
	}

	@PostMapping("/users/{id}/delete")
	public String deleteUser(@PathVariable("id") Long id,
			@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {

		try {
			userService.deleteUser(id, userDetails.getUsername());
			return "redirect:/admin/users";
		} catch (IllegalStateException e) {
			// 自分自身を削除しようとした場合など
			redirectAttributes.addFlashAttribute("deleteError", e.getMessage());
			return "redirect:/admin/users";
		}
	}

	// ========== UC02: チーム・部署管理 ==========

	@GetMapping("/teams")
	public String listTeams(Model model) {
		var teams = teamService.listAllTeams();
		model.addAttribute("teams", teams);

		Map<Long, Integer> memberCounts = new HashMap<>();
		for (Team team : teams) {
			memberCounts.put(team.getId(), teamService.findTeamMembers(team.getId()).size());
		}
		model.addAttribute("memberCounts", memberCounts);

		return "admin/teams/list";
	}

	@GetMapping("/teams/create")
	public String createTeamForm(Model model) {
		model.addAttribute("team", new Team());
		return "admin/teams/create";
	}

	@PostMapping("/teams/create")
	public String createTeam(@RequestParam("name") String name) {
		teamService.createTeam(name);
		return "redirect:/admin/teams";
	}

	@GetMapping("/teams/{id}")
	public String teamDetail(@PathVariable("id") Long id, Model model) {
		model.addAttribute("team", teamService.getTeamById(id));
		model.addAttribute("members", teamService.findTeamMembers(id));
		return "admin/teams/detail";
	}

	@PostMapping("/teams/{id}/delete")
	public String deleteTeam(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			teamService.deleteTeam(id);
			return "redirect:/admin/teams";
		} catch (IllegalStateException e) {
			// メンバー・プロジェクトが残っているため削除できなかった場合
			redirectAttributes.addFlashAttribute("deleteError", e.getMessage());
			return "redirect:/admin/teams/" + id;
		}
	}

	// ========== UC03: システム管理 ==========

	@GetMapping("/system")
	public String system(Model model) {
		model.addAttribute("totalUsers", userService.countUsers());
		model.addAttribute("totalTeams", teamService.countTeams());
		return "admin/system";
	}
}