package com.example.taskmanagementtool.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.taskmanagementtool.service.ProjectService;
import com.example.taskmanagementtool.service.ReportService;
import com.example.taskmanagementtool.service.ReportService.MemberHoursSummary;

@Controller
@RequestMapping("/projects/{projectId}/reports")
public class ReportController {
	private final ReportService reportService;

	private final ProjectService projectService;

	public ReportController(ReportService reportService, ProjectService projectService) {
		this.reportService = reportService;
		this.projectService = projectService;
	}

	@GetMapping
	public String report(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("project", projectService.getProjectById(projectId));
		model.addAttribute("projectId", projectId);

		List<MemberHoursSummary> summaries = reportService.summarizeHoursByAssignee(projectId);
		model.addAttribute("summaries", summaries);

		// Thymeleafの#aggregatesにはmax()が存在しない(sum/avgのみ)ため、
		// 棒グラフの幅を正規化するための最大値はここ(Java側)で計算して渡す。
		int maxHours = summaries.stream()
				.mapToInt(s -> Math.max(s.planHoursTotal(), s.actualHoursTotal()))
				.max()
				.orElse(0);
		model.addAttribute("maxHours", maxHours);

		return "project/reports";
	}
}