-- 既存テーブルを一旦削除（開発中の再実行を想定。依存関係があるためCASCADE指定）
DROP TABLE IF EXISTS integrations CASCADE;
DROP TABLE IF EXISTS recurring_rules CASCADE;
DROP TABLE IF EXISTS task_dependencies CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS milestones CASCADE;
DROP TABLE IF EXISTS project_members CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS team_members CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS teams CASCADE;

CREATE TABLE teams (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    team_id         BIGINT REFERENCES teams(id),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    sso_provider    VARCHAR(50),
    sso_token       TEXT
);

CREATE TABLE team_members (
    team_id   BIGINT NOT NULL REFERENCES teams(id),
    user_id   BIGINT NOT NULL REFERENCES users(id),
    role      VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP   NOT NULL,
    PRIMARY KEY (team_id, user_id)
);

CREATE TABLE projects (
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT NOT NULL REFERENCES users(id),
    team_id     BIGINT NOT NULL REFERENCES teams(id),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE TABLE project_members (
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id    BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (project_id, user_id)
);

CREATE TABLE milestones (
    id          BIGSERIAL PRIMARY KEY,
    project_id  BIGINT NOT NULL REFERENCES projects(id),
    title       VARCHAR(255) NOT NULL,
    target_date DATE,
    status      VARCHAR(50)  NOT NULL,
    description TEXT
);

CREATE TABLE tasks (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES projects(id),
    parent_task_id  BIGINT REFERENCES tasks(id),
    milestone_id    BIGINT REFERENCES milestones(id),
    assignee_id     BIGINT REFERENCES users(id),
    created_by      BIGINT NOT NULL REFERENCES users(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(50)  NOT NULL,
    priority        VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    start_date      DATE,
    due_date        DATE,
    completed_at    TIMESTAMP,
    plan_hours      INTEGER,
    actual_hours    INTEGER
);

CREATE TABLE task_dependencies (
    preceding_task_id  BIGINT NOT NULL REFERENCES tasks(id),
    succeeding_task_id BIGINT NOT NULL REFERENCES tasks(id),
    dependency_type    VARCHAR(50),
    lag_days           INTEGER,
    PRIMARY KEY (preceding_task_id, succeeding_task_id)
);


CREATE TABLE recurring_rules (
    id                   BIGSERIAL PRIMARY KEY,
    project_id           BIGINT NOT NULL REFERENCES projects(id),
    assignee_id          BIGINT REFERENCES users(id),
    template_title       VARCHAR(255) NOT NULL,
    template_description TEXT,
    frequency            VARCHAR(50)  NOT NULL,
    cron_expression      VARCHAR(100),
    end_date             DATE,
    next_execution_date  TIMESTAMP,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE
);


CREATE TABLE integrations (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    service_name            VARCHAR(100) NOT NULL,
    api_endpoint            TEXT,
    access_token_encrypted  TEXT,
    refresh_token_encrypted TEXT,
    expires_at              TIMESTAMP,
    CONSTRAINT uq_integrations_user_service UNIQUE (user_id, service_name)
);
