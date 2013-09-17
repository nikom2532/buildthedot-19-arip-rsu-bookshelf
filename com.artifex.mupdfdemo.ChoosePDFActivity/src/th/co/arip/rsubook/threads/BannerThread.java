package th.co.arip.rsubook.threads;

import th.co.arip.rsubook.adapter.BannerAdapter;
import th.co.arip.rsubook.utils.Config;
import android.os.Handler;
import android.support.v4.view.ViewPager;

public class BannerThread extends Thread {
	private ViewPager viewPager;
	private Handler handler;
	private BannerAdapter bannerAdapter;
	
	public BannerThread(ViewPager banner,BannerAdapter adapter) {
		viewPager = banner;
		handler = new Handler();
		bannerAdapter = adapter;
	}
	
    @Override
    public void run() {
    	while(true) {
            try {
				sleep(Config.AD_DURATION);
				if (viewPager.getChildCount()>0) {
					if (viewPager.getCurrentItem()==bannerAdapter.getCount()-1) {
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								viewPager.setCurrentItem(0);
								
							}
						});
					}
					else{
						handler.post(new Runnable() {
							
							@Override
							public void run() {		 
								viewPager.setCurrentItem(viewPager.getCurrentItem()+1);				    

							}
						});
					}
				}
				
			} catch (InterruptedException e) {
				
			}
          }
    }
}
