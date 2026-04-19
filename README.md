# mytodo

Android ネイティブのシンプルな TODO アプリ。単一ユーザー・オフライン動作・片手操作を前提とし、Jetpack Compose + Material 3（Dynamic Color / ダークモードファースト）でモダンな UI を提供する。

設計方針・アーキテクチャ・ドメインモデルの詳細は [`CLAUDE.md`](./CLAUDE.md) を参照。本 README は「動かし方」と「リリースの仕方」に特化している。

## 主な機能

- フォルダ単位で管理する TODO（TODO は必ずいずれかのフォルダに属する）
- 優先度 5 段階: すぐやる / 今日やる / 明日やる / 今週中 / そのうち
- 全フォルダ横断で未完了 TODO を優先度順に表示するアクティブ一覧画面
- 右スワイプで完了 / 左スワイプで削除、スナックバーから即 Undo
- 長押し＋ドラッグで TODO・フォルダを任意順に並び替え
- ゴミ箱で 30 日保持し、WorkManager が定期的に物理削除

## 技術スタック

| 領域 | 採用 |
| --- | --- |
| 言語 / JVM | Kotlin / JDK 17 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| 永続化 | Room（KSP） |
| 非同期 | Kotlin Coroutines + Flow |
| ナビゲーション | Navigation Compose |
| バックグラウンド | WorkManager |
| ビルド | Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`) + `dependencyLocking` |
| minSdk / targetSdk / compileSdk | 29 / 36 / 36 |

## 必要な環境

- JDK 17（Temurin 推奨）
- Android SDK（`ANDROID_HOME` 環境変数、未設定時は `$HOME/Android/Sdk` が使われる）
- [`just`](https://github.com/casey/just)（ローカル / CI で共通のタスクランナー）
- エミュレータ実行用に AVD 名 `Medium_Phone_API_36.1` を用意（別名を使う場合は `justfile` 冒頭の `avd` 定数を編集する）

## 開発フロー

すべてのタスクは `just` に集約してある。`justfile` を唯一の真実とし、ローカルと CI で同一コマンドを使う。

| コマンド | 実行内容 |
| --- | --- |
| `just` | サブコマンド一覧 |
| `just version` | `VERSION` の内容を表示 |
| `just lint` | `./gradlew :app:lintDebug` |
| `just test` | `./gradlew :app:testDebugUnitTest` |
| `just check` | `just lint` → `just test`（CI と同一） |
| `just build-release` | Release APK をビルド（署名環境変数があれば署名） |
| `just emulator` | AVD を起動し `boot_completed` まで待機 |
| `just run` | `emulator` → `installDebug` → アプリ起動 |
| `just connected-test` | `emulator` → `connectedDebugAndroidTest` |

CI が走らせているのは `just check` なので、手元でこれが通ればまず CI も通る。

## CI

[`.github/workflows/ci.yml`](./.github/workflows/ci.yml) が `main` への push と PR で `just check` を実行する。JDK 17（Temurin）+ Gradle セットアップ + `just` セットアップのみのシンプル構成。

## リリース手順

リリースは GitHub Actions の [`Release` ワークフロー](./.github/workflows/release.yml) を手動起動（`workflow_dispatch`）して行う。

### 1. バージョンを上げる

`VERSION` ファイルを semver (`MAJOR.MINOR.PATCH`) で更新し、commit & push する。各要素は 0..99 の範囲でなければならない。

```sh
echo "0.2.0" > VERSION
git add VERSION
git commit -m "bump version to 0.2.0"
git push
```

`app/build.gradle.kts` が `VERSION` を読み取り、`versionName` にそのまま、`versionCode` は `major*10000 + minor*100 + patch` に変換して設定する（例: `0.2.0` → `versionCode = 200`）。

### 2. Release ワークフローを起動

GitHub の Actions タブ → `Release` → **Run workflow** を押す。`dry_run` の扱いは次のとおり。

- `dry_run: true` — APK をビルドして workflow アーティファクトとしてアップロードするのみ。タグや GitHub Release は作成しない。署名や keystore 周りの検証に使う。
- `dry_run: false`（既定）— 以下を自動で行う。
  1. `just check` を実行
  2. タグ `v<VERSION>` が存在しないことを確認（存在したら失敗）
  3. 署名用シークレット 4 種が揃っていることを検証
  4. `just build-release` で署名済み APK を生成
  5. `mytodo-v<VERSION>.apk` にリネーム
  6. GitHub Release（自動生成リリースノート付き）を作成し、APK を添付

同じバージョンで 2 回目のリリースはできない。再リリースしたい場合は `VERSION` をインクリメントする。

### 3. リリースに必要な GitHub Secrets

次の 4 つを必ずリポジトリの **Settings → Secrets and variables → Actions** に登録しておく。1 つでも欠けるとワークフローが `Verify signing secrets are present` ステップで失敗する。

| Secret 名 | 内容 |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | 署名用 keystore (`.jks`) を `base64` エンコードした文字列 |
| `RELEASE_KEYSTORE_PASSWORD` | keystore のパスワード |
| `RELEASE_KEY_ALIAS` | 署名に使うキーのエイリアス |
| `RELEASE_KEY_PASSWORD` | エイリアスのパスワード |

keystore を base64 化する例:

```sh
base64 -w0 release.jks   # Linux
base64 -i release.jks    # macOS
```

### 4. ローカルで署名済み APK をビルドする（任意）

CI に出す前に手元で確認したい場合は、次の 4 つの環境変数を設定してから `just build-release` を実行する。

```sh
export RELEASE_KEYSTORE_PATH=/absolute/path/to/release.jks
export RELEASE_KEYSTORE_PASSWORD=...
export RELEASE_KEY_ALIAS=...
export RELEASE_KEY_PASSWORD=...
just build-release
```

生成物は `app/build/outputs/apk/release/app-release.apk`。環境変数が未設定の場合、`app/build.gradle.kts` の `signingConfigs` ブロックが条件分岐して **未署名ビルド**になる（インストール不可）。

## ライセンス

Apache License 2.0。詳細は [`LICENSE`](./LICENSE) を参照。
