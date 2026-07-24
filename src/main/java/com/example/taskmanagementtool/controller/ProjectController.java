package com.example.taskmanagementtool.controller;

import java.time.LocalDate;
import java.util.List;

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

import com.example.taskmanagementtool.entity.Milestone;
import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.service.MilestoneService;
import com.example.taskmanagementtool.service.ProjectService;
import com.example.taskmanagementtool.service.TeamService;

@Controller
@RequestMapping("/projects")
public class ProjectController {
	private final ProjectService projectService;
	private final MilestoneService milestoneService;
	private final TeamService teamService;

	public ProjectController(ProjectService projectService, MilestoneService milestoneService,
			TeamService teamService) {
		this.projectService = projectService;
		this.milestoneService = milestoneService;
		this.teamService = teamService;
	}

	// ========== プロジェクト ==========

	@GetMapping
	public String listProjects(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		List<Project> projects = projectService.listProjectsForUser(userDetails.getUsername());
		model.addAttribute("projects", projects);
		return "project/list";
	}

	@GetMapping("/{id}")
	public String projectDetail(@PathVariable("id") Long id, Model model) {
		Project project = projectService.getProjectById(id);
		model.addAttribute("project", project);
		model.addAttribute("projectId", id);

		// メンバー管理パネル用: 同じチームに所属していて、まだこのプロジェクトのメンバーになっていないユーザー一覧
		if (project.getTeam() != null) {
			List<User> teamMembers = teamService.findTeamMembers(project.getTeam().getId());
			List<User> addableUsers = teamMembers.stream()
					.filter(u -> project.getMembers() == null || !project.getMembers().contains(u))
					.toList();
			model.addAttribute("addableUsers", addableUsers);
		}

		return "project/detail";
	}

	@GetMapping("/create")
	public String createProjectForm(Model model) {
		model.addAttribute("project", new Project());
		return "project/create";
	}

	@PostMapping("/create")
	public String createProject(@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			RedirectAttributes redirectAttributes) {

		try {
			projectService.createProject(userDetails.getUsername(), name, description, startDate, endDate);
			return "redirect:/projects";
		} catch (IllegalStateException e) {
			// チーム未所属など、業務ルール違反で作成できなかった場合はフォームに戻してエラー表示する
			redirectAttributes.addFlashAttribute("createError", e.getMessage());
			redirectAttributes.addFlashAttribute("name", name);
			redirectAttributes.addFlashAttribute("description", description);
			redirectAttributes.addFlashAttribute("startDate", startDate);
			redirectAttributes.addFlashAttribute("endDate", endDate);
			return "redirect:/projects/create";
		}
	}

	@GetMapping("/{id}/edit")
	public String editProjectForm(@PathVariable("id") Long id, Model model) {
		model.addAttribute("project", projectService.getProjectById(id));
		return "project/edit";
	}

	@PostMapping("/{id}/edit")
	public String updateProject(@PathVariable("id") Long id,
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		projectService.updateProject(id, name, description, startDate, endDate);
		return "redirect:/projects/" + id;
	}

	@PostMapping("/{id}/delete")
	public String deleteProject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
		try {
			projectService.deleteProject(id);
			return "redirect:/projects";
		} catch (IllegalStateException e) {
			// 繰り返しルールやタスク依存関係が残っているため削除できなかった場合
			redirectAttributes.addFlashAttribute("deleteError", e.getMessage());
			return "redirect:/projects/" + id;
		}
	}

	// ========== プロジェクトメンバー ==========

	@PostMapping("/{id}/members/add")
	public String addMember(@PathVariable("id") Long id,
			@RequestParam("userId") Long userId,
			RedirectAttributes redirectAttributes) {
		try {
			projectService.addMember(id, userId);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("memberError", e.getMessage());
		}
		return "redirect:/projects/" + id;
	}

	@PostMapping("/{id}/members/{userId}/remove")
	public String removeMember(@PathVariable("id") Long id,
			@PathVariable("userId") Long userId,
			RedirectAttributes redirectAttributes) {
		try {
			projectService.removeMember(id, userId);
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("memberError", e.getMessage());
		}
		return "redirect:/projects/" + id;
	}

	// ========== マイルストーン ==========

	@GetMapping("/{projectId}/milestones")
	public String listMilestones(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("project", projectService.getProjectById(projectId));
		model.addAttribute("projectId", projectId);
		model.addAttribute("milestones", milestoneService.listByProject(projectId));
		return "milestone/list";
	}

	@GetMapping("/{projectId}/milestones/{milestoneId}")
	public String milestoneDetail(@PathVariable("projectId") Long projectId,
			@PathVariable("milestoneId") Long milestoneId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("milestone", milestoneService.getMilestoneInProject(projectId, milestoneId));
		return "milestone/detail";
	}

	@GetMapping("/{projectId}/milestones/create")
	public String createMilestoneForm(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("milestone", new Milestone());
		return "milestone/create";
	}

	@PostMapping("/{projectId}/milestones/create")
	public String createMilestone(@PathVariable("projectId") Long projectId,
			@RequestParam("title") String title,
			@RequestParam(value = "targetDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
			@RequestParam(value = "description", required = false) String description) {

		milestoneService.createMilestone(projectId, title, targetDate, description);
		return "redirect:/projects/" + projectId + "/milestones";
	}

	@GetMapping("/{projectId}/milestones/{milestoneId}/edit")
	public String editMilestoneForm(@PathVariable("projectId") Long projectId,
			@PathVariable("milestoneId") Long milestoneId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("milestone", milestoneService.getMilestoneInProject(projectId, milestoneId));
		model.addAttribute("statuses", MilestoneService.VALID_STATUSES);
		return "milestone/edit";
	}

	@PostMapping("/{projectId}/milestones/{milestoneId}/edit")
	public String updateMilestone(@PathVariable("projectId") Long projectId,
			@PathVariable("milestoneId") Long milestoneId,
			@RequestParam("title") String title,
			@RequestParam(value = "targetDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "status", required = false) String status) {

		milestoneService.updateMilestone(projectId, milestoneId, title, targetDate, description, status);
		return "redirect:/projects/" + projectId + "/milestones/" + milestoneId;
	}

	@PostMapping("/{projectId}/milestones/{milestoneId}/status")
	public String updateMilestoneStatus(@PathVariable("projectId") Long projectId,
			@PathVariable("milestoneId") Long milestoneId,
			@RequestParam("status") String status) {

		milestoneService.updateStatus(projectId, milestoneId, status);
		return "redirect:/projects/" + projectId + "/milestones/" + milestoneId;
	}

	@PostMapping("/{projectId}/milestones/{milestoneId}/delete")
	public String deleteMilestone(@PathVariable("projectId") Long projectId,
			@PathVariable("milestoneId") Long milestoneId,
			RedirectAttributes redirectAttributes) {

		try {
			milestoneService.deleteMilestone(projectId, milestoneId);
			return "redirect:/projects/" + projectId + "/milestones";
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("deleteError", e.getMessage());
			return "redirect:/projects/" + projectId + "/milestones/" + milestoneId;
		}
	}
}