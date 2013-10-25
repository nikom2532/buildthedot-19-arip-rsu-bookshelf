package com.artifex.mupdfdemo;

import java.util.concurrent.Executor;
import java.io.InputStream;
import java.io.FileInputStream;
import th.co.arip.rsubook.R;
import th.co.arip.rsubook.lib.TwoWayView;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

class ThreadPerTaskExecutor implements Executor {
	@Override
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity {

	private final String tag = this.getClass().getSimpleName();

	private final int REQUEST_GRID = 0;
	private final int REQUEST_OUTLINE = 1;
	/* The core rendering instance */
	private MuPDFCore core;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;

	// From Eak, Add our View
	private LinearLayout topBar;
	private TwoWayView bottomBar;
	private PDFSlideAdapter adapter;
	private TextView tPdfTitle;
	private ImageButton bOutline;
	private ImageButton bGrid;

	/*
	 * private EditText mPasswordView; private TextView mFilenameView; private
	 * SeekBar mPageSlider; private int mPageSliderRes; private TextView
	 * mPageNumberView; private TextView mInfoView; private ImageButton
	 * mSearchButton; private ImageButton mReflowButton; private ImageButton
	 * mSelectButton; private ImageButton mCancelSelectButton; private
	 * ImageButton mCopySelectButton; private ImageButton mStrikeOutButton;
	 * private ImageButton mCancelButton; private ImageButton mOutlineButton;
	 * private ViewAnimator mTopBarSwitcher; private ImageButton mLinkButton;
	 * private boolean mTopBarIsSearch; private ImageButton mSearchBack; private
	 * ImageButton mSearchFwd; private EditText mSearchText; private SearchTask
	 * mSearchTask; private boolean mLinkHighlight = false; private final
	 * Handler mHandler = new Handler(); private boolean mReflow = false;
	 */

	private AlertDialog.Builder mAlertBuilder;
	private boolean mAlertsActive = false;
	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;

	private int mOrientation;

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid
		// stalling
		// the UI. Some calls can lead to javascript-invoked requests to display
		// an
		// alert dialog and collect a reply from the user. The task has to be
		// blocked
		// until the user's reply is received. This method creates an
		// asynchronous task,
		// the purpose of which is to wait of these requests and produce the
		// dialog
		// in response, while leaving the core blocked. When the dialog receives
		// the
		// user's response, it is sent to the core via replyToAlert, unblocking
		// it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1:
								index = 0;
								break;
							case AlertDialog.BUTTON2:
								index = 1;
								break;
							case AlertDialog.BUTTON3:
								index = 2;
								break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it
							// can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next
							// alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType) {
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2, "Cancel",
							listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1, "Ok", listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3, "Cancel",
							listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog
							.setButton(AlertDialog.BUTTON1, "Yes", listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2, "No", listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								mAlertDialog = null;
								if (mAlertsActive) {
									result.buttonPressed = MuPDFAlert.ButtonPressed.None;
									core.replyToAlert(result);
									createAlertWaiter();
								}
							}
						});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		/*
		 * mFileName = new String(lastSlashPos == -1 ? path :
		 * path.substring(lastSlashPos+1));
		 */
		mFileName = getIntent().getExtras().getString("title");

		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[]) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlertBuilder = new AlertDialog.Builder(this);

		core = getMuPDFCore(savedInstanceState);

		if (core == null) {
			return;
		}

		mOrientation = getResources().getConfiguration().orientation;

		if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			core.setNumDisplayPages(2);
		} else {
			core.setNumDisplayPages(1);
		}

		createUI(savedInstanceState);
	}

	private MuPDFCore getMuPDFCore(Bundle savedInstanceState) {
		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();

			if (savedInstanceState != null
					&& savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file
					// manager
					// Hopefully other file managers will use this same scheme,
					// if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri,
							new String[] { "_data" }, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String failString = null;
						if (str == null) {
							try {
								InputStream is = getContentResolver()
										.openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							} catch (java.lang.OutOfMemoryError e) {
								System.out
										.println("Out of memory during buffer reading");
								failString = e.toString();
							} catch (Exception e) {
								failString = e.toString();
							}
							if (failString != null) {
								buffer = null;
								Resources res = getResources();
								AlertDialog alert = mAlertBuilder.create();
								String contentFailure = res
										.getString(R.string.content_failure);
								String openFailed = res
										.getString(R.string.open_failed);
								setTitle(String.format(contentFailure,
										openFailed, failString));
								alert.setButton(AlertDialog.BUTTON_POSITIVE,
										"Dismiss",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												finish();
											}
										});
								alert.show();
								return null;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				// requestPassword(savedInstanceState);
				return null;
			}
		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return null;
		}
		return core;
	}

	/*
	 * public void requestPassword(final Bundle savedInstanceState) {
	 * mPasswordView = new EditText(this);
	 * mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
	 * mPasswordView .setTransformationMethod(new
	 * PasswordTransformationMethod());
	 * 
	 * AlertDialog alert = mAlertBuilder.create();
	 * alert.setTitle(R.string.enter_password); alert.setView(mPasswordView);
	 * alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) { if
	 * (core.authenticatePassword(mPasswordView.getText() .toString())) {
	 * createUI(savedInstanceState); } else {
	 * requestPassword(savedInstanceState); } } });
	 * alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * finish(); } }); alert.show(); }
	 */

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				// From Eak, setting page when first load
				/*
				 * mPageNumberView.setText(String.format("%d / %d", i + 1,
				 * core.countPages())); mPageSlider.setMax((core.countPages() -
				 * 1) * mPageSliderRes); mPageSlider.setProgress(i *
				 * mPageSliderRes);
				 */
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, core));

		/*
		 * mSearchTask = new SearchTask(this, core) {
		 * 
		 * @Override protected void onTextFound(SearchTaskResult result) {
		 * SearchTaskResult.set(result); // Ask the ReaderView to move to the
		 * resulting page mDocView.setDisplayedViewIndex(result.pageNumber); //
		 * Make the ReaderView act on the change to SearchTaskResult // via
		 * overridden onChildSetup method. mDocView.resetupChildren(); } };
		 */

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// From Eak, remove unnecessary buttons and views
		/*
		 * // Set up the page slider int smax = Math.max(core.countPages() - 1,
		 * 1); mPageSliderRes = ((10 + smax - 1) / smax) * 2;
		 * 
		 * // Set the file-name text mFilenameView.setText(mFileName);
		 * 
		 * // Activate the seekbar mPageSlider .setOnSeekBarChangeListener(new
		 * SeekBar.OnSeekBarChangeListener() {
		 * 
		 * @Override public void onStopTrackingTouch(SeekBar seekBar) {
		 * mDocView.setDisplayedViewIndex((seekBar.getProgress() +
		 * mPageSliderRes / 2) / mPageSliderRes); }
		 * 
		 * @Override public void onStartTrackingTouch(SeekBar seekBar) { }
		 * 
		 * @Override public void onProgressChanged(SeekBar seekBar, int
		 * progress, boolean fromUser) { updatePageNumView((progress +
		 * mPageSliderRes / 2) / mPageSliderRes); } });
		 * 
		 * // Activate the search-preparing button
		 * mSearchButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { searchModeOn(); } });
		 * 
		 * // Activate the reflow button mReflowButton.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { toggleReflow(); } });
		 * 
		 * // Activate the select button mSelectButton.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * mDocView.setSelectionMode(true);
		 * mTopBarSwitcher.setDisplayedChild(2); } });
		 * 
		 * mCancelSelectButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { MuPDFView pageView =
		 * (MuPDFView) mDocView.getDisplayedView(); if (pageView != null)
		 * pageView.deselectText(); mDocView.setSelectionMode(false);
		 * mTopBarSwitcher.setDisplayedChild(0); } });
		 * 
		 * final Context context = this;
		 * mCopySelectButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @SuppressLint("NewApi")
		 * 
		 * @Override public void onClick(View v) { MuPDFView pageView =
		 * (MuPDFView) mDocView.getDisplayedView(); boolean copied = false; if
		 * (pageView != null) copied = pageView.copySelection();
		 * mDocView.setSelectionMode(false);
		 * mTopBarSwitcher.setDisplayedChild(0); mInfoView.setText(copied ?
		 * "Copied to clipboard" : "No text selected");
		 * 
		 * int currentApiVersion = android.os.Build.VERSION.SDK_INT; if
		 * (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
		 * AnimatorSet set = (AnimatorSet) AnimatorInflater
		 * .loadAnimator(context, R.animator.info); set.setTarget(mInfoView);
		 * set.addListener(new Animator.AnimatorListener() {
		 * 
		 * @Override public void onAnimationStart(Animator animation) {
		 * mInfoView.setVisibility(View.VISIBLE); }
		 * 
		 * @Override public void onAnimationRepeat(Animator animation) { }
		 * 
		 * @Override public void onAnimationEnd(Animator animation) {
		 * mInfoView.setVisibility(View.INVISIBLE); }
		 * 
		 * @Override public void onAnimationCancel(Animator animation) { } });
		 * set.start(); } else { mInfoView.setVisibility(View.VISIBLE);
		 * mHandler.postDelayed(new Runnable() {
		 * 
		 * @Override public void run() {
		 * mInfoView.setVisibility(View.INVISIBLE); } }, 500); } } });
		 * 
		 * mStrikeOutButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { MuPDFView pageView =
		 * (MuPDFView) mDocView.getDisplayedView(); if (pageView != null)
		 * pageView.strikeOutSelection(); mDocView.setSelectionMode(false);
		 * mTopBarSwitcher.setDisplayedChild(0); } });
		 * 
		 * mCancelButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { searchModeOff(); } });
		 * 
		 * // Search invoking buttons are disabled while there is no text
		 * specified mSearchBack.setEnabled(false);
		 * mSearchFwd.setEnabled(false);
		 * mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		 * mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));
		 * 
		 * // React to interaction with the text widget
		 * mSearchText.addTextChangedListener(new TextWatcher() {
		 * 
		 * @Override public void afterTextChanged(Editable s) { boolean haveText
		 * = s.toString().length() > 0; mSearchBack.setEnabled(haveText);
		 * mSearchFwd.setEnabled(haveText); if (haveText) {
		 * mSearchBack.setColorFilter(Color.argb(255, 255, 255, 255));
		 * mSearchFwd.setColorFilter(Color.argb(255, 255, 255, 255)); } else {
		 * mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		 * mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128)); }
		 * 
		 * // Remove any previous search results if (SearchTaskResult.get() !=
		 * null && !mSearchText.getText().toString()
		 * .equals(SearchTaskResult.get().txt)) { SearchTaskResult.set(null);
		 * mDocView.resetupChildren(); } }
		 * 
		 * @Override public void beforeTextChanged(CharSequence s, int start,
		 * int count, int after) { }
		 * 
		 * @Override public void onTextChanged(CharSequence s, int start, int
		 * before, int count) { } });
		 * 
		 * // React to Done button on keyboard mSearchText
		 * .setOnEditorActionListener(new TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView v, int actionId,
		 * KeyEvent event) { if (actionId == EditorInfo.IME_ACTION_DONE)
		 * search(1); return false; } });
		 * 
		 * mSearchText.setOnKeyListener(new View.OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
		 * if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode ==
		 * KeyEvent.KEYCODE_ENTER) search(1); return false; } });
		 * 
		 * // Activate search invoking buttons
		 * mSearchBack.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { search(-1); } });
		 * mSearchFwd.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { search(1); } });
		 * 
		 * mLinkButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { if (mLinkHighlight) {
		 * mLinkButton.setColorFilter(Color.argb(0xFF, 255, 255, 255));
		 * mLinkHighlight = false; } else { // LINK_COLOR tint
		 * mLinkButton.setColorFilter(Color.argb(0xFF, 172, 114, 37));
		 * mLinkHighlight = true; } // Inform pages of the change.
		 * mDocView.setLinksEnabled(mLinkHighlight); } });
		 * 
		 * if (core.hasOutline()) { mOutlineButton.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { OutlineItem outline[] =
		 * core.getOutline(); if (outline != null) {
		 * OutlineActivityData.get().items = outline; Intent intent = new
		 * Intent(MuPDFActivity.this, OutlineActivity.class);
		 * startActivityForResult(intent, 0); } } }); } else {
		 * mOutlineButton.setVisibility(View.GONE); }
		 */

		// From Eak, add button listener for gridview(preview) and
		// listview(outline)
		bGrid.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MuPDFCoreHelper.core = core;
				Intent i = new Intent(MuPDFActivity.this,
						GridPreviewActivity.class);
				startActivityForResult(i, REQUEST_GRID);
			}
		});

		bOutline.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		int orientation = prefs.getInt("orientation", mOrientation);
		int pageNum = prefs.getInt("page" + mFileName, 0);
		Log.e(tag, "getIndex : " + pageNum);
		if (orientation == mOrientation)
			mDocView.setDisplayedViewIndex(pageNum);
		else {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				mDocView.setDisplayedViewIndex((pageNum + 1) / 2);
			} else {
				mDocView.setDisplayedViewIndex((pageNum == 0) ? 0
						: pageNum * 2 - 1);
			}
		}

		if (savedInstanceState == null
				|| !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		// From Eak, no search mode
		/*
		 * if (savedInstanceState != null &&
		 * savedInstanceState.getBoolean("SearchMode", false)) searchModeOn();
		 */

		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		layout.setBackgroundResource(R.drawable.tiled_background);
		// layout.setBackgroundResource(R.color.canvas);
		setContentView(layout);
	}

	// From Eak, get select page based on the number of display pages
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(tag, "Page Click : " + resultCode);
		if (resultCode >= 0)
			mDocView.setDisplayedViewIndex(getSelectPage(resultCode));

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	/*
	 * private void toggleReflow() { mReflow = !mReflow; if (mReflow) {
	 * mDocView.setAdapter(new MuPDFReflowAdapter(this, core));
	 * mReflowButton.setColorFilter(Color.argb(0xFF, 172, 114, 37)); } else {
	 * mDocView.setAdapter(new MuPDFPageAdapter(this, core));
	 * mReflowButton.setColorFilter(Color.argb(0xFF, 255, 255, 255)); }
	 * mDocView.refresh(mReflow); }
	 */

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			Log.e(tag, "index : " + mDocView.getDisplayedViewIndex());
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.putInt("orientation", mOrientation);
			edit.commit();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);

		/*
		 * if (mTopBarIsSearch) outState.putBoolean("SearchMode", true);
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();

		// mSearchTask.stop();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			Log.e(tag, "saveIndex : " + mDocView.getDisplayedViewIndex());
			edit.putInt("orientation", mOrientation);
			edit.commit();
		}
	}

	@Override
	public void onDestroy() {
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		super.onDestroy();
	}

	void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			// From Eak, updatePageNumView of slider text
			// updatePageNumView(index);
			/*
			 * mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			 * mPageSlider.setProgress(index * mPageSliderRes); if
			 * (mTopBarIsSearch) { mSearchText.requestFocus(); showKeyboard(); }
			 */

			// From Eak, animation showing top bar
			Animation anim = new TranslateAnimation(0, 0, -topBar.getHeight(),
					0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					topBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
				}
			});
			topBar.startAnimation(anim);

			// From Eak, animation showing bottom bar
			anim = new TranslateAnimation(0, 0, bottomBar.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					bottomBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
				}
			});
			bottomBar.startAnimation(anim);
		}
	}

	void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			// hideKeyboard();

			// From Eak, animation hiding top bar
			Animation anim = new TranslateAnimation(0, 0, 0,
					-topBar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					topBar.setVisibility(View.INVISIBLE);
				}
			});
			topBar.startAnimation(anim);

			// From Eak, animation hiding bottom bar
			anim = new TranslateAnimation(0, 0, 0, bottomBar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					bottomBar.setVisibility(View.INVISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
				}
			});
			bottomBar.startAnimation(anim);
		}
	}

	// From Eak, no search mode
	/*
	 * void searchModeOn() { if (!mTopBarIsSearch) { mTopBarIsSearch = true; //
	 * Focus on EditTextWidget mSearchText.requestFocus(); showKeyboard();
	 * mTopBarSwitcher.setDisplayedChild(1); } }
	 */

	// From Eak, no search mode
	/*
	 * void searchModeOff() { if (mTopBarIsSearch) { mTopBarIsSearch = false;
	 * hideKeyboard(); mTopBarSwitcher.setDisplayedChild(0);
	 * SearchTaskResult.set(null); // Make the ReaderView act on the change to
	 * mSearchTaskResult // via overridden onChildSetup method.
	 * mDocView.resetupChildren(); } }
	 */

	// From Eak, update slider page text to show, no need, we don't use slider
	/*
	 * void updatePageNumView(int index) { if (core == null) return;
	 * mPageNumberView.setText(String.format("%d / %d", index + 1,
	 * core.countPages())); }
	 */

	void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.buttons, null);

		topBar = (LinearLayout) mButtonsView.findViewById(R.id.topbar);

		bOutline = (ImageButton) mButtonsView.findViewById(R.id.outlineButton);
		bGrid = (ImageButton) mButtonsView.findViewById(R.id.gridButton);

		bottomBar = (TwoWayView) mButtonsView.findViewById(R.id.bottombar);
		adapter = new PDFSlideAdapter(this, core);
		bottomBar.setAdapter(adapter);
		bottomBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				Toast.makeText(getBaseContext(),
						"Click Position : " + String.valueOf(position + 1),
						Toast.LENGTH_SHORT).show();
				mDocView.setDisplayedViewIndex(getSelectPage(position));
				hideButtons();
			}
		});

		topBar.setVisibility(View.INVISIBLE);
		bottomBar.setVisibility(View.INVISIBLE);

		// From Eak, remove unnecessary UIs.
		/*
		 * mFilenameView = (TextView)
		 * mButtonsView.findViewById(R.id.docNameText); mPageSlider = (SeekBar)
		 * mButtonsView.findViewById(R.id.pageSlider); mPageNumberView =
		 * (TextView) mButtonsView.findViewById(R.id.pageNumber); mInfoView =
		 * (TextView) mButtonsView.findViewById(R.id.info); mSearchButton =
		 * (ImageButton) mButtonsView .findViewById(R.id.searchButton);
		 * mReflowButton = (ImageButton) mButtonsView
		 * .findViewById(R.id.reflowButton); mSelectButton = (ImageButton)
		 * mButtonsView .findViewById(R.id.selectButton); mCancelSelectButton =
		 * (ImageButton) mButtonsView .findViewById(R.id.cancelSelectButton);
		 * mCopySelectButton = (ImageButton) mButtonsView
		 * .findViewById(R.id.copySelectButton); mStrikeOutButton =
		 * (ImageButton) mButtonsView .findViewById(R.id.strikeOutButton);
		 * mCancelButton = (ImageButton) mButtonsView.findViewById(R.id.cancel);
		 * mOutlineButton = (ImageButton) mButtonsView
		 * .findViewById(R.id.outlineButton); mTopBarSwitcher = (ViewAnimator)
		 * mButtonsView .findViewById(R.id.switcher); mSearchBack =
		 * (ImageButton) mButtonsView.findViewById(R.id.searchBack); mSearchFwd
		 * = (ImageButton) mButtonsView .findViewById(R.id.searchForward);
		 * mSearchText = (EditText) mButtonsView.findViewById(R.id.searchText);
		 * mLinkButton = (ImageButton)
		 * mButtonsView.findViewById(R.id.linkButton);
		 * mTopBarSwitcher.setVisibility(View.INVISIBLE);
		 * mPageNumberView.setVisibility(View.INVISIBLE);
		 * mInfoView.setVisibility(View.INVISIBLE);
		 * mPageSlider.setVisibility(View.INVISIBLE);
		 */
	}

	// From Eak, no keyboard showing
	/*
	 * void showKeyboard() { InputMethodManager imm = (InputMethodManager)
	 * getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null)
	 * imm.showSoftInput(mSearchText, 0); }
	 * 
	 * void hideKeyboard() { InputMethodManager imm = (InputMethodManager)
	 * getSystemService(Context.INPUT_METHOD_SERVICE); if (imm != null)
	 * imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0); }
	 */

	// From Eak, no search mode
	/*
	 * void search(int direction) { hideKeyboard(); int displayPage =
	 * mDocView.getDisplayedViewIndex(); SearchTaskResult r =
	 * SearchTaskResult.get(); int searchPage = r != null ? r.pageNumber : -1;
	 * mSearchTask.go(mSearchText.getText().toString(), direction, displayPage,
	 * searchPage); }
	 * 
	 * @Override public boolean onSearchRequested() { if (mButtonsVisible &&
	 * mTopBarIsSearch) { hideButtons(); } else { showButtons(); searchModeOn();
	 * } return super.onSearchRequested(); }
	 */

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible /* && !mTopBarIsSearch */) {
			hideButtons();
		} else {
			showButtons();
			// searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}

		super.onStop();
	}

	private int getSelectPage(int page) {
		if (core.getNumDisplayPages() == 1 || page == 0)
			return page;
		else
			return (page + 1) / 2;
	}

	// From Eak, no change to document pdf in our case, no need.
	/*
	 * @Override public void onBackPressed() { if (core.hasChanges()) {
	 * DialogInterface.OnClickListener listener = new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) { if
	 * (which == AlertDialog.BUTTON_POSITIVE) core.save();
	 * 
	 * finish(); } }; AlertDialog alert = mAlertBuilder.create();
	 * alert.setTitle("MuPDF");
	 * alert.setMessage("Document has changes. Save them?");
	 * alert.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", listener);
	 * alert.setButton(AlertDialog.BUTTON_NEGATIVE, "No", listener);
	 * alert.show(); } else { super.onBackPressed(); } }
	 */
}
