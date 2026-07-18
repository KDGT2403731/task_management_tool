package com.example.taskmanagementtool.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Task;
import com.example.taskmanagementtool.repository.ProjectRepository;
import com.example.taskmanagementtool.repository.TaskRepository;

@Service
public class ReportService {
	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;

	public ReportService(TaskRepository taskRepository, ProjectRepository projectRepository) {
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
	}

	@Transactional(readOnly = true)
	public List<MemberHoursSummary> summarizeHoursByAssignee(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new IllegalArgumentException("プロジェクトが存在しません: " + projectId);
		}

		List<Task> tasks = taskRepository.findByProjectId(projectId);

		Map<String, List<Task>> tasksByAssignee = tasks.stream()
				.collect(Collectors.groupingBy(
						task -> task.getAssignee() == null ? "未アサイン" : task.getAssignee().getName()));

		return tasksByAssignee.entrySet().stream()
				.map(entry -> toSummary(entry.getKey(), entry.getValue()))
				.sorted(Comparator.comparing(MemberHoursSummary::assigneeName))
				.toList();
	}

	private MemberHoursSummary toSummary(String assigneeName, List<Task> tasks) {
		int planTotal = tasks.stream().mapToInt(t -> t.getPlanHours() == null ? 0 : t.getPlanHours()).sum();
		int actualTotal = tasks.stream().mapToInt(t -> t.getActualHours() == null ? 0 : t.getActualHours()).sum();
		return new MemberHoursSummary(assigneeName, tasks.size(), planTotal, actualTotal);
	}

	public record MemberHoursSummary(String assigneeName, int taskCount, int planHoursTotal, int actualHoursTotal) {
	}
}
