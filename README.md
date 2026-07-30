# 業務効率化タスク・プロジェクト管理ツール

個人のスケジュールやチームのプロジェクト進行を可視化する、業務効率化タスク・プロジェクト管理ツールです。

## 概要

スケジュール管理に課題を感じている個人や小規模チームを対象に、タスクの登録・進捗管理・ガントチャート表示・チーム管理などをワンストップで行えるWebアプリケーションです。

## 主な機能

- **ユーザー管理**
  - メールアドレスによるログイン・サインアップ（Spring Securityによる認証）
  - ロール制御（管理者 / 一般メンバー / ゲスト）
- **タスク管理**
  - タスクの作成・編集・削除（担当者、優先度、期限、工数の見積もりと実績）
  - 繰り返しタスクの自動生成（`@Scheduled`によるルーティン業務対応）
  - ドラッグ＆ドロップ対応のカンバンボード
- **プロジェクト管理**
  - プロジェクト作成・メンバー管理（同一チーム内メンバーの追加/削除）
  - ガントチャートによるスケジュール可視化
  - マイルストーン管理（達成状況のステータス管理）
  - プロジェクト別・メンバー別の工数集計レポート
- **管理者機能**
  - ユーザー・チーム・部署の管理
  - システム全体のダッシュボード（ユーザー数、稼働プロジェクト数など）
- **ゲスト機能**
  - 閲覧専用のタスク一覧・ガントチャート
- **外部連携**
  - Slack / Teams / Google Calendarとの連携設定画面

## 技術スタック

| 分類 | 技術 |
| --- | --- |
| バックエンド | Java 17, Spring Boot |
| 認証 | Spring Security |
| DB アクセス | Spring Data JPA (Hibernate) |
| データベース | PostgreSQL |
| フロントエンド | Thymeleaf（サーバーサイドレンダリング） |
| ビルドツール | Gradle |
| その他 | Lombok |

## 画面構成

| 画面 | パス | アクセス権限 |
| --- | --- | --- |
| トップ | `/` | 制限なし |
| ログイン・サインアップ | `/login`, `/signup` | 制限なし |
| メンバーダッシュボード | `/dashboard` | 一般メンバー |
| プロジェクト一覧 | `/projects` | 一般メンバー |
| タスク一覧・カンバン | `/projects/:projectId/tasks` | 一般メンバー |
| ガントチャート | `/projects/:projectId/tasks/gantt` | 一般メンバー |
| 分析レポート | `/projects/:projectId/reports` | 一般メンバー |
| 管理者ダッシュボード | `/admin/dashboard` | 管理者 |
| ユーザー / チーム管理 | `/admin/users`, `/admin/teams` | 管理者 |
| ゲストダッシュボード | `/guest/dashboard` | ゲスト |

## セットアップ

### 前提条件

- Java 17以上
- PostgreSQL 17
- Gradle

### 環境変数

```
APP_ENCRYPTION_SECRET_KEY=<暗号化用シークレットキー>
DB_PASSWORD=<データベースパスワード>
```

### 起動手順

```bash
# リポジトリをクローン
git clone https://github.com/KDGT2403731/task_management_tool.git
cd task_management_tool

# 開発環境で起動（初回スキーマ・シードデータ投入あり）
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## アーキテクチャ

- Controller / Service / Repository の3層構造
- Controllerは薄く保ち、ビジネスロジックは`@Transactional`なServiceに集約
- コンストラクタインジェクションを採用
- ID基準の`equals`/`hashCode`実装により、双方向関連（User⇔Project、Team⇔TeamMemberなど）での循環参照を回避

## 今後の課題

- 外部サービス連携（Slack / Teams / Google Calendar）のOAuthフロー実装
- AIアシスタント連携（サブタスク分解・工数見積もり提案）
- マイクロサービス化を見据えたAPI設計の拡張