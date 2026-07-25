# タスク・プロジェクト管理ツール
**タスク・プロジェクト管理ツール**は、個人のスケジュールやチームのプロジェクト進行を可視化するツールです。
## ディレクトリ構成
```
.
├── bin
│   ├── default
│   ├── generated-sources
│   │   └── annotations
│   ├── generated-test-sources
│   │   └── annotations
│   ├── main
│   │   ├── application-dev.properties
│   │   ├── application.properties
│   │   ├── com
│   │   │   ├── example
│   │   │   │   └── taskmanagementtool
│   │   │   │       ├── config
│   │   │   │       │   └── SecurityConfig.class
│   │   │   │       ├── controller
│   │   │   │       │   ├── AdminController.class
│   │   │   │       │   ├── AuthController.class
│   │   │   │       │   ├── AuthController$Signup.class
│   │   │   │       │   ├── DashboardController.class
│   │   │   │       │   ├── GuestController.class
│   │   │   │       │   ├── ProjectController.class
│   │   │   │       │   ├── ReportController.class
│   │   │   │       │   ├── SettingsController.class
│   │   │   │       │   └── TaskController.class
│   │   │   │       ├── entity
│   │   │   │       │   ├── Integration.class
│   │   │   │       │   ├── Milestone.class
│   │   │   │       │   ├── Project.class
│   │   │   │       │   ├── RecurringRule.class
│   │   │   │       │   ├── Task.class
│   │   │   │       │   ├── TaskDependency.class
│   │   │   │       │   ├── TaskDependencyId.class
│   │   │   │       │   ├── Team.class
│   │   │   │       │   ├── TeamMember.class
│   │   │   │       │   ├── TeamMemberId.class
│   │   │   │       │   └── User.class
│   │   │   │       ├── repository
│   │   │   │       │   ├── IntegrationRepository.class
│   │   │   │       │   ├── MilestoneRepository.class
│   │   │   │       │   ├── ProjectRepository.class
│   │   │   │       │   ├── RecurringRuleRepository.class
│   │   │   │       │   ├── TaskDependencyRepository.class
│   │   │   │       │   ├── TaskRepository.class
│   │   │   │       │   ├── TeamMemberRepository.class
│   │   │   │       │   ├── TeamRepository.class
│   │   │   │       │   └── UserRepository.class
│   │   │   │       ├── service
│   │   │   │       │   ├── AuthService.class
│   │   │   │       │   ├── CustomUserDetailsService.class
│   │   │   │       │   ├── IntegrationService.class
│   │   │   │       │   ├── MilestoneService.class
│   │   │   │       │   ├── ProjectService.class
│   │   │   │       │   ├── RecurringRuleService.class
│   │   │   │       │   ├── ReportService.class
│   │   │   │       │   ├── ReportService$MemberHoursSummary.class
│   │   │   │       │   ├── TaskDependencyService.class
│   │   │   │       │   ├── TaskService.class
│   │   │   │       │   ├── TeamService.class
│   │   │   │       │   ├── TokenEncryptionService.class
│   │   │   │       │   └── UserService.class
│   │   │   │       └── TaskmanagementtoolApplication.class
│   │   │   └── example 3
│   │   ├── data.sql
│   │   ├── META-INF
│   │   │   └── additional-spring-configuration-metadata.json
│   │   ├── schema.sql
│   │   ├── static
│   │   │   └── css
│   │   │       └── style.css
│   │   └── templates
│   │       ├── admin
│   │       │   ├── dashboard.html
│   │       │   ├── system.html
│   │       │   ├── teams
│   │       │   │   ├── create.html
│   │       │   │   ├── detail.html
│   │       │   │   └── list.html
│   │       │   └── users
│   │       │       ├── detail.html
│   │       │       └── list.html
│   │       ├── dashboard.html
│   │       ├── guest
│   │       │   ├── dashboard.html
│   │       │   ├── gantt.html
│   │       │   └── tasks.html
│   │       ├── index.html
│   │       ├── login.html
│   │       ├── milestone
│   │       │   ├── create.html
│   │       │   ├── detail.html
│   │       │   ├── edit.html
│   │       │   └── list.html
│   │       ├── project
│   │       │   ├── create.html
│   │       │   ├── detail.html
│   │       │   ├── edit.html
│   │       │   ├── list.html
│   │       │   └── reports.html
│   │       ├── settings
│   │       │   └── integration.html
│   │       ├── signup.html
│   │       └── task
│   │           ├── create.html
│   │           ├── detail.html
│   │           ├── gantt.html
│   │           ├── list.html
│   │           └── recurring.html
│   └── test
│       └── com
│           ├── example
│           │   └── taskmanagementtool
│           │       └── TaskmanagementtoolApplicationTests.class
│           └── example 3
├── build
│   ├── classes
│   │   └── java
│   │       ├── main
│   │       │   └── com
│   │       │       └── example
│   │       │           └── taskmanagementtool
│   │       │               ├── config
│   │       │               │   └── SecurityConfig.class
│   │       │               ├── controller
│   │       │               │   ├── AdminController.class
│   │       │               │   ├── AuthController.class
│   │       │               │   ├── AuthController$Signup.class
│   │       │               │   ├── DashboardController.class
│   │       │               │   ├── GuestController.class
│   │       │               │   ├── MilestoneController.class
│   │       │               │   ├── ProjectController.class
│   │       │               │   ├── ReportController.class
│   │       │               │   ├── SettingsController.class
│   │       │               │   └── TaskController.class
│   │       │               ├── entity
│   │       │               │   ├── Integration.class
│   │       │               │   ├── Milestone.class
│   │       │               │   ├── Project.class
│   │       │               │   ├── RecurringRule.class
│   │       │               │   ├── Task.class
│   │       │               │   ├── TaskDependency.class
│   │       │               │   ├── TaskDependencyId.class
│   │       │               │   ├── Team.class
│   │       │               │   ├── TeamMember.class
│   │       │               │   ├── TeamMemberId.class
│   │       │               │   └── User.class
│   │       │               ├── repository
│   │       │               │   ├── IntegrationRepository.class
│   │       │               │   ├── MilestoneRepository.class
│   │       │               │   ├── ProjectRepository.class
│   │       │               │   ├── RecurringRuleRepository.class
│   │       │               │   ├── TaskDependencyRepository.class
│   │       │               │   ├── TaskRepository.class
│   │       │               │   ├── TeamMemberRepository.class
│   │       │               │   ├── TeamRepository.class
│   │       │               │   └── UserRepository.class
│   │       │               ├── service
│   │       │               │   ├── AuthService.class
│   │       │               │   ├── IntegrationService.class
│   │       │               │   ├── MilestoneService.class
│   │       │               │   ├── ProjectService.class
│   │       │               │   ├── ReportService.class
│   │       │               │   ├── ReportService$MemberHoursSummary.class
│   │       │               │   ├── TaskService.class
│   │       │               │   ├── TeamService.class
│   │       │               │   ├── TokenEncryptionService.class
│   │       │               │   └── UserService.class
│   │       │               └── TaskmanagementtoolApplication.class
│   │       └── test
│   │           └── com
│   │               └── example
│   │                   └── taskmanagementtool
│   │                       └── TaskmanagementtoolApplicationTests.class
│   ├── reports
│   │   └── problems
│   │       └── problems-report.html
│   └── resources
│       ├── main
│       │   ├── application.properties
│       │   ├── data.sql
│       │   ├── META-INF
│       │   │   └── additional-spring-configuration-metadata.json
│       │   ├── schema.sql
│       │   ├── static
│       │   │   └── css
│       │   │       └── style.css
│       │   └── templates
│       └── test
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── HELP.md
├── settings.gradle
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── taskmanagementtool
    │   │               ├── config
    │   │               │   └── SecurityConfig.java
    │   │               ├── controller
    │   │               │   ├── AdminController.java
    │   │               │   ├── AuthController.java
    │   │               │   ├── DashboardController.java
    │   │               │   ├── GuestController.java
    │   │               │   ├── ProjectController.java
    │   │               │   ├── ReportController.java
    │   │               │   ├── SettingsController.java
    │   │               │   └── TaskController.java
    │   │               ├── entity
    │   │               │   ├── Integration.java
    │   │               │   ├── Milestone.java
    │   │               │   ├── Project.java
    │   │               │   ├── RecurringRule.java
    │   │               │   ├── Task.java
    │   │               │   ├── TaskDependency.java
    │   │               │   ├── TaskDependencyId.java
    │   │               │   ├── Team.java
    │   │               │   ├── TeamMember.java
    │   │               │   ├── TeamMemberId.java
    │   │               │   └── User.java
    │   │               ├── repository
    │   │               │   ├── IntegrationRepository.java
    │   │               │   ├── MilestoneRepository.java
    │   │               │   ├── ProjectRepository.java
    │   │               │   ├── RecurringRuleRepository.java
    │   │               │   ├── TaskDependencyRepository.java
    │   │               │   ├── TaskRepository.java
    │   │               │   ├── TeamMemberRepository.java
    │   │               │   ├── TeamRepository.java
    │   │               │   └── UserRepository.java
    │   │               ├── service
    │   │               │   ├── AuthService.java
    │   │               │   ├── CustomUserDetailsService.java
    │   │               │   ├── IntegrationService.java
    │   │               │   ├── MilestoneService.java
    │   │               │   ├── ProjectService.java
    │   │               │   ├── RecurringRuleService.java
    │   │               │   ├── ReportService.java
    │   │               │   ├── TaskDependencyService.java
    │   │               │   ├── TaskService.java
    │   │               │   ├── TeamService.java
    │   │               │   ├── TokenEncryptionService.java
    │   │               │   └── UserService.java
    │   │               └── TaskmanagementtoolApplication.java
    │   └── resources
    │       ├── application-dev.properties
    │       ├── application.properties
    │       ├── data.sql
    │       ├── META-INF
    │       │   └── additional-spring-configuration-metadata.json
    │       ├── schema.sql
    │       ├── static
    │       │   └── css
    │       │       └── style.css
    │       └── templates
    │           ├── admin
    │           │   ├── dashboard.html
    │           │   ├── system.html
    │           │   ├── teams
    │           │   │   ├── create.html
    │           │   │   ├── detail.html
    │           │   │   └── list.html
    │           │   └── users
    │           │       ├── detail.html
    │           │       └── list.html
    │           ├── dashboard.html
    │           ├── guest
    │           │   ├── dashboard.html
    │           │   ├── gantt.html
    │           │   └── tasks.html
    │           ├── index.html
    │           ├── login.html
    │           ├── milestone
    │           │   ├── create.html
    │           │   ├── detail.html
    │           │   ├── edit.html
    │           │   └── list.html
    │           ├── project
    │           │   ├── create.html
    │           │   ├── detail.html
    │           │   ├── edit.html
    │           │   ├── list.html
    │           │   └── reports.html
    │           ├── settings
    │           │   └── integration.html
    │           ├── signup.html
    │           └── task
    │               ├── create.html
    │               ├── detail.html
    │               ├── gantt.html
    │               ├── list.html
    │               └── recurring.html
    └── test
        └── java
            └── com
                └── example
                    └── taskmanagementtool
                        └── TaskmanagementtoolApplicationTests.java
```
