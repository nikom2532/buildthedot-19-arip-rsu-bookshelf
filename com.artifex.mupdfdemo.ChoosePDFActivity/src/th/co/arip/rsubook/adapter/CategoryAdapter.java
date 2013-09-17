package th.co.arip.rsubook.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.androidquery.AQuery;

import th.co.arip.rsubook.MainActivity;
import th.co.arip.rsubook.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CategoryAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
    private AQuery aq;
    private ArrayList<HashMap<String, String>> data;
    private Context ctx;
	
	public CategoryAdapter(Context context,ArrayList<HashMap<String, String>> d){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ctx = context;
        data = d;
	}
	
	@Override
	public int getCount() {
		if (data.size()%2==0) {
			return (data.size())/2;
		}
		else{
			return ((data.size())/2)+1;
		}
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final int pos = position;
		  if(convertView==null){
			convertView = mInflater.inflate(R.layout.category, null);
        	holder = new ViewHolder();
        	holder.layoutn = (RelativeLayout)convertView.findViewById(R.id.layout_category_n);
        	holder.layout2n = (RelativeLayout)convertView.findViewById(R.id.layout_category_2n);
			holder.img_category1 = (ImageView)convertView.findViewById(R.id.img_category_n);
			holder.txt_category1 = (TextView)convertView.findViewById(R.id.txt_category_n);
			holder.img_category2 = (ImageView)convertView.findViewById(R.id.img_category_2n);
			holder.txt_category2 = (TextView)convertView.findViewById(R.id.txt_category_2n);
			holder.progressBar1 = (ProgressBar)convertView.findViewById(R.id.progressbar_category_n);
			holder.progressBar2 = (ProgressBar)convertView.findViewById(R.id.progressbar_category_2n);
        	convertView.setTag(holder);
		  }		  
	       else{
	        	holder =(ViewHolder)convertView.getTag();
	        }
		  
			aq =  new AQuery(convertView);		
			holder.txt_category1.setText(data.get(position*2).get("name"));		
			aq.id(holder.img_category1).progress(holder.progressBar1).image(data.get(position*2).get("image"), true, true, 0, 0, null, 0);
			if ((position*2)+1<data.size()) {
			    holder.txt_category2.setText(data.get(((position*2)+1)).get("name"));
			    aq.id(holder.img_category2).progress(holder.progressBar2).image(data.get(((position*2)+1)).get("image"), true, true, 0, 0, null, 0);
			    
			  	aq.id(holder.layout2n).clicked(new OnClickListener() {
					
						@Override
						public void onClick(View v) {
							MainActivity main = (MainActivity) ctx;
							main.showCategoryDetail(data.get((pos*2)+1).get("id"), data.get((pos*2)+1).get("name"));
							
							//Toast.makeText(ctx, ""+data.get((pos*2)+1).get("id"), Toast.LENGTH_SHORT).show();

						}
					});
			}
		    aq.id(holder.progressBar2).visibility(View.GONE);
			
		  /* imageLoader.displayImage(data.get(position*2).get("image"), holder.img_category1,options , new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted() {
					holder.progressBar1.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(FailReason failReason) {
					//String message = null;
					switch (failReason) {
						case IO_ERROR:
							//message = "Input/Output error";
							break;
						case OUT_OF_MEMORY:
							//message = "Out Of Memory error";
							break;
						case UNKNOWN:
							//message = "Unknown error";
							break;
					}
					
					//Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();

					holder.progressBar1.setVisibility(View.GONE);
					//imageView.setImageResource(R.drawable.img_banner);
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					holder.progressBar1.setVisibility(View.GONE);
				}
			});
		    
		    imageLoader.displayImage(data.get(((position*2)+1)).get("image"), holder.img_category2,options , new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted() {
					holder.progressBar2.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(FailReason failReason) {
					//String message = null;
					switch (failReason) {
						case IO_ERROR:
							//message = "Input/Output error";
							break;
						case OUT_OF_MEMORY:
							//message = "Out Of Memory error";
							break;
						case UNKNOWN:
							//message = "Unknown error";
							break;
					}
					
					//Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();

					holder.progressBar2.setVisibility(View.GONE);
					//imageView.setImageResource(R.drawable.img_banner);
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					holder.progressBar2.setVisibility(View.GONE);
				}
			});		    		  	
		  */
		  	holder.layoutn.setClickable(true);
		  	holder.layoutn.setFocusable(true);
		  	holder.layout2n.setClickable(true);
		  	holder.layout2n.setFocusable(true);
		  			  	
		  	
		  	aq.id(holder.layoutn).clicked(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MainActivity main = (MainActivity) ctx;
					main.showCategoryDetail(data.get(pos*2).get("id"), data.get(pos*2).get("name"));
					
					//Toast.makeText(ctx, ""+data.get(pos*2).get("id"), Toast.LENGTH_SHORT).show();
					
				}
			});
		  	
		return convertView;
	}
	
	   static class ViewHolder{
		   	RelativeLayout layoutn;
		   	RelativeLayout layout2n;
	        ImageView img_category1;
	        ImageView img_category2;
	        TextView txt_category1; 
	        TextView txt_category2;
	        ProgressBar progressBar1;
	        ProgressBar progressBar2;
	    }
}
