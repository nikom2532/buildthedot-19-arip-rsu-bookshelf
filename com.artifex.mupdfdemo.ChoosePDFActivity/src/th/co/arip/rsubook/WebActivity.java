package th.co.arip.rsubook;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;

public class WebActivity extends Activity {
	private WebView wv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up, R.anim.hold);
        setContentView(R.layout.web);
        
		String link = getIntent().getExtras().getString("link");
        
		wv = (WebView) findViewById(R.id.webview);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new SafeWebView());
		wv.loadUrl(link);
    }
    
    private class SafeWebView extends WebViewClient{
    	private ProgressDialog pd;
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
				pd = ProgressDialog.show(view.getContext(), null, "Loading", true, true);

				pd = ProgressDialog.show(view.getContext(), null, "Loading", true, true);

			
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
				pd.dismiss();
		}
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
        overridePendingTransition(R.anim.hold, R.anim.slide_down);
    }
}
