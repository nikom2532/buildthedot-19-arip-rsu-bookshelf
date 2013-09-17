package th.co.arip.rsubook.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import th.co.arip.db.BookHandler;
import th.co.arip.rsubook.DetailActivity;
import th.co.arip.rsubook.MainActivity;
import th.co.arip.rsubook.R;

import com.androidquery.AQuery;
import com.artifex.mupdfdemo.MuPDFActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LibraryAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<HashMap<String, String>> d;
	private ArrayList<HashMap<String, String>> d2;
	private AQuery aq;
	
	public LibraryAdapter(Context context, ArrayList<HashMap<String, String>> data,
			ArrayList<HashMap<String, String>> compare) {
		mContext = context;
		d = data;
		if (compare!=null) {
			d2 = compare;
		}
	}
	
	@Override
	  public int getCount() {
	   return d.size();
	  }

	  @Override
	  public Object getItem(int position) {
	   return d.get(position);
	  }

	  @Override
	  public long getItemId(int position) {
	   return position;
	  }

	  @Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		LayoutInflater inflater;
		  if (convertView==null) {
				inflater =LayoutInflater.from(mContext);
				convertView = inflater.inflate(R.layout.grid, null);
	        	holder = new ViewHolder();
	        	holder.layout = (RelativeLayout)convertView.findViewById(R.id.layout_grid);
	        	holder.img = (ImageView)convertView.findViewById(R.id.img_grid);
	        	holder.txt = (TextView)convertView.findViewById(R.id.txt_grid);
	        	holder.tag = (ImageView)convertView.findViewById(R.id.img_status_grid);
	        	convertView.setTag(holder);
		}	  
		  else{
	        	holder =(ViewHolder)convertView.getTag();
		  }
	   aq = new AQuery(convertView);

	   //Toast.makeText(mContext, d.get(position).get("revision"), Toast.LENGTH_SHORT).show();
	   
       Typeface tf = Typeface.createFromAsset(mContext.getAssets(),"DB Helvethaica X Blk Cond.ttf");
	   aq.id(holder.txt).text(d.get(position).get("name"));
	   aq.id(holder.txt).textSize(28);
	   aq.id(holder.txt).typeface(tf);
	   aq.id(holder.img).progress(R.id.pg_grid).image(d.get(position).get("image"),true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);
	   aq.id(holder.img).clicked(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			/*Intent intent = new Intent(mContext, DetailActivity.class);
			intent.putExtra("id", d.get(position).get("id"));
			mContext.startActivity(intent);*/
			Uri uri = Uri.parse(d.get(position).get("category"));
			Intent intent = new Intent(mContext,MuPDFActivity.class);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(uri);
			intent.putExtra("title", d.get(position).get("name"));
			mContext.startActivity(intent);
		}
	});
	   
	   holder.img.setLongClickable(true);
	   holder.img.setOnLongClickListener(new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			confirmDelete(position);
			return true;
		}
	});
	   
	   holder.layout.setLongClickable(true);
	   holder.layout.setOnLongClickListener(new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			confirmDelete(position);
			return true;
		}
	});
	   
	   if (d2!=null) {
		   double re_new = Double.parseDouble(d2.get(position).get("revision"));
		   double re_old = Double.parseDouble(d.get(position).get("revision"));
		   //aq.id(holder.txt).text(d.get(position).get("revision"));
		   if (re_new>re_old) {
			   holder.tag.setVisibility(View.VISIBLE);
			   holder.tag.setImageResource(R.drawable.update);
			   aq.id(holder.img).clicked(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, DetailActivity.class);
						intent.putExtra("id", d.get(position).get("id"));
						intent.putExtra("tag", "update");
						mContext.startActivity(intent);
					}
				});
		   }
		   else{
			   holder.tag.setVisibility(View.INVISIBLE);
		   }
	   }

	   
	   return convertView;
	  }
	  
		private void confirmDelete(final int position){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	        builder.setCancelable(true);
	        builder.setTitle("Are you sure you want to delete this book?");
	        builder.setInverseBackgroundForced(true);
	        builder.setPositiveButton("Yes",
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,
	                            int which) {
	    					BookHandler favoriteHandler = new BookHandler(mContext);
	    					favoriteHandler.open();
	    					favoriteHandler.deleteComment(d.get(position).get("id"));
	    					
	    					new DeleteFile().execute(d.get(position).get("id"));

	                    }
	                });
	        builder.setNegativeButton("No",
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,
	                            int which) {
	                        dialog.dismiss();
	                    }
	                });
	        AlertDialog alert = builder.create();
	        alert.show();
		}
	  
	   static class ViewHolder{
		   RelativeLayout layout;
	        ImageView img;
	        TextView txt;
	        ImageView tag;
	    }
	   
	   private void deleteRecursive(File fileOrDirectory) {
		    if (fileOrDirectory.isDirectory())
		        for (File child : fileOrDirectory.listFiles())
		            deleteRecursive(child);

		    fileOrDirectory.delete();
		}
	   
	   private class DeleteFile extends AsyncTask<String, Void, Void>{
		   private ProgressDialog pd;
		   
		   @Override
		   protected Void doInBackground(String... params) {
	        	String DB_PATH = Environment.getExternalStorageDirectory()+
	            		"/Android/data/th.co.arip.rsubook/"+params[0]+"/";
	        	File fileOrDirectory = new File(DB_PATH);
	        	deleteRecursive(fileOrDirectory);
			return null;
		   }
		
		   @Override
		   protected void onPreExecute() {
			super.onPreExecute();
			pd = ProgressDialog.show(mContext, null, "Deleting");
		   }
		   
		   @Override
		   protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			pd.dismiss();
			MainActivity fav = (MainActivity)mContext;
			fav.showLibrary(MainActivity.CURRENT_SORT);
		   }
	   }
}