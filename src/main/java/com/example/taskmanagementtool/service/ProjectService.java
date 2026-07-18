package com.example.taskmanagementtool.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.ProjectRepository;
import com.example.taskmanagementtool.repository.RecurringRuleRepository;
import com.example.taskmanagementtool.repository.TaskDependencyRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final RecurringRuleRepository recurringRuleRepository;
	private final TaskDependencyRepository taskDependencyRepository;

	public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
			RecurringRuleRepository recurringRuleRepository, TaskDependencyRepository taskDependencyRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.recurringRuleRepository = recurringRuleRepository;
		this.taskDependencyRepository = taskDependencyRepository;
	}

	@Transactional(readOnly = true)
	public List<Project> listProjectsForUser(String email) {
		User currentUser = findUserByEmail(email);
		return currentUser.getProjects() == null ? List.of() : currentUser.getProjects().stream().toList();
	}

	@Transactional(readOnly = true)
	public Project getProjectById(Long projectId) {
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("プロジェクトが存在しません: " + projectId));
	}

	@Transactional
	public Project createProject(String email, String name, String description, LocalDate startDate,
			LocalDate endDate) {
		User currentUser = findUserByEmail(email);

		if (currentUser.getTeam() == null) {
			throw new IllegalStateException("チームに所属していないユーザーはプロジェクトを作成できません。先にチームへの参加が必要です。");
		}

		Project project = new Project();
		project.setName(name);
		project.setDescription(description);
		project.setStartDate(startDate);
		project.setEndDate(endDate);
		project.setStatus("NOT_STARTED");
		project.setOwner(currentUser);
		project.setTeam(currentUser.getTeam());

		projectRepository.save(project);

		if (currentUser.getProjects() == null) {
			currentUser.setProjects(new HashSet<>());
		}
		currentUser.getProjects().add(project);
		userRepository.save(currentUser);

		return project;
	}

	@Transactional
	public Project updateProject(Long projectId, String name, String description, LocalDate startDate,
			LocalDate endDate) {
		Project project = getProjectById(projectId);
		project.setName(name);
		project.setDescription(description);
		project.setStartDate(startDate);
		project.setEndDate(endDate);
		return projectRepository.save(project);
	}

	@Transactional
	public void deleteProject(Long projectId) {
		Project project = getProjectById(projectId);

		int recurringRuleCount = recurringRuleRepository.findByProjectId(projectId).size();
		if (recurringRuleCount > 0) {
			throw new IllegalStateException(
					"このプロジェクトには" + recurringRuleCount + "件の繰り返しルールが紐づいているため削除できません。先に繰り返し設定を削除してください。");
		}

		int dependencyCount = taskDependencyRepository.findByPrecedingTaskProjectId(projectId).size();
		if (dependencyCount > 0) {
			throw new IllegalStateException(
					"このプロジェクトには" + dependencyCount + "件のタスク依存関係が設定されているため削除できません。先に依存関係を解除してください。");
		}

		projectRepository.delete(project);
	}

	private User findUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));
	}
}