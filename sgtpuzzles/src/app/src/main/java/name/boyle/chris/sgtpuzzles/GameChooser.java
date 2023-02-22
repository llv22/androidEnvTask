package name.boyle.chris.sgtpuzzles;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.game.rl.RLTask;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameChooser extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	static final String CHOOSER_STYLE_KEY = "chooserStyle";
	private static final Set<String> DEFAULT_STARRED = new LinkedHashSet<>();

	static {
		DEFAULT_STARRED.add("guess");
		DEFAULT_STARRED.add("keen");
		DEFAULT_STARRED.add("lightup");
		DEFAULT_STARRED.add("net");
		DEFAULT_STARRED.add("signpost");
		DEFAULT_STARRED.add("solo");
		DEFAULT_STARRED.add("towers");
		if (RLTask.get().isEnabled()) {
			DEFAULT_STARRED.clear();
			DEFAULT_STARRED.add("blackbox");
			DEFAULT_STARRED.add("bridges");
			DEFAULT_STARRED.add("cube");
			DEFAULT_STARRED.add("dominosa");
			DEFAULT_STARRED.add("fifteen");
			// TODO DEFAULT_STARRED.add("filling"); // seems to require external input device
			DEFAULT_STARRED.add("flip");
			DEFAULT_STARRED.add("flood");
			DEFAULT_STARRED.add("galaxies");
			DEFAULT_STARRED.add("guess");
			DEFAULT_STARRED.add("inertia");
			// TODO DEFAULT_STARRED.add("keen"); // seems to require external input device
			DEFAULT_STARRED.add("lightup");
			DEFAULT_STARRED.add("loopy");
			DEFAULT_STARRED.add("net");
		}
	}

	private static final int REQ_CODE_PICKER = Activity.RESULT_FIRST_USER;

    private GridLayout table;

	private TextView starredHeader;
	private TextView otherHeader;

	private SharedPreferences prefs;
    private boolean useGrid;
	private String[] games;
	private View[] views;
	private Menu menu;
	private ScrollView scrollView;
	private int scrollToOnNextLayout = -1;
	private long resumeTime = 0L;

	@Override
    @SuppressLint("CommitPrefEdits")
    protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences state = getSharedPreferences(GamePlay.STATE_PREFS_NAME, MODE_PRIVATE);

		String oldCS = state.getString(CHOOSER_STYLE_KEY, null);
		if (oldCS != null) {  // migrate to somewhere more sensible
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString(CHOOSER_STYLE_KEY, oldCS);
			ed.apply();
			ed = state.edit();
			ed.remove(CHOOSER_STYLE_KEY);
			ed.apply();
		}

		String s = prefs.getString(CHOOSER_STYLE_KEY, "list");
		useGrid = "grid".equals(s);
		games = getResources().getStringArray(R.array.games);
		views = new View[games.length];
		setContentView(R.layout.chooser);
		table = findViewById(R.id.table);
		starredHeader = findViewById(R.id.games_starred);
		otherHeader = findViewById(R.id.games_others);
		buildViews();
		rethinkActionBarCapacity();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && getSupportActionBar() != null) {
			getSupportActionBar().addOnMenuVisibilityListener(visible -> {
				// https://code.google.com/p/android/issues/detail?id=69205
				if (!visible) supportInvalidateOptionsMenu();
			});
		}

//		if( ! state.contains("savedGame") || state.getString("savedGame", "").length() <= 0 ) {
//			// first run
//			new AlertDialog.Builder(this)
//					.setMessage(R.string....)
//					.setPositiveButton(android.R.string.ok, null)
//					.show();
//		}

		scrollView = findViewById(R.id.scrollView);
		scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
			if (scrollToOnNextLayout >= 0) {
				final View v = views[scrollToOnNextLayout];
				scrollView.requestChildRectangleOnScreen(v, new Rect(0, 0, v.getWidth(), v.getHeight()), true);
				scrollToOnNextLayout = -1;
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			enableTableAnimations();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		resumeTime = System.nanoTime();
		String currentBackend = null;
		SharedPreferences state = getSharedPreferences(GamePlay.STATE_PREFS_NAME, MODE_PRIVATE);
		if (state.contains(GamePlay.SAVED_BACKEND)) {
			currentBackend = state.getString(GamePlay.SAVED_BACKEND, null);
		}

		for (int i = 0; i < games.length; i++) {
			final View v = views[i];
			final View highlight = v.findViewById(R.id.currentGameHighlight);
			final LayerDrawable layerDrawable = (LayerDrawable) ((ImageView) v.findViewById(R.id.icon)).getDrawable();
			if (layerDrawable == null) continue;
			final Drawable icon = layerDrawable.getDrawable(0);
			// Ideally this would instead key off the new "activated" state, but it's too new.
			if (games[i].equals(currentBackend)) {
				final int highlightColour = ContextCompat.getColor(this, R.color.chooser_current_background);
				highlight.setBackgroundColor(highlightColour);
				icon.setColorFilter(highlightColour, PorterDuff.Mode.SRC_OVER);
				// wait until we know the size
				scrollToOnNextLayout = i;
			} else {
				highlight.setBackgroundResource(0);  // setBackground too new, setBackgroundDrawable deprecated, sigh...
				icon.setColorFilter(null);
			}
		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void enableTableAnimations() {
		final LayoutTransition transition = new LayoutTransition();
		transition.enableTransitionType(LayoutTransition.CHANGING);
		table.setLayoutTransition(transition);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void buildViews()
	{
		for( int i = 0; i < games.length; i++ ) {
			final String gameId = games[i];
			views[i] = getLayoutInflater().inflate(
					R.layout.list_item, table, false);
			final LayerDrawable starredIcon = mkStarryIcon(gameId);
			((ImageView)views[i].findViewById(R.id.icon)).setImageDrawable(starredIcon);
			final int nameId = getResources().getIdentifier("name_"+gameId, "string", getPackageName());
			final int descId = getResources().getIdentifier("desc_"+gameId, "string", getPackageName());
			SpannableStringBuilder desc = new SpannableStringBuilder(nameId > 0 ?
					getString(nameId) : gameId.substring(0,1).toUpperCase() + gameId.substring(1));
			desc.setSpan(new TextAppearanceSpan(this, R.style.ChooserItemName),
					0, desc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			desc.append(": ").append(getString(descId > 0 ? descId : R.string.no_desc));
			final TextView textView = views[i].findViewById(R.id.text);
			textView.setText(desc);
			textView.setVisibility(useGrid ? View.GONE : View.VISIBLE);
			views[i].setOnTouchListener((v, event) -> {
				// Ignore touch within 300ms of resume
				return System.nanoTime() - resumeTime < 300_000_000L;
			});
			views[i].setOnClickListener(v -> {
				Intent i1 = new Intent(GameChooser.this, GamePlay.class);
				i1.setData(Uri.fromParts("sgtpuzzles", gameId, null));
				startActivity(i1);
				overridePendingTransition(0, 0);
			});
			views[i].setOnLongClickListener(v -> {
				toggleStarred(gameId);
				return true;
			});
			views[i].setFocusable(true);
			views[i].setLayoutParams(mkLayoutParams());
			table.addView(views[i]);
		}
		rethinkColumns(true);
	}

	private LayerDrawable mkStarryIcon(String gameId) {
		final int drawableId = getResources().getIdentifier(gameId, "drawable", getPackageName());
		if (drawableId == 0) return null;
		final Drawable icon = ContextCompat.getDrawable(this,
				drawableId);
		final LayerDrawable starredIcon = new LayerDrawable(new Drawable[]{
				icon, Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.ic_star)).mutate() });
		final float density = getResources().getDisplayMetrics().density;
		starredIcon.setLayerInset(1, (int)(42*density), (int)(42*density), 0, 0);
		return starredIcon;
	}

	private GridLayout.LayoutParams mkLayoutParams() {
		final GridLayout.LayoutParams params = new GridLayout.LayoutParams();
		params.setGravity(Gravity.CENTER_HORIZONTAL);
		return params;
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		rethinkColumns(false);
		if (menu != null) {
			// https://github.com/chrisboyle/sgtpuzzles/issues/227
			menu.clear();
			onCreateOptionsMenu(menu);
		}
		rethinkActionBarCapacity();
	}

	private int mColumns = 0;
	private int mColWidthPx = 0;

	private void rethinkColumns(boolean force) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int colWidthDipNeeded = useGrid ? 72 : 298;
		final double screenWidthDip = (double) dm.widthPixels / dm.density;
		final int columns = Math.max(1, (int) Math.floor(
				screenWidthDip / colWidthDipNeeded));
		//noinspection IntegerDivisionInFloatingPointContext
		final int colWidthActualPx = (int) Math.floor(dm.widthPixels / columns);
		if (force || mColumns != columns || mColWidthPx != colWidthActualPx) {
			mColumns = columns;
			mColWidthPx = colWidthActualPx;
			List<View> starred = new ArrayList<>();
			List<View> others = new ArrayList<>();
			for (int i=0; i < games.length; i++) {
				(isStarred(games[i]) ? starred : others).add(views[i]);
			}
			final boolean anyStarred = !starred.isEmpty();
			starredHeader.setVisibility(anyStarred ? View.VISIBLE : View.GONE);
			int row = 0;
			if (anyStarred) {
				setGridCells(starredHeader, 0, row++, mColumns);
				row = setViewsGridCells(row, starred, true);
			}
			otherHeader.setText(anyStarred ? R.string.games_others : R.string.games_others_none_starred);
			setGridCells(otherHeader, 0, row++, mColumns);
			setViewsGridCells(row, others, false);
		}
	}

	@SuppressLint("InlinedApi")
	private void setGridCells(View v, int x, int y, int w) {
		final GridLayout.LayoutParams layoutParams = (GridLayout.LayoutParams) v.getLayoutParams();
		layoutParams.width = mColWidthPx * w;
		layoutParams.columnSpec = GridLayout.spec(x, w, GridLayout.START);
		layoutParams.rowSpec = GridLayout.spec(y, 1, GridLayout.START);
		layoutParams.setGravity((useGrid && w==1) ? Gravity.CENTER_HORIZONTAL : Gravity.START);
		v.setLayoutParams(layoutParams);
	}

	private int setViewsGridCells(final int startRow, final List<View> views, final boolean starred) {
		int col = 0;
		int row = startRow;
		for (View v : views) {
			final LayerDrawable layerDrawable = (LayerDrawable) ((ImageView) v.findViewById(R.id.icon)).getDrawable();
			if (layerDrawable != null) {
				final Drawable star = layerDrawable.getDrawable(1);
				star.setAlpha(starred ? 255 : 0);
				if (col >= mColumns) {
					col = 0;
					row++;
				}
			}
			setGridCells(v, col++, row, 1);
		}
		row++;
		return row;
	}

	private boolean isStarred(String game) {
		return prefs.getBoolean("starred_" + game, DEFAULT_STARRED.contains(game));
	}

	@SuppressLint("CommitPrefEdits")
	private void toggleStarred(String game) {
		SharedPreferences.Editor ed = prefs.edit();
		ed.putBoolean("starred_" + game, !isStarred(game));
		ed.apply();
		rethinkColumns(true);
	}

	private void rethinkActionBarCapacity() {
		if (menu == null) return;
		DisplayMetrics dm = getResources().getDisplayMetrics();
		final int screenWidthDIP = (int) Math.round(((double) dm.widthPixels) / dm.density);
		int state = MenuItem.SHOW_AS_ACTION_ALWAYS;
		if (screenWidthDIP >= 480) {
			state |= MenuItem.SHOW_AS_ACTION_WITH_TEXT;
		}
		menu.findItem(R.id.settings).setShowAsAction(state);
		menu.findItem(R.id.load).setShowAsAction(state);
		menu.findItem(R.id.help_menu).setShowAsAction(state);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
		getMenuInflater().inflate(R.menu.chooser, menu);
		rethinkActionBarCapacity();
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean ret = true;
		switch(item.getItemId()) {
			case R.id.settings:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
			case R.id.load:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					// GET_CONTENT would include Dropbox, but it returns file:// URLs that need SD permission :-(
					Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
					picker.addCategory(Intent.CATEGORY_OPENABLE);
					picker.setType("*/*");
					picker.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"text/*", "application/octet-stream"});
					try {
						startActivityForResult(picker, REQ_CODE_PICKER);
					} catch (ActivityNotFoundException ignored) {
						SendFeedbackActivity.promptToReport(this, R.string.saf_missing_desc, R.string.saf_missing_short);
					}
				} else {
					FilePicker.createAndShow(this, Environment.getExternalStorageDirectory(), false);
				}
				break;
			case R.id.contents:
				Intent intent = new Intent(this, HelpActivity.class);
				intent.putExtra(HelpActivity.TOPIC, "index");
				startActivity(intent);
				break;
			case R.id.email:
				startActivity(new Intent(this, SendFeedbackActivity.class));
				break;
			default: ret = super.onOptionsItemSelected(item);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// https://code.google.com/p/android/issues/detail?id=69205
			supportInvalidateOptionsMenu();
		}
		return ret;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
		if (requestCode != REQ_CODE_PICKER || resultCode != Activity.RESULT_OK || dataIntent == null) return;
		final Uri uri = dataIntent.getData();
		startActivity(new Intent(Intent.ACTION_VIEW, uri, this, GamePlay.class));
		overridePendingTransition(0, 0);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (!key.equals(CHOOSER_STYLE_KEY)) return;
		final boolean newGrid = "grid".equals(prefs.getString(GameChooser.CHOOSER_STYLE_KEY, "list"));
		if(useGrid == newGrid) return;
		useGrid = newGrid;
		rethinkActionBarCapacity();
		for (View v : views) {
			v.findViewById(R.id.text).setVisibility(useGrid ? View.GONE : View.VISIBLE);
			v.setLayoutParams(v.getLayoutParams());
		}
		rethinkColumns(true);
	}
}
