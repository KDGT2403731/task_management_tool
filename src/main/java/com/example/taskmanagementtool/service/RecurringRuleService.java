package com.example.taskmanagementtool.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmanagementtool.entity.Project;
import com.example.taskmanagementtool.entity.RecurringRule;
import com.example.taskmanagementtool.entity.Task;
import com.example.taskmanagementtool.repository.ProjectRepository;
import com.example.taskmanagementtool.repository.RecurringRuleRepository;
import com.example.taskmanagementtool.repository.TaskRepository;

@Service
public class RecurringRuleService {
	public static final List<String> VALID_FREQUENCIES = List.of("DAILY", "WEEKLY", "MONTHLY");

	private final RecurringRuleRepository recurringRuleRepository;
	private final TaskRepository taskRepository;
	private final ProjectRepository projectRepository;

	public RecurringRuleService(RecurringRuleRepository recurringRuleRepository, TaskRepository taskRepository,
			ProjectRepository projectRepository) {
		this.recurringRuleRepository = recurringRuleRepository;
		this.taskRepository = taskRepository;
		this.projectRepository = projectRepository;
	}

	@Transactional(readOnly = true)
	public List<RecurringRule> listAll() {
		return recurringRuleRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<RecurringRule> listActive() {
		return recurringRuleRepository.findByIsActiveTrue();
	}

	@Transactional(readOnly = true)
	public List<RecurringRule> listByProject(Long projectId) {
		if (!projectRepository.existsById(projectId)) {
			throw new IllegalArgumentException("プロジェクトが存在しません: " + projectId);
		}
		return recurringRuleRepository.findByProjectId(projectId);
	}

	@Transactional(readOnly = true)
	public RecurringRule getById(Long id) {
		return recurringRuleRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("繰り返しルールが存在しません: " + id));
	}

	@Transactional
	public RecurringRule save(RecurringRule rule) {
		validateFrequency(rule.getFrequency());
		if (rule.getNextExecutionDate() == null) {
			// 初回のnext_execution_dateが未設定だと、スケジューラーが永久に発火判定できないため、
			// 作成時点を起点に設定しておく。
			rule.setNextExecutionDate(LocalDateTime.now());
		}
		return recurringRuleRepository.save(rule);
	}

	/**
	 * 既存タスクの内容（title/description/assignee/project）をテンプレートとして
	 * 繰り返しルールを作成する。タスク詳細画面の「繰り返し設定」導線用。
	 */
	@Transactional
	public RecurringRule createFromTask(Long projectId, Long taskId, String frequency, String cronExpression,
			LocalDate endDate) {
		validateFrequency(frequency);

		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new IllegalArgumentException("タスクが存在しません: " + taskId));

		if (task.getProject() == null || !projectId.equals(task.getProject().getId())) {
			throw new IllegalArgumentException(
					"指定されたプロジェクトにこのタスクは属していません: projectId=" + projectId + ", taskId=" + taskId);
		}

		RecurringRule rule = new RecurringRule();
		rule.setProject(task.getProject());
		rule.setAssignee(task.getAssignee());
		rule.setTemplateTitle(task.getTitle());
		rule.setTemplateDescription(task.getDescription());
		rule.setFrequency(frequency);
		rule.setCronExpression(cronExpression);
		rule.setEndDate(endDate);
		rule.setIsActive(true);
		rule.setNextExecutionDate(LocalDateTime.now());

		return recurringRuleRepository.save(rule);
	}

	@Transactional
	public void delete(Long id) {
		recurringRuleRepository.deleteById(id);
	}

	/**
	 * 毎日0時に実行し、実行タイミングが到来したアクティブなルールから新規タスクを自動生成する。
	 * 注意: メインの@SpringBootApplicationクラス（またはいずれかの@Configurationクラス）に
	 * @EnableSchedulingが付いていないと、このメソッドは一切呼び出されない。要確認。
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void generateScheduledTasks() {
		LocalDateTime now = LocalDateTime.now();
		List<RecurringRule> activeRules = recurringRuleRepository.findByIsActiveTrue();

		for (RecurringRule rule : activeRules) {
			if (rule.getEndDate() != null && rule.getEndDate().isBefore(LocalDate.now())) {
				rule.setIsActive(false);
				recurringRuleRepository.save(rule);
				continue;
			}

			if (rule.getNextExecutionDate() != null && rule.getNextExecutionDate().isBefore(now)) {
				Project project = rule.getProject();

				Task newTask = new Task();
				newTask.setProject(project);
				newTask.setAssignee(rule.getAssignee());
				// Task.createdByはnullable = falseのため必須。自動生成の主体としてプロジェクトオーナーを設定する
				// （owner_idはnullable = falseで必ず存在するため安全）。
				newTask.setCreatedBy(project.getOwner());
				newTask.setTitle(rule.getTemplateTitle());
				newTask.setDescription(rule.getTemplateDescription());
				newTask.setStatus("TODO");
				newTask.setPriority("MEDIUM");
				// createdAt/updatedAtはTask側の@PrePersistで自動設定されるため、ここで明示的にセットする必要はない。
				newTask.setStartDate(LocalDate.now());
				newTask.setDueDate(LocalDate.now().plusDays(3)); // 例: 3日後を期限とする

				taskRepository.save(newTask);

				LocalDateTime nextDate = calculateNextExecutionDate(rule.getNextExecutionDate(), rule.getFrequency());
				rule.setNextExecutionDate(nextDate);
				recurringRuleRepository.save(rule);
			}
		}
	}

	private void validateFrequency(String frequency) {
		if (!VALID_FREQUENCIES.contains(frequency)) {
			throw new IllegalArgumentException("不正な繰り返し頻度です: " + frequency);
		}
	}

	private LocalDateTime calculateNextExecutionDate(LocalDateTime current, String frequency) {
		switch (frequency.toUpperCase()) {
		case "DAILY":
			return current.plusDays(1);
		case "WEEKLY":
			return current.plusWeeks(1);
		case "MONTHLY":
			return current.plusMonths(1);
		default:
			return current.plusDays(1);
		}
	}
}
