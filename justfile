app_id := "net.meshpeak.mytodo"
main_activity := app_id + "/.MainActivity"
avd := "Medium_Phone_API_36.1"

default:
    @just --list

version:
    @cat VERSION

lint:
    ./gradlew :app:lintDebug

test:
    ./gradlew :app:testDebugUnitTest

check: lint test

build-release:
    ./gradlew :app:assembleRelease

emulator:
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

run: emulator
    ./gradlew :app:installDebug
    adb shell am start -n {{main_activity}}

connected-test: emulator
    ./gradlew :app:connectedDebugAndroidTest
