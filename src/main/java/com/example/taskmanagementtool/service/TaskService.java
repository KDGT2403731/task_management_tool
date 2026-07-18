package com.example.taskmanagementtool.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.entity.Task;
import com.example.taskmanagementtool.entity.User;
import com.example.taskmanagementtool.repository.ProjectRepository;
import com.example.taskmanagementtool.repository.TaskDependencyRepository;
import com.example.taskmanagementtool.repository.TaskRepository;
import com.example.taskmanagementtool.repository.UserRepository;

@Service
public class TaskService {
	public static final List<String> VALID_STATUSES = List.of("TODO", "IN_PROGRESS", "DONE");

	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final TaskDependencyRepository taskDependencyRepository;

	public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
			UserRepository userRepository, TaskDependencyRepository taskDependencyRepository) {
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.taskDependencyRepository = taskDependencyRepository;
	}

	@Transactional(readOnly = true)
	public Project getProjectOrThrow(Long projectId) {
		return projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("プロジェクトが存在しません: " + projectId));
	}

	@Transactional(readOnly = true)
	public List<Task> listByProject(Long projectId) {
		ensureProjectExists(projectId);
		return taskRepository.findByProjectId(projectId);
	}

	@Transactional(readOnly = true)
	public Task getTaskInProject(Long projectId, Long taskId) {
		return findTaskInProject(projectId, taskId);
	}

	/**
	 * 依存関係の「前提タスク」候補として、同じプロジェクト内の他タスク一覧を返す（自分自身は除外）。
	 */
	@Transactional(readOnly = true)
	public List<Task> listOtherTasksInProject(Long projectId, Long excludeTaskId) {
		return taskRepository.findByProjectId(projectId).stream()
				.filter(t -> !t.getId().equals(excludeTaskId))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<User> listAssignableUsers(Long projectId) {
		Project project = getProjectOrThrow(projectId);
		if (project.getTeam() == null) {
			return List.of();
		}
		Long teamId = project.getTeam().getId();
		return userRepository.findAll().stream()
				.filter(u -> u.getTeam() != null && teamId.equals(u.getTeam().getId()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<Task> listTasksForUser(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + email));

		if (user.getProjects() == null || user.getProjects().isEmpty()) {
			return List.of();
		}

		return user.getProjects().stream()
				.flatMap(project -> taskRepository.findByProjectId(project.getId()).stream())
				.toList();
	}

	@Deprecated
	@Transactional(readOnly = true)
	public List<Task> getTasksForUser(String email) {
		return listTasksForUser(email);
	}

	@Transactional
	public Task createTask(Long projectId, String creatorEmail, String title, String description, String priority,
			Long assigneeId, LocalDate startDate, LocalDate dueDate, Integer planHours) {

		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("プロジェクトが存在しません: " + projectId));

		User currentUser = userRepository.findByEmail(creatorEmail)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが存在しません: " + creatorEmail));

		Task task = new Task();
		task.setProject(project);
		task.setTitle(title);
		task.setDescription(description);
		// priorityはnullable = falseのため、未入力時は"MEDIUM"をデフォルトにする
		task.setPriority((priority == null || priority.isBlank()) ? "MEDIUM" : priority);
		task.setStartDate(startDate);
		task.setDueDate(dueDate);
		task.setPlanHours(planHours);
		task.setStatus("TODO");
		task.setCreatedBy(currentUser);

		// 担当者が指定されていれば設定（未指定の場合は未アサインのまま）
		if (assigneeId != null) {
			User assignee = userRepository.findById(assigneeId)
					.orElseThrow(() -> new IllegalArgumentException("担当者が存在しません: " + assigneeId));
			task.setAssignee(assignee);
		}

		return taskRepository.save(task);
	}

	@Transactional
	public Task updateTask(Long projectId, Long taskId, String title, String description, String priority,
			Long assigneeId, LocalDate startDate, LocalDate dueDate, Integer planHours, Integer actualHours) {

		Task task = findTaskInProject(projectId, taskId);

		task.setTitle(title);
		task.setDescription(description);
		// priorityはnullable = falseのため、未入力時は既存値を維持する（空欄で上書きしない）
		if (priority != null && !priority.isBlank()) {
			task.setPriority(priority);
		}
		task.setStartDate(startDate);
		task.setDueDate(dueDate);
		task.setPlanHours(planHours);
		task.setActualHours(actualHours);

		if (assigneeId != null) {
			User assignee = userRepository.findById(assigneeId)
					.orElseThrow(() -> new IllegalArgumentException("担当者が存在しません: " + assigneeId));
			task.setAssignee(assignee);
		} else {
			task.setAssignee(null); // 担当者を解除する場合
		}

		return taskRepository.save(task);
	}

	@Transactional
	public void deleteTask(Long projectId, Long taskId) {
		Task task = findTaskInProject(projectId, taskId);

		// TaskはTaskDependencyへのマッピングを持たないため、依存関係が残っていると
		// 外部キー制約違反になる。先に前提/後続どちらにも使われていないか確認する。
		int asPreceding = taskDependencyRepository.findByPrecedingTaskId(taskId).size();
		int asSucceeding = taskDependencyRepository.findBySucceedingTaskId(taskId).size();
		if (asPreceding > 0 || asSucceeding > 0) {
			throw new IllegalStateException(
					"このタスクは" + (asPreceding + asSucceeding) + "件の依存関係に使われているため削除できません。先に依存関係を解除してください。");
		}

		taskRepository.delete(task);
	}

	/**
	 * カンバンのドラッグ&ドロップでステータスだけを更新する。
	 */
	@Transactional
	public Task updateStatus(Long projectId, Long taskId, String status) {
		if (!VALID_STATUSES.contains(status)) {
			throw new IllegalArgumentException("不正なステータスです: " + status);
		}
		Task task = findTaskInProject(projectId, taskId);
		task.setStatus(status);
		return taskRepository.save(task);
	}

	private void ensureProjectExists(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new IllegalArgumentException("プロジェクトが存在しません: " + projectId);
		}
	}

	private Task findTaskInProject(Long projectId, Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new IllegalArgumentException("タスクが存在しません: " + taskId));

		if (task.getProject() == null || !projectId.equals(task.getProject().getId())) {
			throw new IllegalArgumentException(
					"指定されたプロジェクトにこのタスクは属していません: projectId=" + projectId + ", taskId=" + taskId);
		}

		return task;
	}
}