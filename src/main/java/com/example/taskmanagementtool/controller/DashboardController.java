package com.example.taskmanagementtool.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.taskmanagementtool.entity.Milestone;
import com.example.taskmanagementtool.service.MilestoneService;
import com.example.taskmanagementtool.service.ProjectService;
import com.example.taskmanagementtool.service.TaskService;
import com.example.taskmanagementtool.service.TeamService;
import com.example.taskmanagementtool.service.UserService;

@Controller
public class DashboardController {

	private final MilestoneService milestoneService;
	private final TaskService taskService;
	private final UserService userService;
	private final TeamService teamService;
	private final ProjectService projectService;

	public DashboardController(MilestoneService milestoneService, TaskService taskService, UserService userService,
			TeamService teamService, ProjectService projectService) {
		this.milestoneService = milestoneService;
		this.taskService = taskService;
		this.userService = userService;
		this.teamService = teamService;
		this.projectService = projectService;
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

		// admin/system と同じ集計値を、既存のUserService/TeamServiceからそのまま流用する
		model.addAttribute("totalUsers", userService.countUsers());
		model.addAttribute("totalTeams", teamService.countTeams());
		// ステータスが進行中(IN_PROGRESS)のプロジェクト数
		model.addAttribute("activeProjects", projectService.countActiveProjects());

		return "admin/dashboard";
	}

	@GetMapping("/guest/dashboard")
	public String guestDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails != null) {
			String username = userDetails.getUsername();
			model.addAttribute("username", username);

			// ゲストに紐づくタスクの件数だけをダッシュボードのサマリーとして表示する
			// （一覧そのものは/guest/tasksで確認するため、ここでは件数のみ）
			model.addAttribute("sharedTaskCount", taskService.listTasksForUser(username).size());
		}

		return "guest/dashboard";
	}
}