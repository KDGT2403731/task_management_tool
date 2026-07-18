package com.example.taskmanagementtool.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.taskmanagementtool.entity.Milestone;
import com.example.taskmanagementtool.service.MilestoneService;
import com.example.taskmanagementtool.service.TaskService;

@Controller
public class DashboardController {

	private final MilestoneService milestoneService;
	private final TaskService taskService;

	public DashboardController(MilestoneService milestoneService, TaskService taskService) {
		this.milestoneService = milestoneService;
		this.taskService = taskService;
	}

	@SuppressWarnings("deprecation")
	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails != null) {
			String username = userDetails.getUsername(); // ログイン中のユーザー名（またはemail）を取得
			model.addAttribute("username", username);

			// ユーザーに紐づく今後のマイルストーンを取得してModelに登録
			List<Milestone> upcomingMilestones = milestoneService.getUpcomingMilestonesForUser(username);
			model.addAttribute("upcomingMilestones", upcomingMilestones);

			// ユーザーが所属するプロジェクト横断のタスク一覧
			model.addAttribute("myTasks", taskService.getTasksForUser(username));
		}

		return "dashboard";
	}

	@GetMapping("/admin/dashboard")
	public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails != null) {
			model.addAttribute("username", userDetails.getUsername());
		}

		// model.addAttribute("totalUsers", userService.countAllUsers());
		// model.addAttribute("activeProjects", projectService.countActiveProjects());

		return "admin/dashboard";
	}

	@GetMapping("/guest/dashboard")
	public String guestDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails != null) {
			model.addAttribute("username", userDetails.getUsername());
		}

		// model.addAttribute("sharedTasks", taskService.getSharedTasksForGuest(userDetails.getUsername()));

		return "guest/dashboard";
	}
}