import argparse
from android_env import loader

def config():
    parser = argparse.ArgumentParser()
    # parser.add_argument('--avd_name', type=str, default='Pixel_XL_UpsideDownCake')
    parser.add_argument('--avd_name', type=str, default='Pixel_2_API_29')
    parser.add_argument('--android_avd_home', type=str, default='/Users/llv23/.android/avd')
    parser.add_argument('--android_sdk_root', type=str, default='/Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx')
    parser.add_argument('--emulator_path', type=str, default='/Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/emulator/emulator')
    parser.add_argument('--adb_path', type=str, default='/Users/llv23/Documents/00_dev_engineering/07_android/android-sdk-macosx/platform-tools/adb')
    parser.add_argument('--task_path', type=str, default='/Users/llv23/Documents/OneDrive/PhD/02_research/guided-dialog/04_flow_to_action/1_AndroidEnv/0_tasks/mdp/mdp_0000.textproto')
    return parser.parse_args()


if __name__ == "__main__":
    args = config()
    env = loader.load(
        avd_name=args.avd_name,
        android_avd_home=args.android_avd_home,
        android_sdk_root=args.android_sdk_root,
        emulator_path=args.emulator_path,
        adb_path=args.adb_path,
        task_path=args.task_path,
    )