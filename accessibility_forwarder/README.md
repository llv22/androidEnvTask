<!-- markdownlint-disable MD029 -->
# Run sample based on accessibility_forwarder.apk

1. Enter the folder first

```bash
cd accessibility_forwarder
```

2. Run clock set with RL

```bash
python ../../android_env/examples/run_human_agent.py --avd_name Pixel_XL_UpsideDownCake --android_avd_home ~/.android/avd --android_sdk_root ${ANDROID_SDK_ROOT} --emulator_path ${ANDROID_SDK_ROOT}/emulator/emulator --adb_path ${ANDROID_SDK_ROOT}/platform-tools/adb --task_path accessibility_forwarder_clock_set_timer.textproto
```

3. Run Calculator set with RL (Invalid for current avd)

```bash
python ../../android_env/examples/run_human_agent.py --avd_name Pixel_XL_UpsideDownCake --android_avd_home ~/.android/avd --android_sdk_root ${ANDROID_SDK_ROOT} --emulator_path ${ANDROID_SDK_ROOT}/emulator/emulator --adb_path ${ANDROID_SDK_ROOT}/platform-tools/adb --task_path accessibility_forwarder_calculator_history.textproto
```

4. Enter system

```bash
python ../../android_env/examples/run_human_agent.py --avd_name Pixel_XL_UpsideDownCake --android_avd_home ~/.android/avd --android_sdk_root ${ANDROID_SDK_ROOT} --emulator_path ${ANDROID_SDK_ROOT}/emulator/emulator --adb_path ${ANDROID_SDK_ROOT}/platform-tools/adb --task_path accessibility_forwarder_com.android.settings.textproto
```
