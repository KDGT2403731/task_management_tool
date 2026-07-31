package com.example.taskmanagementtool.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taskmanagementtool.entity.SystemSettings;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {
}