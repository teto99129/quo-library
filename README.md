# library-api

Spring Boot、Kotlin、および jOOQ を使用した図書管理（著者・書籍）アプリケーションのバックエンド API です。

---

## 🛠 技術スタック

* **Language**: Kotlin `2.3.21` (JDK `21`)
* **Framework**: Spring Boot `4.1.0`
* **Database**: PostgreSQL `18.4` (Docker)
* **Migration**: Flyway `12.11.0`
* **Database Access**: jOOQ `3.21.6` (KotlinGenerator を使用したタイプセーフなクエリ)
* **Testing**: Kotest `5.9.1` (DescribeSpec) + Mockito
* **Linter / Formatter**: KtLint `14.2.0` (インデント幅 `4`), Detekt `1.23.8`

---

## 📂 主要なプロジェクト構造

```
src/
├── main/
│   ├── kotlin/com/github/teto99129/library/
│   │   ├── author/             # 著者（Author）関連のドメイン
│   │   │   ├── controller/     # APIエンドポイント
│   │   │   ├── model/          # リクエスト/レスポンス/エンティティ定義
│   │   │   └── service/        # サービス・ユースケース層
│   │   ├── book/               # 書籍（Book）関連のドメイン
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   └── service/
│   │   ├── database/           # リポジトリのjOOQ実装層
│   │   │   ├── JooqAuthorRepository.kt
│   │   │   └── JooqBookRepository.kt
│   │   └── common/             # 共通処理・例外ハンドリング
│   │       ├── GlobalExceptionHandler.kt
│   │       └── exception/ResourceNotFoundException.kt
│   └── resources/
│       ├── application.yaml    # アプリケーション設定
│       └── db/migration/       # FlywayによるSQLマイグレーション定義
└── test/                       # テストコード（ControllerのMockMvcテスト、RepositoryのDB統合テスト、Serviceの単体テスト）
```

---

## 🚀 開発環境の起動方法

本プロジェクトは Docker コンテナ内で動作するように構成されています。

### 1. コンテナの起動
以下のコマンドで、アプリケーションサーバーと PostgreSQL データベースサーバーを起動します。
```bash
docker compose up -d
```

### 2. アプリケーションのビルドと起動（コンテナ内）
コンテナ内で起動します。
```bash
docker compose exec api ./gradlew bootRun
```

---

## 🧪 テスト・静的解析・コードフォーマット

コンテナ内で以下の Gradle タスクを実行できます。

### 1. 全テストの実行
Controller（Web）、Service（単体）、Repository（統合）のすべてのテストを実行します。
```bash
docker compose exec api ./gradlew test
```

### 2. コードフォーマットのチェックと自動整形 (KtLint)
プロジェクト規約（Kotlin公式に準拠した4インデント幅）に従って、ソースコードのスタイルチェックと自動整形を行います。
```bash
# スタイルの自動修正
docker compose exec api ./gradlew ktlintFormat

# スタイルのチェックのみ
docker compose exec api ./gradlew ktlintCheck
```

### 3. 静的解析の実行 (Detekt)
コードの品質（複雑度や潜在的なバグなど）の解析を実行します。
```bash
docker compose exec api ./gradlew detekt
```

---

## 📝 APIの主要な仕様

### 1. エラーハンドリング
不正なリクエスト（バリデーションエラー）やリソース不在時には、クライアントに対して分かりやすいシンプルなJSONレスポンスを返します。

* **400 Bad Request（例：未来の生年月日が指定された場合）**
  ```json
  {
    "birthday": "生年月日は過去の日付を指定してください"
  }
  ```
* **404 Not Found（例：存在しない著者IDが指定された場合）**
  ```json
  {
    "error": "Author not found with ID: 999"
  }
  ```
