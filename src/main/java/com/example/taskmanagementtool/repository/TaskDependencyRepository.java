package com.example.taskmanagementtool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.TaskDependency;
import com.example.taskmanagementtool.entity.TaskDependencyId;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, TaskDependencyId> {
	List<TaskDependency> findBySucceedingTaskId(Long succeedingTaskId); // 特定のタスクの前に完了すべきタスクを探す

	List<TaskDependency> findByPrecedingTaskId(Long precedingTaskId); // 特定のタスクに依存している後続タスクを探す

	// プロジェクト単位で依存関係一覧を取得（ガントチャート用）。
	// 依存関係は同一プロジェクト内のタスク同士にのみ成立する前提（Serviceで検証）のため、
	// precedingTask側のprojectで絞り込めば十分。
	List<TaskDependency> findByPrecedingTaskProjectId(Long projectId);
}
