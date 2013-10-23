package com.artifex.mupdfdemo;

import java.util.ArrayList;
import th.co.arip.rsubook.R;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

public class MuPDFCore {
	/* load our native library */
	static {
		System.loadLibrary("mupdf");
	}

	private final String tag = this.getClass().getSimpleName();

	/* Readable members */
	private int numPages = -1;
	private float pageWidth;
	private float pageHeight;
	private long globals;
	private byte fileBuffer[];

	// From Eak, add numberOfPage Show
	private int numDisplayPages = 1;

	/* The native functions */
	private native long openFile(String filename);

	private native long openBuffer();

	private native int countPagesInternal();

	private native void gotoPageInternal(int localActionPageNum);

	private native float getPageWidth();

	private native float getPageHeight();

	private native void drawPage(Bitmap bitmap, int pageW, int pageH,
			int patchX, int patchY, int patchW, int patchH);

	private native void updatePageInternal(Bitmap bitmap, int page, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH);

	private native RectF[] searchPage(String text);

	private native TextChar[][][][] text();

	private native byte[] textAsHtml();

	private native void addStrikeOutAnnotationInternal(RectF[] lines);

	private native int passClickEventInternal(int page, float x, float y);

	private native void setFocusedWidgetChoiceSelectedInternal(String[] selected);

	private native String[] getFocusedWidgetChoiceSelected();

	private native String[] getFocusedWidgetChoiceOptions();

	private native int setFocusedWidgetTextInternal(String text);

	private native String getFocusedWidgetTextInternal();

	private native int getFocusedWidgetTypeInternal();

	private native LinkInfo[] getPageLinksInternal(int page);

	private native RectF[] getWidgetAreasInternal(int page);

	private native OutlineItem[] getOutlineInternal();

	private native boolean hasOutlineInternal();

	private native boolean needsPasswordInternal();

	private native boolean authenticatePasswordInternal(String password);

	private native MuPDFAlertInternal waitForAlertInternal();

	private native void replyToAlertInternal(MuPDFAlertInternal alert);

	private native void startAlertsInternal();

	private native void stopAlertsInternal();

	private native void destroying();

	private native boolean hasChangesInternal();

	private native void saveInternal();

	public static native boolean javascriptSupported();

	public MuPDFCore(String filename) throws Exception {
		globals = openFile(filename);
		if (globals == 0) {
			throw new Exception("Failed to open " + filename);
		}
	}

	public MuPDFCore(byte buffer[]) throws Exception {
		fileBuffer = buffer;
		globals = openBuffer();
		if (globals == 0) {
			throw new Exception("Failed to open buffer");
		}
	}

	public int countPages() {
		if (numPages < 0)
			numPages = countPagesSynchronized();

		if (numDisplayPages == 1) {
			return numPages;
		} else {
			return numPages / 2 + 1;
		}

	}

	public void setNumDisplayPages(int numDisplayPages) {
		this.numDisplayPages = numDisplayPages;
	}

	private synchronized int countPagesSynchronized() {
		return countPagesInternal();
	}

	/* Shim function */
	private void gotoPage(int page) {
		if (page > numPages - 1)
			page = numPages - 1;
		else if (page < 0)
			page = 0;
		gotoPageInternal(page);
		this.pageWidth = getPageWidth();
		this.pageHeight = getPageHeight();
	}

	public synchronized PointF getPageSize(int page) {

		// extend to handle 2 pages
		if (numDisplayPages == 1 || page == 0) {
			gotoPage(page);
			return new PointF(pageWidth, pageHeight);
		} else {
			gotoPage(page);
			if(page == numPages - 1 && numPages % 2 == 0)
				return new PointF(pageWidth, pageHeight);
				
			gotoPage(page + 1);
			return new PointF(pageWidth * 2, pageHeight);
		}
	}

	public MuPDFAlert waitForAlert() {
		MuPDFAlertInternal alert = waitForAlertInternal();
		return alert != null ? alert.toAlert() : null;
	}

	public void replyToAlert(MuPDFAlert alert) {
		replyToAlertInternal(new MuPDFAlertInternal(alert));
	}

	public void stopAlerts() {
		stopAlertsInternal();
	}

	public void startAlerts() {
		startAlertsInternal();
	}

	public synchronized void onDestroy() {
		destroying();
		globals = 0;
	}

	// page = number of page
	// pageW = scale width of page page image at the current zoom (ex. pdf
	// 1536*2048 pixel full scale
	// normal scale for galaxy s2 = (480 * 640) if we zoom the scale page will
	// be change to something like (722 * 962)
	// pageH = scale height of pdf page image
	// patchX = offset from the leftmost of the image because if we zoom, we
	// can't see the whole pdf
	// patchX is there to indicate how far from the left of the image to the
	// current location of view area
	// patchY = offset from the top
	// patchW = camera width --> highest value is equal to screen width in
	// vertical and screen height in horizontal
	// patchH = camera height
	//
	// The calculation
	// For Ex. if we have screen as (width * height) = 480*800 (s2 vertical) and
	// the image pdf scale (pageW * pageH) = (480 * 640)
	// Then we can directly show all the content of that pdf page on screen
	// because the image size is less than the camera
	// size. In this case patchX and patchY = 0 because left camera is the
	// leftmost of the image and same for the top.
	// pageW = 480, pageH = 640, patchX = 0, patchY = 0, patchW = 480, patchH =
	// 640
	// Another Ex. (width * height) = 480 * 800 and the image scale to (pageW *
	// pageH) = (722 * 962)
	// so our camera cannot show all the content of that pdf image so in this
	// case
	// pageW = 722, pageH = 962, patchX = 0, patchY = 0, patchW = 480, patchH =
	// 800
	// patchW and patchH are the highest value of our screen and patchX, patchY
	// = 0 mean that we are at the left-top of the
	// image so we can see only X = (0 - 480) anything from 481 - 722 we cannot
	// see and for Y = (0 - 800) anything from
	// 801 - 962 we also cannot see. it is out of camera bounds.
	// so if we move the camera to the left then the patchX start increasing
	// For Ex. move to right by 50 points
	// then patchX becomes 50 and we can see the area between (50 - (480+50)) we
	// cannot see the area (0-49) and (531-722)
	public synchronized Bitmap drawPage(int page, int pageW, int pageH,
			int patchX, int patchY, int patchW, int patchH) {
		// extends for 2 pages
		Log.e(tag, "DRAW");
		Log.e(tag, "pageW : " + pageW);
		Log.e(tag, "pageH : " + pageH);
		Log.e(tag, "patchX : " + patchX);
		Log.e(tag, "patchY : " + patchY);
		Log.e(tag, "patchW : " + patchW);
		Log.e(tag, "patchH : " + patchH);
		Log.e(tag, "page : " + page);
		Bitmap bm = Bitmap.createBitmap(patchW, patchH, Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		canvas.drawColor(Color.TRANSPARENT);
		if (numDisplayPages == 1 || page == 0) {
			Log.e(tag, "draw first page");
			gotoPage(page);
			drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH);
			return bm;
		} else if (numDisplayPages == 2 && numPages % 2 == 0
				&& page == numPages / 2 + 1) {
			Log.e(tag, "draw last page");
			drawPage(bm, pageW, pageH, patchX, patchY, patchW, patchH);
			return bm;
		} else {
			// idea for 2 pages combine one page together

			final int thePageToDraw = (page == 0) ? 0 : page * 2 - 1;
			int leftPageWidth = pageW / 2;
			int rightPageWidth = pageW - leftPageWidth;

			int leftBitmapWidth = Math.min(leftPageWidth, leftPageWidth
					- patchX);

			leftBitmapWidth = (leftBitmapWidth < 0) ? 0 : leftBitmapWidth;

			int rightBitmapWidth = patchW - leftBitmapWidth;

			Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

			if (leftBitmapWidth > 0) {
				Log.e(tag, "draw left");
				Bitmap leftBm = Bitmap.createBitmap(leftBitmapWidth, patchH,
						Config.ARGB_8888);
				gotoPage(thePageToDraw);
				drawPage(leftBm, leftPageWidth, pageH, patchX, patchY,
						leftBitmapWidth, patchH);
				canvas.drawBitmap(leftBm, 0, 0, paint);
				leftBm.recycle();
			}
			if (rightBitmapWidth > 0) {
				Log.e(tag, "draw right");
				Bitmap rightBm = Bitmap.createBitmap(rightBitmapWidth, patchH,
						Config.ARGB_8888);
				gotoPage(thePageToDraw + 1);

				int patchXRight = (leftBitmapWidth == 0) ? patchX
						- leftBitmapWidth : 0;
				drawPage(rightBm, rightPageWidth, pageH, patchXRight, patchY,
						rightPageWidth, patchH);

				canvas.drawBitmap(rightBm, (float) leftPageWidth, 0, paint);
				rightBm.recycle();
			}
		}

		return bm;
	}

	// From Eak, update to 2 page
	public synchronized Bitmap updatePage(BitmapHolder h, int page, int pageW,
			int pageH, int patchX, int patchY, int patchW, int patchH) {
		Bitmap bm = null;
		Bitmap old_bm = h.getBm();

		// extends to 2 pages
		if (old_bm == null)
			return null;

		bm = old_bm.copy(Bitmap.Config.ARGB_8888, false);
		old_bm = null;

		Log.e(tag, "UPDATE");
		Log.e(tag, "pageW : " + pageW);
		Log.e(tag, "pageH : " + pageH);
		Log.e(tag, "patchX : " + patchX);
		Log.e(tag, "patchY : " + patchY);
		Log.e(tag, "patchW : " + patchW);
		Log.e(tag, "patchH : " + patchH);
		Log.e(tag, "page : " + page);
		//if (numDisplayPages == 1) {

			updatePageInternal(bm, page, pageW, pageH, patchX, patchY, patchW,
					patchH);
			return bm;
		/*} else {
			Canvas canvas = new Canvas(bm);
			canvas.drawColor(Color.TRANSPARENT);

			final int thePageToDraw = (page == 0) ? 0 : page * 2 - 1;
			int leftPageWidth = pageW / 2;
			int rightPageWidth = pageW - leftPageWidth;

			int leftBitmapWidth = Math.min(leftPageWidth, leftPageWidth
					- patchX);

			leftBitmapWidth = (leftBitmapWidth < 0) ? 0 : leftBitmapWidth;

			int rightBitmapWidth = patchW - leftBitmapWidth;

			Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
			if (leftBitmapWidth > 0) {
				
				int leftWidth = Math.min(leftBitmapWidth, bm.getWidth());
				Bitmap leftBm = Bitmap
						.createBitmap(bm, 0, 0, leftWidth, patchH);
				updatePageInternal(leftBm, page, leftPageWidth, pageH, patchX,
						patchY, leftBitmapWidth, patchH);
				canvas.drawBitmap(leftBm, 0, 0, paint);
				leftBm.recycle();
			}
			if (rightBitmapWidth > 0) {
				
				Bitmap rightBm = Bitmap.createBitmap(bm, leftBitmapWidth, 0,
						rightBitmapWidth, patchH);
				int patchXRight = (leftBitmapWidth == 0) ? patchX
						- leftBitmapWidth : 0;
				updatePageInternal(rightBm, page, rightPageWidth, pageH,
						patchXRight, patchY, rightPageWidth, patchH);

				canvas.drawBitmap(rightBm, (float) leftBitmapWidth, 0, paint);
				rightBm.recycle();
			}
			return bm;
		}*/

	}

	public synchronized PassClickResult passClickEvent(int page, float x,
			float y) {
		boolean changed = passClickEventInternal(page, x, y) != 0;

		switch (WidgetType.values()[getFocusedWidgetTypeInternal()]) {
		case TEXT:
			return new PassClickResultText(changed,
					getFocusedWidgetTextInternal());
		case LISTBOX:
		case COMBOBOX:
			return new PassClickResultChoice(changed,
					getFocusedWidgetChoiceOptions(),
					getFocusedWidgetChoiceSelected());
		default:
			return new PassClickResult(changed);
		}

	}

	public synchronized boolean setFocusedWidgetText(int page, String text) {
		boolean success;
		gotoPage(page);
		success = setFocusedWidgetTextInternal(text) != 0 ? true : false;

		return success;
	}

	public synchronized void setFocusedWidgetChoiceSelected(String[] selected) {
		setFocusedWidgetChoiceSelectedInternal(selected);
	}

	public synchronized LinkInfo[] getPageLinks(int page) {
		// extends for 2 pages
		return getPageLinksInternal(page);
	}

	public synchronized RectF[] getWidgetAreas(int page) {
		return getWidgetAreasInternal(page);
	}

	public synchronized RectF[] searchPage(int page, String text) {
		gotoPage(page);
		return searchPage(text);
	}

	public synchronized byte[] html(int page) {
		gotoPage(page);
		return textAsHtml();
	}

	public synchronized TextWord[][] textLines(int page) {
		gotoPage(page);
		TextChar[][][][] chars = text();

		// The text of the page held in a hierarchy (blocks, lines, spans).
		// Currently we don't need to distinguish the blocks level or
		// the spans, and we need to collect the text into words.
		ArrayList<TextWord[]> lns = new ArrayList<TextWord[]>();

		for (TextChar[][][] bl : chars) {
			for (TextChar[][] ln : bl) {
				ArrayList<TextWord> wds = new ArrayList<TextWord>();
				TextWord wd = new TextWord();

				for (TextChar[] sp : ln) {
					for (TextChar tc : sp) {
						if (tc.c != ' ') {
							wd.Add(tc);
						} else if (wd.w.length() > 0) {
							wds.add(wd);
							wd = new TextWord();
						}
					}
				}

				if (wd.w.length() > 0)
					wds.add(wd);

				if (wds.size() > 0)
					lns.add(wds.toArray(new TextWord[wds.size()]));
			}
		}

		return lns.toArray(new TextWord[lns.size()][]);
	}

	public synchronized void addStrikeOutAnnotation(int page, RectF[] lines) {
		gotoPage(page);
		addStrikeOutAnnotationInternal(lines);
	}

	public synchronized boolean hasOutline() {
		return hasOutlineInternal();
	}

	public synchronized OutlineItem[] getOutline() {
		return getOutlineInternal();
	}

	public synchronized boolean needsPassword() {
		return needsPasswordInternal();
	}

	public synchronized boolean authenticatePassword(String password) {
		return authenticatePasswordInternal(password);
	}

	public synchronized boolean hasChanges() {
		return hasChangesInternal();
	}

	public synchronized void save() {
		saveInternal();
	}
}
