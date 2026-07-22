package com.example.taskmanagementtool.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
	// 特定のプロジェクトに属するタスクを一覧取得（カンバンやガントチャート用）
	List<Task> findByProjectId(Long projectId);

	// 特定の担当者に割り当てられたタスクを一覧取得（メンバーダッシュボード用）
	List<Task> findByAssigneeId(Long assigneeId);

	// 特定のマイルストーンに紐づくタスクを一覧取得 
	List<Task> findByMilestoneId(Long milestoneId);

	// 特定のユーザーが作成したタスクを一覧取得（ユーザー削除時のFK整合性チェック用）
	List<Task> findByCreatedById(Long createdById);
}
