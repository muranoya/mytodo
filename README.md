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

## 開発フロー

すべてのタスクは `just` に集約してある。

## リリース手順

リリースは GitHub Actions の [`Release` ワークフロー](./.github/workflows/release.yml) を手動起動（`workflow_dispatch`）して行う。

## ライセンス

Apache License 2.0。詳細は [`LICENSE`](./LICENSE) を参照。
