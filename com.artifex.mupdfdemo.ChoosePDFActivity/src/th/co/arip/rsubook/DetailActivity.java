package th.co.arip.rsubook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import th.co.arip.db.BookHandler;
import th.co.arip.rsubook.adapter.PreviewAdapter;
import th.co.arip.rsubook.adapter.PreviewHorizontalAdapter;
import th.co.arip.rsubook.utils.Config;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.artifex.mupdfdemo.MuPDFActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;

public class DetailActivity extends Activity {
	private AQuery aq;
	private ProgressDialog mProgressDialog;
	private ViewPager vp;
	private PreviewAdapter adapter;
	private PreviewHorizontalAdapter adapter_horz;
	private String id,name,summary,datetime,cover,size;
	private String pdf;
	private BookHandler bh;
	private String revision;
	
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        aq = new AQuery(this);
        bh = new BookHandler(this);
        bh.open();
        
        id = getIntent().getExtras().getString("id");
        
    	if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT) {
    		vp = (ViewPager)findViewById(R.id.preview_detail);
    		loadDetail(id);
    	}
    	else{
            vp = (ViewPager)findViewById(R.id.preview_detailland);
    		loadDetailHorz(id);
    	}
            
    	if (!bh.check(id)) {
			aq.id(R.id.btn_download_detail).text("Read");
		}
    	
        if (getIntent().getExtras().containsKey("tag")) {
			aq.id(R.id.btn_download_detail).text("Update");
		}
    	
        aq.id(R.id.btn_download_detail).clicked(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!bh.check(id)&&!getIntent().getExtras().containsKey("tag")) {			
					openPdfIntent(bh.getPDF(id));
				}
				else if(!bh.check(id)&&getIntent().getExtras().containsKey("tag")){
					getDownloadLink(id);
				}
				else{
					getDownloadLink(id);
				}

			}
		});
    }

    private void loadDetail(String id){    
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		ArrayList<HashMap<String, String>> arrayImage = new ArrayList<HashMap<String,String>>();
                if(json != null){
                	try {
						if (json.getString("status").equals("1")) {
	           					name = json.getString("name");
	           					cover = json.getString("cover");
	           					summary = json.getString("summary");
	           					datetime = json.getString("datetime");
	           					size = json.getString("size");
	           					
	           					JSONArray jArr = json.getJSONArray("preview_pic");
	           					for (int i = 0; i < jArr.length(); i++) {
	           						HashMap<String, String> hash2 = new HashMap<String, String>();
									String img = jArr.getString(i);
									hash2.put("img", img);
									arrayImage.add(hash2);						
								}
	           					
	           					aq.id(R.id.image_cover_detail).image(cover, true, true, 0, 0, null, 0);    
	           					aq.id(R.id.txt_name_detail).text(name);
	           					aq.id(R.id.txt_summary_detail).text(summary);
	           					aq.id(R.id.txt_date_detail).text(datetime);
	           					aq.id(R.id.txt_size_detail).text(size+" MB");
						}
						else{
							Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
                	
                	aq.id(R.id.btn_download_detail).visible();
                	adapter = new PreviewAdapter(aq.getContext(), arrayImage);
                	vp.setAdapter(adapter);
                }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
        //pairs.add(new BasicNameValuePair("test", "1")); 
        pairs.add(new BasicNameValuePair("preview", "1")); 
        pairs.add(new BasicNameValuePair("id", id));
        pairs.add(new BasicNameValuePair("username", Config.USERNAME));
        pairs.add(new BasicNameValuePair("pgroup", Config.GROUP));
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.ajax(Config.LINK, params, JSONObject.class, cb);

    }
    
    private void loadDetailHorz(String id){      
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		ArrayList<HashMap<String, String>> arrayImage = new ArrayList<HashMap<String,String>>();
                if(json != null){
                	try {
						if (json.getString("status").equals("1")) {
	           					name = json.getString("name");
	           					cover = json.getString("cover");
	           					summary = json.getString("summary");
	           					datetime = json.getString("datetime");
	           					size = json.getString("size");
	           					
	           					JSONArray jArr = json.getJSONArray("preview_pic");
	           					for (int i = 0; i < jArr.length(); i++) {
	           						HashMap<String, String> hash2 = new HashMap<String, String>();
									String img = jArr.getString(i);
									hash2.put("img", img);
									arrayImage.add(hash2);						
								}
	           					
	           					aq.id(R.id.image_cover_detailland).image(cover, true, true, 0, 0, null, 0);    
	           					aq.id(R.id.txt_name_detail).text(name);
	           					aq.id(R.id.txt_summary_detailland).text(summary);
	           					aq.id(R.id.txt_date_detailland).text(datetime);
	           					aq.id(R.id.txt_size_detailland).text(size+" MB");
						}
						else{
							Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
                	
                  	aq.id(R.id.btn_download_detail).visible();
                	adapter_horz = new PreviewHorizontalAdapter(aq.getContext(), arrayImage);
                	vp.setAdapter(adapter_horz);
                }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
        //pairs.add(new BasicNameValuePair("test", "1")); 
        pairs.add(new BasicNameValuePair("preview", "1")); 
        pairs.add(new BasicNameValuePair("id", id));
        pairs.add(new BasicNameValuePair("username", Config.USERNAME));
        pairs.add(new BasicNameValuePair("pgroup", Config.GROUP));
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.ajax(Config.LINK, params, JSONObject.class, cb);

    }
    
    private void getDownloadLink(String id){    
		final ProgressDialog pd = ProgressDialog.show(this, null, "Preparing...");
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		pd.dismiss();
        		if (json!=null) {
        			try {
						if (json.getString("status").equals("1")) {
							pdf = json.getString("pdf_link");
							revision = json.getString("re_vision");
							/*String DB_PATH = Environment.getExternalStorageDirectory()+
			                		"/Android/data/th.co.arip.rsubook/"+pdf.hashCode()+"/book.pdf";
							openPdfIntent(DB_PATH);*/
							new DownloadFile().execute(pdf);					
						}
						else{
							Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
        	}
    	};
    	cb.header("User-Agent", "android");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("t", "1"));    
            pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
            //pairs.add(new BasicNameValuePair("test", "1"));  
            pairs.add(new BasicNameValuePair("id", id));
            pairs.add(new BasicNameValuePair("username", Config.USERNAME));
            pairs.add(new BasicNameValuePair("pgroup", Config.GROUP));
      
            HttpEntity entity = null;
    		try {
    			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(AQuery.POST_ENTITY, entity);
            
            aq.ajax(Config.LINK, params, JSONObject.class, cb);
    }
    
    private void openPdfIntent(String path) {
    	TextView txtTitle = (TextView)findViewById(R.id.txt_name_detail);
		Uri uri = Uri.parse(path);
		Intent intent = new Intent(this,MuPDFActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		intent.putExtra("title", txtTitle.getText().toString());
		startActivity(intent);
    }
    
    private class DownloadFile extends AsyncTask<String, Integer, String> {
    	
    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            // instantiate it within the onCreate method
            mProgressDialog = new ProgressDialog(DetailActivity.this);
            mProgressDialog.setMessage("Downloading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
    	
    	   @Override
    	    protected void onProgressUpdate(Integer... progress) {
    	        super.onProgressUpdate(progress);
    	        mProgressDialog.setProgress(progress[0]);
    	    }
    	
        @Override
        protected String doInBackground(String... sUrl) {
        	String DB_PATH = Environment.getExternalStorageDirectory()+
            		"/Android/data/th.co.arip.rsubook/"+id+"/";
            try {
                URL url = new URL(sUrl[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                // download the file
                File f = new File(DB_PATH);
                if (!f.exists()) {
					f.mkdirs();
				}
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(DB_PATH+"book.pdf");

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
        	mProgressDialog.dismiss();
        	String DB_PATH = Environment.getExternalStorageDirectory()+
            		"/Android/data/th.co.arip.rsubook/"+id+"/";
            bh.open();
            if (bh.check(id)) {
            	   bh.addFavorite(id, name, summary, datetime, DB_PATH+"book.pdf", cover, revision);
			}
            if (getIntent().getExtras().containsKey("tag")) {
            	bh.updateBook(id, revision);
            	aq.id(R.id.btn_download_detail).clicked(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						openPdfIntent(bh.getPDF(id));
						
					}
				});
			}
			aq.id(R.id.btn_download_detail).text("Read");
        	
        }
    }
    
    @Override
    protected void onDestroy(){       
        super.onDestroy();
        bh.close();
        	if(isTaskRoot()){
                AQUtility.cleanCacheAsync(this);
        	}
    } 
}
