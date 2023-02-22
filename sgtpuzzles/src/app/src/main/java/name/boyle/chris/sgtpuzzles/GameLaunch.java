package name.boyle.chris.sgtpuzzles;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A game the user wants to launch, whether saved, or identified by backend and
 * optional parameters.
 */
@SuppressWarnings("WeakerAccess")
public class GameLaunch {

	@Nullable
	private String saved;

	@Nullable
	private final Uri uri;
	@Nullable
	private final String whichBackend;
	@Nullable
	private final String params;
	@Nullable
	private final String gameID;
	@Nullable
	private final String seed;
	private final boolean fromChooser;
	private final boolean ofLocalState;
	private final boolean undoingOrRedoing;

	private GameLaunch(@Nullable final String whichBackend,
					   @Nullable final String params,
					   @Nullable final String gameID,
					   @Nullable final String seed,
					   @Nullable final Uri uri,
					   @Nullable final String saved,
					   final boolean fromChooser,
					   final boolean ofLocalState,
					   boolean undoingOrRedoing) {
		this.whichBackend = whichBackend;
		this.params = params;
		this.gameID = gameID;
		this.seed = seed;
		this.uri = uri;
		this.saved = saved;
		this.fromChooser = fromChooser;
		this.ofLocalState = ofLocalState;
		this.undoingOrRedoing = undoingOrRedoing;
	}

	@NonNull
	@Override
	public String toString() {
		if (uri != null) return "GameLaunch.ofUri(" + uri + ")";
		return "GameLaunch(" + whichBackend + ", " + params + ", " + gameID + ", " + seed + ", " + saved + ")";
	}

	@NonNull
	public String toStringO() {
		return GameLaunch.class.getSimpleName() + "{" +
				"saved='" + toStringO(saved) + '\'' +
				", uri=" + uri +
				", whichBackend='" + whichBackend + '\'' +
				", params='" + params + '\'' +
				", gameID='" + gameID + '\'' +
				", seed='" + seed + '\'' +
				", fromChooser=" + fromChooser +
				", ofLocalState=" + ofLocalState +
				", undoingOrRedoing=" + undoingOrRedoing +
				'}';
	}

	public static String toStringO(@Nullable String string) {
		return string == null ? null : string.substring(0, Math.min(string.length(), 33)) + "...";
	}

	public static GameLaunch ofSavedGame(@NonNull final String saved) {
		return new GameLaunch(null, null, null, null, null, saved, true, false, false);
	}

	public static GameLaunch undoingOrRedoingNewGame(@Nullable final String saved) {
		return new GameLaunch(null, null, null, null, null, saved, true, true, true);
	}

	public static GameLaunch ofLocalState(@Nullable final String backend, @Nullable final String saved, final boolean fromChooser) {
		return new GameLaunch(backend, null, null, null, null, saved, fromChooser, true, false);
	}

	public static GameLaunch toGenerate(@Nullable String whichBackend, @NonNull String params) {
		return new GameLaunch(whichBackend, params, null, null, null, null, false, false, false);
	}

	public static GameLaunch toGenerateFromChooser(@NonNull String whichBackend) {
		return new GameLaunch(whichBackend, null, null, null, null, null, true, false, false);
	}

	public static GameLaunch ofGameID(@Nullable String whichBackend, @NonNull String gameID) {
		final int pos = gameID.indexOf(':');
		if (pos < 0) throw new IllegalArgumentException("Game ID invalid: " + gameID);
		return new GameLaunch(whichBackend, gameID.substring(0, pos), gameID, null, null, null, false, false, false);
	}

	public static GameLaunch fromSeed(@Nullable String whichBackend, @NonNull String seed) {
		final int pos = seed.indexOf('#');
		if (pos < 0) throw new IllegalArgumentException("Seed invalid: " + seed);
		return new GameLaunch(whichBackend, seed.substring(0, pos), null, seed, null, null, false, false, false);
	}

	public static GameLaunch ofUri(@NonNull final Uri uri) {
		return new GameLaunch(null, null, null, null, uri, null, true, false, false);
	}

	public boolean needsGenerating() {
		return saved == null && gameID == null && uri == null;
	}

	@Nullable
	public String getWhichBackend() {
		return whichBackend;
	}

	@Nullable
	public String getParams() {
		return params;
	}

	@Nullable
	public String getGameID() {
		return gameID;
	}

	@Nullable
	public String getSeed() {
		return seed;
	}

	@Nullable
	public Uri getUri() {
		return uri;
	}

	public void finishedGenerating(@NonNull String saved) {
		if (this.saved != null) {
			throw new RuntimeException("finishedGenerating called twice");
		}
		this.saved = saved;
	}

	@Nullable
	public String getSaved() {
		return saved;
	}

	public boolean isFromChooser() {
		return fromChooser;
	}

	public boolean isOfLocalState() {
		return ofLocalState;
	}

	public boolean isUndoingOrRedoing() {
		return undoingOrRedoing;
	}
}
