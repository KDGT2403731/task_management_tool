package com.example.taskmanagementtool.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskmanagementtool.entity.Integration;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Long> {
	List<Integration> findByUserId(Long userId); // ユーザーの連携一覧取得用

	Optional<Integration> findByUserIdAndServiceName(Long userId, String serviceName);
}
