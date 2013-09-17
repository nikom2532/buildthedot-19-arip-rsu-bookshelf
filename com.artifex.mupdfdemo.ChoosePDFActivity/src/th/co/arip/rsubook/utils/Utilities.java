package th.co.arip.rsubook.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Utilities {
	private Context mContext;
	
	public Utilities(Context context) {
		mContext = context;
	}
	
    public int getScreenWidth(){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int width = displaymetrics.widthPixels;
		return width;
	}
    
    public int getScreenHeight(){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		return height;
	}
    
    public float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}
