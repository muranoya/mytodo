app_id := "net.meshpeak.mytodo"
main_activity := app_id + "/.MainActivity"
avd := "Medium_Phone_API_36.1"

# レシピ一覧を表示する（引数なしで just を実行したときの既定動作）
default:
    @just --list

# JVM 上のユニットテストを実行する
test:
    ./gradlew :app:testDebugUnitTest

# Lint とユニットテストを 1 回の Gradle 起動でまとめて実行する（CI の品質ゲート）
check:
    ./gradlew :app:lintDebug :app:testDebugUnitTest

# リリース APK をビルドする
build-release:
    ./gradlew :app:assembleRelease

# Gradle のビルド成果物（app/build, build 等）を全削除する
clean:
    ./gradlew clean

# AVD を起動してブート完了を待つ（run / connected-test の前段で使う内部レシピ）
_emulator:
    #!/usr/bin/env bash
    set -euo pipefail
    if adb devices | awk 'NR>1 && $2=="device"{found=1} END{exit !found}'; then
        echo "device already online"
        exit 0
    fi
    sdk="${ANDROID_HOME:-$HOME/Android/Sdk}"
    echo "starting AVD {{avd}}..."
    nohup "$sdk/emulator/emulator" -avd {{avd}} >/dev/null 2>&1 &
    adb wait-for-device
    until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
        sleep 2
    done
    echo "device ready"

# エミュレータ起動 → Debug APK をビルド/インストール → アプリを起動
run: _emulator
    ./gradlew :app:installDebug
    adb shell am start -n {{main_activity}}

# エミュレータ上で androidTest（Room DAO・マイグレーション等の実機テスト）を実行
connected-test: _emulator
    ./gradlew :app:connectedDebugAndroidTest
