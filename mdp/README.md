<!-- markdownlint-disable-file MD029 -->
# Loader of AndroidEnv

## [Vokram (MDP)](https://github.com/deepmind/android_env/blob/main/docs/example_tasks.md#vokram)

1. APK: ~/Documents/OneDrive/PhD/02_research/guided-dialog/04_flow_to_action/1_AndroidEnv/0_tasks/mdp'
2. Loader

issue 1:

```bash
Exception has occurred: ImportError
cannot import name 'builder' from 'google.protobuf.internal' (/Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/__init__.py)
  File "/Users/llv23/Documents/OneDrive/PhD/02_research/guided-dialog/04_flow_to_action/1_code/1_loader/Vokram.py", line 2, in <module>
    from android_env import loader
ImportError: cannot import name 'builder' from 'google.protobuf.internal' (/Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/__init__.py)
```

Adjust pip library protobuf==3.19.6, then fix the issue [builder.py](https://github.com/protocolbuffers/protobuf/blob/main/python/google/protobuf/internal/builder.py) issue, by copy it to /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/builder-3.20.3.py
then ln -s /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/builder-3.20.3.py /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/builder.py.

```bash
cd /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal
wget https://raw.githubusercontent.com/protocolbuffers/protobuf/main/python/google/protobuf/internal/builder.py builder-3.20.3.py
ln -s /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/builder-3.20.3.py /Users/llv23/opt/miniconda3/lib/python3.8/site-packages/google/protobuf/internal/builder.py
```

3. Run emulator

Run human agent:

```bash
python /Users/llv23/Documents/05_machine_learning/04_phd/4_flow_action/android_env/examples/run_human_agent.py --avd_name Pixel_XL_UpsideDownCake --android_avd_home /Users/llv23/.android/avd --android_sdk_root /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx --emulator_path /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/emulator/emulator --adb_path /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/platform-tools/adb --task_path /Users/llv23/Documents/OneDrive/PhD/02_research/guided-dialog/04_flow_to_action/1_AndroidEnv/0_tasks/mdp/mdp_0000.textproto
```

Run acme agent:

```bash
pip install dm_acme==0.2.4
python /Users/llv23/Documents/05_machine_learning/04_phd/4_flow_action/android_env/examples/run_acme_agent.py --avd_name Pixel_XL_UpsideDownCake --android_avd_home /Users/llv23/.android/avd --android_sdk_root /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx --emulator_path /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/emulator/emulator --adb_path /Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/platform-tools/adb --task_path /Users/llv23/Documents/OneDrive/PhD/02_research/guided-dialog/04_flow_to_action/1_AndroidEnv/0_tasks/mdp/mdp_0000.textproto
```
