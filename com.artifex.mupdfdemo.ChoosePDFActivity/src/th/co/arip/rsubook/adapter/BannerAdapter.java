package th.co.arip.rsubook.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.androidquery.AQuery;

import th.co.arip.rsubook.R;
import th.co.arip.rsubook.WebActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class BannerAdapter extends PagerAdapter {
	private static final int VIEW_BOOK = 1;
	private static final int VIEW_GROUP = 2;
	private static final int VIEW_CATEGORY = 3;
	private static final int TEST = 4;
	private static final int TEST2 = 5;
	
	private LayoutInflater mInflater;
    private ArrayList<HashMap<String, String>> data;
    private Context ctx;
    private AQuery aq;
	
	public BannerAdapter(Context context,ArrayList<HashMap<String, String>> d){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ctx = context;
        data = d;
		}
		
    @Override
    public int getCount() {
    	return data.size();
    }
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}



    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View imageLayout = mInflater.inflate(R.layout.banner, null);
		aq = new AQuery(imageLayout);
		aq.id(R.id.img_banner).progress(R.id.progressbar_banner).image(data.get(position).get("picture"), true, true, 0, 0, null, 0);    
		
      ((ViewPager) container).addView(imageLayout, 0);

          final int pos = position;
          if(!data.get(pos).get("click").equals("0")) {
        	  aq.id(R.id.img_banner).clicked(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    		    String click_mode = data.get(pos).get("click");
    			switch (Integer.parseInt(click_mode)) {
				case VIEW_BOOK:
					Toast.makeText(ctx, ""+VIEW_BOOK, Toast.LENGTH_SHORT).show();
					break;
				case VIEW_GROUP:
					Toast.makeText(ctx, ""+VIEW_GROUP, Toast.LENGTH_SHORT).show();
					break;
				case VIEW_CATEGORY:
					Toast.makeText(ctx, ""+VIEW_CATEGORY, Toast.LENGTH_SHORT).show();
					break;
				case TEST:
					Intent intent = new Intent(ctx, WebActivity.class);
					intent.putExtra("link", data.get(pos).get("link"));
					ctx.startActivity(intent);
					break;
				case TEST2:
					ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.get(pos).get("link"))));
					break;
			}
    		}
    	});
      }

      return imageLayout;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View container) {
	}
}
