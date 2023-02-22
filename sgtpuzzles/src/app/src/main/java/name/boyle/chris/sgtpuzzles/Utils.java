package name.boyle.chris.sgtpuzzles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

abstract class Utils {

	static String readAllOf(InputStream s) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s), 8096);
		String line;
		StringBuilder log = new StringBuilder();
		while ((line = bufferedReader.readLine()) != null) {
			log.append(line);
			log.append("\n");
		}
		return log.toString();
	}

	static void closeQuietly(@Nullable Closeable c)
	{
		if (c == null) return;
		try {
			c.close();
		} catch (IOException ignored) {}
	}

	static void setExecutable(File executablePath) throws IOException {
		if (!executablePath.setExecutable(true)) {
			throw new IOException("Can't make game binary executable: File.setExecutable failed");
		}
	}

	static int waitForProcess(Process process) {
		if (process == null) return -1;
		try {
			while (true) {
				try {
					return process.waitFor();
				} catch (InterruptedException ignored) {}
			}
		} finally {
			process.destroy();
		}
	}

	@SuppressLint("CommitPrefEdits")
	static void toastFirstFewTimes(Context context, SharedPreferences state, String prefID, int showCount, int messageID) {
		long seen = state.getLong(prefID, 0);
		if (seen < showCount) {
			Toast.makeText(context, messageID, Toast.LENGTH_SHORT).show();
		}
		seen++;
		SharedPreferences.Editor ed = state.edit();
		ed.putLong(prefID, seen);
		ed.apply();
	}
}
