package com.artifex.mupdfdemo;

import java.lang.ref.WeakReference;

import th.co.arip.rsubook.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// From Eak, adapter for preview pdf
public class PDFSlideAdapter extends BaseAdapter {

	private Context context;
	private MuPDFCore core;

	private Bitmap placeHolderBitmap;

	private Point scalePdfSize;

	public PDFSlideAdapter(Context context, MuPDFCore core) {
		this.context = context;
		this.core = core;

		placeHolderBitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.darkdenim3);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return core.countOriginalPages();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	static class ViewHolder {
		ImageView pageImage;
		TextView pageNumber;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.pdf_preview_item, parent,
					false);
			holder = new ViewHolder();
			holder.pageImage = (ImageView) convertView
					.findViewById(R.id.page_preview);
			holder.pageNumber = (TextView) convertView
					.findViewById(R.id.page_number);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.pageNumber.setText(String.valueOf(position + 1));
		loadBitmap(position, holder.pageImage);

		return convertView;
	}

	// From Eak, bitmap operation in background copy from
	// http://developer.android.com/training/displaying-bitmaps/process-bitmap.html
	// with slight modification
	public void loadBitmap(int position, ImageView imageView) {
		if (cancelPotentialWork(position, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView,
					position);
			final AsyncDrawable asyncDrawable = new AsyncDrawable(
					context.getResources(), placeHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute();
		}
	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private int position;

		public BitmapWorkerTask(ImageView imageView, int position) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			this.position = position;
		}

		// Decode image in background.
		// From Eak, create scale size of the preview pdf
		// let height fix at 100 pixel
		@Override
		protected Bitmap doInBackground(Integer... params) {

			if (scalePdfSize == null) {
				PointF pdfSize = core.getPageSize(0);
				float ratio = pdfSize.y / pdfSize.x;
				scalePdfSize = new Point((int) (ratio * 100.0f), 100);
			}
			Bitmap pageBitmap = Bitmap.createBitmap(scalePdfSize.x,
					scalePdfSize.y, Bitmap.Config.ARGB_8888);
			core.drawScalePage(position, pageBitmap, scalePdfSize.x,
					scalePdfSize.y);
			return pageBitmap;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(
					bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public static boolean cancelPotentialWork(int position, ImageView imageView) {
		final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

		if (bitmapWorkerTask != null) {
			final int bitmapData = bitmapWorkerTask.position;
			if (bitmapData != position) {
				// Cancel previous task
				bitmapWorkerTask.cancel(true);
			} else {
				// The same work is already in progress
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was
		// cancelled
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}
}
