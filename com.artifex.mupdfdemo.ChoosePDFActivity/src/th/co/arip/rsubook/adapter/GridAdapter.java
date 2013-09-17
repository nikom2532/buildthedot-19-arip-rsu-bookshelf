package th.co.arip.rsubook.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import th.co.arip.rsubook.DetailActivity;
import th.co.arip.rsubook.R;

import com.androidquery.AQuery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<HashMap<String, String>> d;
	private AQuery aq;
	
	public GridAdapter(Context context, ArrayList<HashMap<String, String>> data) {
		mContext = context;
		d = data;
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
	        	holder.img = (ImageView)convertView.findViewById(R.id.img_grid);
	        	holder.txt = (TextView)convertView.findViewById(R.id.txt_grid);
	        	holder.tag = (ImageView)convertView.findViewById(R.id.img_status_grid);
	        	convertView.setTag(holder);
		}	  
		  else{
	        	holder =(ViewHolder)convertView.getTag();
		  }
	   aq = new AQuery(convertView);

       Typeface tf = Typeface.createFromAsset(mContext.getAssets(),"DB Helvethaica X Blk Cond.ttf");
	   aq.id(holder.txt).text(d.get(position).get("name"));
	   aq.id(holder.txt).textSize(28);
	   aq.id(holder.txt).typeface(tf);
	   aq.id(holder.img).progress(R.id.pg_grid).image(d.get(position).get("image"),true, true, 0, 0, null, AQuery.FADE_IN_NETWORK);
	   aq.id(holder.img).clicked(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mContext, DetailActivity.class);
			intent.putExtra("id", d.get(position).get("id"));
			mContext.startActivity(intent);
		}
	});
	   
	   if (d.get(position).get("status").equals("New")) {
		holder.tag.setVisibility(View.VISIBLE);
	   }
	   else{
		   holder.tag.setVisibility(View.INVISIBLE);
	   }
	   return convertView;
	  }
	  
	   static class ViewHolder{
	        ImageView img;
	        TextView txt;
	        ImageView tag;
	    }
}