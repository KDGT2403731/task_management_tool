package com.example.taskmanagementtool.entity;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "teams")
@Getter
@Setter
@ToString(exclude = { "teamMembers", "projects" })
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 255)
	private String name;

	// チームに所属するユーザーたち（OneToMany）
	// user側の「private Team team;」に対応
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<TeamMember> teamMembers;

	// チームに紐づくプロジェクトたち（OneToMany）
	@OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Project> projects;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Team other)) {
			return false;
		}
		return id != null && id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}