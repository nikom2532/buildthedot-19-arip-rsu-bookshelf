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

public class CategoryAdapterHorizontal extends BaseAdapter {
	private LayoutInflater mInflater;
    private AQuery aq;
    private ArrayList<HashMap<String, String>> data;
    private Context ctx;
	
	public CategoryAdapterHorizontal(Context context,ArrayList<HashMap<String, String>> d){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ctx = context;
        data = d;
	}
	
	@Override
	public int getCount() {
		return data.size();
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
			convertView = mInflater.inflate(R.layout.catagory_horizontal, null);

        	holder = new ViewHolder();
        	holder.layout = (RelativeLayout)convertView.findViewById(R.id.layout_category_horizontal);
			holder.img_category1 = (ImageView)convertView.findViewById(R.id.img_category_n_h);
			holder.txt_category1 = (TextView)convertView.findViewById(R.id.txt_category_n_h);
			holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressbar_category_horizontal);
        	convertView.setTag(holder);
		  }		  
	       else{
	        	holder =(ViewHolder)convertView.getTag();
	        }
		  
			aq =  new AQuery(convertView);
		  	holder.txt_category1.setText(data.get(position).get("name"));
		  	aq.id(holder.img_category1).progress(holder.progressBar).image(data.get(position).get("image"), true, true, 0, 0, null, 0);
		  	
		  	/*imageLoader.displayImage(data.get(position).get("image"), holder.img_category1,options , new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted() {
					holder.progressBar.setVisibility(View.GONE);
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

					holder.progressBar.setVisibility(View.GONE);
					//imageView.setImageResource(R.drawable.img_banner);
				}

				@Override
				public void onLoadingComplete(Bitmap loadedImage) {
					holder.progressBar.setVisibility(View.GONE);
				}
			});

		  	*/
		  	
		  	aq.id(holder.layout).clicked(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MainActivity main = (MainActivity) ctx;
					main.showCategoryDetail(data.get(pos).get("id"), data.get(pos).get("name"));
					//Toast.makeText(ctx, ""+data.get(pos).get("id"), Toast.LENGTH_SHORT).show();
					
				}
			});		 
		  	
		return convertView;
	}
	
	   static class ViewHolder{
		    RelativeLayout layout;
	        ImageView img_category1;
	        TextView txt_category1;
	        ProgressBar progressBar;
	    }
}
