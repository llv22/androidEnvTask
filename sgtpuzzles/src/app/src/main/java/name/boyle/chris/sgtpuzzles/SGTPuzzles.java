package name.boyle.chris.sgtpuzzles;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.game.rl.RLTask;

import java.util.Arrays;

public class SGTPuzzles extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		RLTask.get().onCreateActivity(this);
		super.onCreate(savedInstanceState);
		if (RLTask.get().isEnabled()) {
			String gameId = RLTask.get().get("gameId", "");
			if (gameId.isEmpty()) {
				Log.w(getClass().getSimpleName(), "Ignore invalid empty gameId '" + gameId + "'!");
				finish();
				return;
			}
			String[] games = getResources().getStringArray(R.array.games);
			if (!Arrays.asList(games).contains(gameId)) {
				Log.w(getClass().getSimpleName(), "Ignore invalid unknown gameId '" + gameId + "'!");
				finish();
				return;
			}
			final String params = RLTask.get().get("params", "");
			if (!params.isEmpty()) {
				gameId += ":" + params;
			}
			Intent intent = new Intent(this, GamePlay.class);
			intent.setData(Uri.fromParts(GamePlay.OUR_SCHEME, gameId, null));
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
			return;
		}
		final Intent intent = getIntent();
		intent.setClass(this, GamePlay.class);
		TaskStackBuilder.create(this)
				.addNextIntentWithParentStack(intent)
				.startActivities();
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		RLTask.get().onNewIntentActivity(intent);
		super.onNewIntent(intent);
	}
}
