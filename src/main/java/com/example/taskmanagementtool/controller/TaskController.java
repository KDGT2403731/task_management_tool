package com.example.taskmanagementtool.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

import com.example.taskmanagementtool.entity.Task;
import com.example.taskmanagementtool.entity.TaskDependency;
import com.example.taskmanagementtool.service.RecurringRuleService;
import com.example.taskmanagementtool.service.TaskDependencyService;
import com.example.taskmanagementtool.service.TaskService;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {
	private final TaskService taskService;
	private final TaskDependencyService taskDependencyService;
	private final RecurringRuleService recurringRuleService;

	public TaskController(TaskService taskService, TaskDependencyService taskDependencyService,
			RecurringRuleService recurringRuleService) {
		this.taskService = taskService;
		this.taskDependencyService = taskDependencyService;
		this.recurringRuleService = recurringRuleService;
	}

	@GetMapping
	public String listTasks(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("project", taskService.getProjectOrThrow(projectId));
		model.addAttribute("projectId", projectId);
		model.addAttribute("tasks", taskService.listByProject(projectId));
		return "task/list";
	}

	@GetMapping("/gantt")
	public String ganttChart(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("dependencies", taskDependencyService.getDependenciesByProject(projectId));
		model.addAttribute("tasks", taskService.listByProject(projectId));
		return "task/gantt";
	}

	@GetMapping("/{taskId}")
	public String taskDetail(@PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId,
			Model model) {
		List<Task> otherTasks = taskService.listOtherTasksInProject(projectId, taskId);
		List<TaskDependency> precedingDependencies = taskDependencyService.getPrecedingDependencies(taskId);
		List<TaskDependency> succeedingDependencies = taskDependencyService.getSucceedingDependencies(taskId);

		Set<Long> alreadyPrecedingIds = precedingDependencies.stream()
				.map(dep -> dep.getPrecedingTask().getId())
				.collect(Collectors.toSet());
		Set<Long> alreadySucceedingIds = succeedingDependencies.stream()
				.map(dep -> dep.getSucceedingTask().getId())
				.collect(Collectors.toSet());

		// 前提タスクの候補：すでに前提として登録済みのタスク、および追加すると循環になるタスクを除外する
		List<Task> precedingTaskOptions = otherTasks.stream()
				.filter(t -> !alreadyPrecedingIds.contains(t.getId()))
				.filter(t -> !taskDependencyService.wouldCreateCycle(t.getId(), taskId))
				.toList();

		// 後続タスクの候補：すでに後続として登録済みのタスク、および追加すると循環になるタスクを除外する
		List<Task> succeedingTaskOptions = otherTasks.stream()
				.filter(t -> !alreadySucceedingIds.contains(t.getId()))
				.filter(t -> !taskDependencyService.wouldCreateCycle(taskId, t.getId()))
				.toList();

		model.addAttribute("projectId", projectId);
		model.addAttribute("taskId", taskId);
		model.addAttribute("task", taskService.getTaskInProject(projectId, taskId));
		model.addAttribute("assignableUsers", taskService.listAssignableUsers(projectId));
		model.addAttribute("precedingTaskOptions", precedingTaskOptions);
		model.addAttribute("succeedingTaskOptions", succeedingTaskOptions);
		model.addAttribute("precedingDependencies", precedingDependencies);
		model.addAttribute("succeedingDependencies", succeedingDependencies);
		model.addAttribute("frequencies", RecurringRuleService.VALID_FREQUENCIES);
		return "task/detail";
	}

	@GetMapping("/create")
	public String createTaskForm(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("task", new Task());
		model.addAttribute("assignableUsers", taskService.listAssignableUsers(projectId));
		return "task/create";
	}

	@PostMapping("/create")
	public String createTask(@PathVariable("projectId") Long projectId,
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "priority", required = false) String priority,
			@RequestParam(value = "assigneeId", required = false) Long assigneeId,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "dueDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			@RequestParam(value = "planHours", required = false) Integer planHours) {

		taskService.createTask(projectId, userDetails.getUsername(), title, description, priority, assigneeId,
				startDate, dueDate, planHours);

		return "redirect:/projects/" + projectId + "/tasks";
	}

	@PostMapping("/{taskId}/edit")
	public String updateTask(@PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId,
			@RequestParam("title") String title,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "priority", required = false) String priority,
			@RequestParam(value = "assigneeId", required = false) Long assigneeId,
			@RequestParam(value = "startDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "dueDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
			@RequestParam(value = "planHours", required = false) Integer planHours,
			@RequestParam(value = "actualHours", required = false) Integer actualHours) {

		taskService.updateTask(projectId, taskId, title, description, priority, assigneeId, startDate, dueDate,
				planHours, actualHours);

		return "redirect:/projects/" + projectId + "/tasks/" + taskId;
	}

	@PostMapping("/{taskId}/delete")
	public String deleteTask(@PathVariable("projectId") Long projectId, @PathVariable("taskId") Long taskId) {
		taskService.deleteTask(projectId, taskId);
		return "redirect:/projects/" + projectId + "/tasks";
	}

	// ========== カンバン: ステータス更新 ==========

	@PostMapping("/{taskId}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId,
			@RequestParam("status") String status) {
		taskService.updateStatus(projectId, taskId, status);
		return ResponseEntity.ok().build();
	}

	// ========== 依存関係設定（UC09 スケジュール可視化の一部） ==========

	@PostMapping("/{taskId}/dependencies/add")
	public String addDependency(@PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long succeedingTaskId,
			@RequestParam("precedingTaskId") Long precedingTaskId,
			@RequestParam(value = "dependencyType", defaultValue = "FS") String dependencyType,
			@RequestParam(value = "lagDays", defaultValue = "0") Integer lagDays,
			RedirectAttributes redirectAttributes) {

		// succeedingTaskId(URLのtaskId)が本当にこのprojectIdに属しているかを検証してから処理する。
		// これがないと、他プロジェクトのタスクIDを直接指定して依存関係を作られてしまう。
		taskService.getTaskInProject(projectId, succeedingTaskId);

		try {
			taskDependencyService.addDependency(precedingTaskId, succeedingTaskId, dependencyType, lagDays);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("dependencyError", e.getMessage());
		}

		return "redirect:/projects/" + projectId + "/tasks/" + succeedingTaskId;
	}

	@PostMapping("/{taskId}/dependencies/add-succeeding")
	public String addSucceedingDependency(@PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long precedingTaskId,
			@RequestParam("succeedingTaskId") Long succeedingTaskId,
			@RequestParam(value = "dependencyType", defaultValue = "FS") String dependencyType,
			@RequestParam(value = "lagDays", defaultValue = "0") Integer lagDays,
			RedirectAttributes redirectAttributes) {

		// precedingTaskId(URLのtaskId)が本当にこのprojectIdに属しているかを検証してから処理する。
		taskService.getTaskInProject(projectId, precedingTaskId);

		try {
			taskDependencyService.addDependency(precedingTaskId, succeedingTaskId, dependencyType, lagDays);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("dependencyError", e.getMessage());
		}

		return "redirect:/projects/" + projectId + "/tasks/" + precedingTaskId;
	}

	// ========== 繰り返し設定（UC08） ==========

	@GetMapping("/recurring")
	public String listRecurringRules(@PathVariable("projectId") Long projectId, Model model) {
		model.addAttribute("projectId", projectId);
		model.addAttribute("rules", recurringRuleService.listByProject(projectId));
		model.addAttribute("frequencies", RecurringRuleService.VALID_FREQUENCIES);
		return "task/recurring";
	}

	@PostMapping("/{taskId}/recurring")
	public String createRecurringRule(@PathVariable("projectId") Long projectId,
			@PathVariable("taskId") Long taskId,
			@RequestParam("frequency") String frequency,
			@RequestParam(value = "cronExpression", required = false) String cronExpression,
			@RequestParam(value = "endDate", required = false)
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		recurringRuleService.createFromTask(projectId, taskId, frequency, cronExpression, endDate);
		return "redirect:/projects/" + projectId + "/tasks/" + taskId;
	}
}