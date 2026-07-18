package com.example.taskmanagementtool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.taskmanagementtool.service.TaskService;

@Controller
@RequestMapping("/guest")
public class GuestController {
	@Autowired
	private TaskService taskService;

	@GetMapping("/tasks")
	public String listTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		model.addAttribute("tasks", taskService.listTasksForUser(userDetails.getUsername()));
		return "guest/tasks";
	}

	@GetMapping("/tasks/gantt")
	public String ganttChart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		model.addAttribute("tasks", taskService.listTasksForUser(userDetails.getUsername()));
		return "guest/gantt";
	}
}
