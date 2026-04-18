# mytodo — プロジェクト方針

## 1. プロジェクト概要

Android ネイティブの TODO アプリ。単一ユーザー・オフライン動作・片手操作前提のモダン UI を目指す。

### 機能要件

- TODO の一覧 / 作成 / 完了 / 更新 / 削除
- フォルダ: TODO は必ずいずれかのフォルダに属する
- 優先度: **すぐやる / 今日やる / 明日やる / 今週中 / そのうち** の 5 段階。システム規定の固定ラベル集合で、ユーザーはこの中から選択するだけ
- ゴミ箱: 完了・削除した TODO を 30 日保持し、経過後に物理削除
- 並び替え: TODO / フォルダともに長押し＋ドラッグでユーザー任意に並び替え可能
- **アクティブ一覧画面**: 全フォルダ横断で未完了・未削除の TODO を優先度順に表示する専用画面を用意する（フォルダ単位の画面とは別に、トップレベルで切り替え可能）

### 非機能要件

- 端末ローカルのみ（同期なし）。将来のクラウド同期に備え Repository 層で抽象化は保つ
- 片手操作最優先。親指導線上に主要アクションを配置する
- モダンでクールな UI（Material 3 + Dynamic Color）

## 2. 技術スタック

| 領域 | 採用 |
| --- | --- |
| 言語 | Kotlin（最新安定版） |
| UI | Jetpack Compose + Material 3（Expressive motion 有効） |
| ビルド | Gradle Kotlin DSL + `gradle/libs.versions.toml`（Version Catalog） |
| minSdk / targetSdk | 29 / 最新安定 |
| 永続化 | Room（KSP 経由） |
| DI | Hilt |
| 非同期 | Kotlin Coroutines + Flow（UI state は `StateFlow`） |
| ナビゲーション | Navigation Compose（type-safe routes） |
| バックグラウンド | WorkManager（ゴミ箱 30 日経過品の物理削除） |
| テスト | JUnit4 (AndroidX) / Turbine / Compose UI Test / Robolectric（必要時） |

### バージョン指定ルール（グローバル方針の Android 版）

- すべての依存バージョンは `gradle/libs.versions.toml` に一元管理
- `*.gradle.kts` からは alias 参照のみ（ハードコード禁止）
- `gradle.lockfile` を有効化してコミット
- 更新は `./gradlew dependencies --write-locks` などで明示的に実施

## 3. アーキテクチャ

- レイヤー分離: `ui` / `domain` / `data`
- 単方向データフロー (UDF): ViewModel が `StateFlow<UiState>` を公開し、Composable は受動描画
- ViewModel は Android フレームワーク非依存に保つ（`Context` / `Resources` を直接扱わず、`@StringRes` や domain 層の列挙で返す）
- Repository はインタフェースを `domain` 層に置き、実装を `data` 層に置く

### ディレクトリ骨格

```
app/src/main/java/com/example/mytodo/
  ui/
    todo/         # フォルダ別の TODO 一覧・編集・詳細
    folder/       # フォルダ切替・管理
    overview/     # 全フォルダ横断のアクティブ TODO 一覧（優先度順）
    trash/        # ゴミ箱画面
    theme/        # MaterialTheme / Typography / Colors
    components/   # 汎用 Composable
  domain/
    model/        # Todo, Folder, Priority
    usecase/      # CompleteTodoUseCase など
    repository/   # インタフェース定義
  data/
    local/        # Room Entity / DAO / Database / Migration
    repository/   # Repository 実装
    worker/       # WorkManager のゴミ箱クリーナ
```

パッケージ名は `com.example.mytodo` を仮置き（公開前に確定）。

## 4. ドメインモデル

### Folder

```
Folder {
  id: Long
  name: String
  orderIndex: Int
  createdAt: Instant
}
```

### Todo

```
Todo {
  id: Long
  folderId: Long
  title: String
  note: String?
  priority: Priority
  isCompleted: Boolean
  orderIndex: Int
  createdAt: Instant
  completedAt: Instant?   // 完了時刻。ゴミ箱TTL計算に使用
  deletedAt: Instant?     // 論理削除時刻。ゴミ箱TTL計算に使用
}
```

### Priority

システム規定の固定 5 段階。ユーザーは候補から 1 つを選ぶだけで、ラベル追加・色カスタマイズは提供しない。`sealed interface Priority` として定義し、各要素はソート用の `rank: Int`、表示色 `Color`、ラベル `@StringRes` を保持する。配色は `ui/theme` 側で一元管理し、個別 TODO には色属性を持たせない。

```
sealed interface Priority {
  val rank: Int                 // 小さいほど緊急。SQL ソートキーとしても使用
  val color: Color              // 系統色。MaterialTheme.colorScheme から派生
  @get:StringRes val labelRes: Int
  data object Asap     : Priority  // rank = 0
  data object Today    : Priority  // rank = 1
  data object Tomorrow : Priority  // rank = 2
  data object ThisWeek : Priority  // rank = 3
  data object Someday  : Priority  // rank = 4
}
```

Room には `rank: Int` を `TypeConverter` 経由で永続化し、`ORDER BY priority_rank` を SQL 側で効かせる。

### ゴミ箱

- 論理削除: `deletedAt != null` または `isCompleted == true` を「ゴミ箱内」とみなす
- 物理削除: `deletedAt` または `completedAt` から 30 日経過した行を WorkManager が削除
- WorkManager は 1 日 1 回の `PeriodicWorkRequest`（Doze モード考慮で深夜帯に寄せる）

## 5. UI / UX 原則

### 片手操作

- 主要アクションは画面下部に集約（`FloatingActionButton` 右下、必要に応じ `BottomAppBar`）
- タップで開く編集 UI は画面上部に被せず、**`ModalBottomSheet` で下から展開**する
- 新規作成も FAB → `ModalBottomSheet` によるインライン入力で画面遷移を最小化
- 画面上部の見出し領域は装飾に留め、操作は求めない

### ジェスチャ

- 長押し＋ドラッグで並び替え（フォルダ別一覧のみ有効）
- 右スワイプで完了、左スワイプで削除（`SwipeToDismissBox`）
- 誤操作防止に完了/削除はスナックバーから即 Undo 可能にする

### 画面構成

- ボトムナビまたは上部タブでトップレベルを切り替え: **アクティブ横断 / フォルダ別 / ゴミ箱**
- アクティブ横断画面:
  - 全フォルダの未完了かつ未削除の TODO を表示
  - 優先度バケット（ASAP → そのうち）でセクション分け、セクション内は `createdAt` 昇順
  - 並び替え（ドラッグ）は無効化し、各アイテムに所属フォルダ名をサブテキストで表示
  - スワイプでの完了／削除、タップでの編集はフォルダ別画面と同一操作

### ビジュアル

- Material 3 Dynamic Color を既定で有効化、Android 12+ ではユーザー壁紙に追随
- ダークモードファースト。`MaterialTheme.colorScheme` からのみ色を引き、リテラル Color の直書きは避ける
- タイポグラフィは Material 3 type scale を採用
- 操作フィードバックに `HapticFeedback`（完了・削除・並び替え確定で軽く鳴らす）
- モーションは Material 3 Expressive のスプリング系を基本とする

## 6. コーディング規約

- Kotlin 公式スタイル、`Spotless` または `ktlint` を CI ゲートに組み込む
- Composable は状態ホイスト徹底、`@Preview` を公開 Composable ごとに最低 1 個
- ViewModel は副作用を `SharedFlow<UiEvent>` 等で一方向に流す（直接 Navigation しない）
- Room のスキーマ変更は必ず `Migration` を記述し、`MigrationTestHelper.runMigrationsAndValidate` 相当のテストを追加
- 例外を握り潰さない。`Result` 型や sealed class で失敗を UI state に載せる

## 7. リポジトリ運用

- git 操作（`add` / `commit` / `push`）はユーザーの明示指示があるまで自動で行わない（グローバル方針準拠）
- PR タイトルは日本語で簡潔に。PR 本文には「意図」と「検証手順」を含める
- ブランチ名は `feature/<topic>`, `fix/<topic>` 等のプレフィックス

## 8. 将来拡張として意識だけしておく項目

- クラウド同期（Firebase など）。Repository のインタフェースは同期実装を差し込める形を維持
- ホーム画面ウィジェット / クイックタイル
- 期限日・通知リマインダー
- 検索・タグ

現時点では実装しない。スキーマや画面遷移の設計時に「あとで入れやすい形」になっているかを確認するだけでよい。
