package com.example.taskmanagementtool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.taskmanagementtool.service.ProjectService;
import com.example.taskmanagementtool.service.ReportService;

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
		model.addAttribute("summaries", reportService.summarizeHoursByAssignee(projectId));
		return "project/reports";
	}
}
