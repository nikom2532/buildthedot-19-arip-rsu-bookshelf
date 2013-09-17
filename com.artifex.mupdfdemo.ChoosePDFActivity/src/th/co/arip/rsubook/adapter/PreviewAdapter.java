package th.co.arip.rsubook.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.androidquery.AQuery;

import th.co.arip.rsubook.R;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PreviewAdapter extends PagerAdapter {	
	private LayoutInflater mInflater;
    private ArrayList<HashMap<String, String>> data;
    private AQuery aq;
	
	public PreviewAdapter(Context context,ArrayList<HashMap<String, String>> d){
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        data = d;
		}
		
    @Override
    public int getCount() {
    	return data.size()/2;
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
        View imageLayout = mInflater.inflate(R.layout.preview, null);
		aq = new AQuery(imageLayout);
		if ((position*2)<data.size()) {
			aq.id(R.id.img_left_preview).progress(R.id.progressbar1).image(data.get(position*2).get("img"), true, true, 0, 0, null, 0);    	
		}
			if ((position*2)+1<data.size()) {
			aq.id(R.id.img_right_preview).progress(R.id.progressbar2).image(data.get(((position*2)+1)).get("img"), true, true, 0, 0, null, 0); 
			}
      ((ViewPager) container).addView(imageLayout, 0);

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
