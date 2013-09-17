package th.co.arip.rsubook;

import java.io.File;
import java.io.UnsupportedEncodingException;
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

import th.co.arip.db.Book;
import th.co.arip.db.BookHandler;
import th.co.arip.rsubook.adapter.BannerAdapter;
import th.co.arip.rsubook.adapter.CategoryAdapter;
import th.co.arip.rsubook.adapter.CategoryAdapterHorizontal; 
import th.co.arip.rsubook.adapter.GridAdapter;
import th.co.arip.rsubook.adapter.LibraryAdapter;
import th.co.arip.rsubook.threads.BannerThread;
import th.co.arip.rsubook.utils.Config;
import th.co.arip.rsubook.utils.Utilities;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.pheonec.playpark.quickaction.ActionItem;
import com.pheonec.playpark.quickaction.QuickAction;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class MainActivity extends Activity implements OnClickListener {
	private Utilities utils;
	
	private EditText edtSearch;
	private ImageView btnSearch;
	private ImageView btnDelete;
	private TextView user;
	private ImageView login;
	private TextView date;
	private TextView title;
	private AQuery aq;
	private ViewFlipper vf;
	private ImageButton btnHome,btnLibrary;
	
	private RelativeLayout layout_banner;
	private ProgressBar pb_banner;
	private ViewPager vp_banner;
	private BannerAdapter adapter_banner;
	private BannerThread thread_banner;
	
	private RelativeLayout layout_features;
	private ImageView btnFeature,btnPopular,btnNewArrival; 
	
	private RelativeLayout layout_category;
	private ProgressBar pb_category;
	private ListView list;
	private CategoryAdapter adapter_category;
	private CategoryAdapterHorizontal adapter_category_horizontal;
	
	private EditText edtSearch2;
	private ImageView btnSearch2;
	private ImageView btnDelete2;
	private QuickAction quickAction;
	
	public static String DATE = "date";
	public static String TITLE = "title";
	public static String CURRENT_SORT = "date";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }    
    
    private void init(){
    	utils = new Utilities(this);
        aq = new AQuery(this);
        edtSearch = (EditText)findViewById(R.id.edt_search);
        btnSearch = (ImageView)findViewById(R.id.img_search);
        btnDelete = (ImageView)findViewById(R.id.img_delete);
        edtSearch2 = (EditText)findViewById(R.id.edt_search2);
        btnSearch2 = (ImageView)findViewById(R.id.img_search2);
        btnDelete2 = (ImageView)findViewById(R.id.img_delete2);
        user = (TextView)findViewById(R.id.txt_username);
        login = (ImageView)findViewById(R.id.img_login);
        date = (TextView)findViewById(R.id.txt_date);
        title = (TextView)findViewById(R.id.txt_title);
        vf = (ViewFlipper)findViewById(R.id.layout_body);
        btnHome = (ImageButton)findViewById(R.id.btn_home);
        btnLibrary = (ImageButton)findViewById(R.id.btn_library);
        btnFeature = (ImageView)findViewById(R.id.btn_features_home); 
        btnPopular = (ImageView)findViewById(R.id.btn_popular_home);
        btnNewArrival = (ImageView)findViewById(R.id.btn_newarrival_home);
        //list_features = (HorizontalListView)findViewById(R.id.list_features);
        login.setOnClickListener(new LoginOnClickListener());
        btnSearch.setOnClickListener(this);
        btnSearch2.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        btnLibrary.setOnClickListener(this);
        btnFeature.setOnClickListener(this);
        btnPopular.setOnClickListener(this);
        btnNewArrival.setOnClickListener(this);
        
        edtSearch.addTextChangedListener(new TextWatcherListener(btnDelete));
        btnDelete.setOnClickListener(new DeleteOnClickListener(edtSearch));
        edtSearch2.addTextChangedListener(new TextWatcherListener(btnDelete2));
        btnDelete2.setOnClickListener(new DeleteOnClickListener(edtSearch2));
        
        date.setOnClickListener(new SortOnClickListener());
        title.setOnClickListener(new SortOnClickListener());
        
        edtSearch.setOnEditorActionListener(new SearchOnClickListener(1));
        edtSearch2.setOnEditorActionListener(new SearchOnClickListener(2));
        showHome();
    }
    
    private void showHome(){
    	layout_banner  = (RelativeLayout)findViewById(R.id.layout_banner);
    	pb_banner = (ProgressBar)findViewById(R.id.progressbar_banner);
    	vp_banner = (ViewPager)findViewById(R.id.banner);
    	
    	layout_features = (RelativeLayout)findViewById(R.id.layout_features);
    	
    	layout_category  = (RelativeLayout)findViewById(R.id.layout_categories);
    	pb_category = (ProgressBar)findViewById(R.id.progressbar_list_catagory);
    	list = (ListView)findViewById(R.id.list_category);    	
		
    	if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT) {
    		layout_banner.getLayoutParams().width = (int) (utils.getScreenWidth()-utils.convertDpToPixel(20, this));
    		layout_banner.getLayoutParams().height = (int) (layout_banner.getLayoutParams().width*(1.0f/2.0f)); 

        	list.setAdapter(adapter_category);
		}  
    	else {
        	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
        			,RelativeLayout.LayoutParams.WRAP_CONTENT);
        	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);   
        	params.setMargins((int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this), (int) utils.convertDpToPixel(10, this), 0);
        	layout_banner.setLayoutParams(params);
        	layout_banner.getLayoutParams().width = (int) (utils.getScreenHeight()-utils.convertDpToPixel(20, this));
        	layout_banner.getLayoutParams().height = (int) (layout_banner.getLayoutParams().width*(1.0f/2.0f)); 
        	
        	RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
        			,RelativeLayout.LayoutParams.MATCH_PARENT);
        	params1.addRule(RelativeLayout.BELOW,R.id.layout_banner);
        	params1.addRule(RelativeLayout.ALIGN_LEFT,R.id.layout_banner);
        	params1.addRule(RelativeLayout.ALIGN_RIGHT,R.id.layout_banner);
        	params1.setMargins(0, (int)utils.convertDpToPixel(10, this), 0, (int)utils.convertDpToPixel(10, this));
        	layout_features.setLayoutParams(params1);
        	
        	RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
        			,RelativeLayout.LayoutParams.WRAP_CONTENT);
        	params2.addRule(RelativeLayout.RIGHT_OF, R.id.layout_banner);   
        	params2.setMargins(0, (int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this));
        	layout_category.setLayoutParams(params2);                                         
                         
    		list.setAdapter(adapter_category_horizontal);
		}
    	
    		loadBanner();
    		loadFeatures();
    		loadCategory();
    }
    
    public void showLibrary(String sort){
       TextView txt = (TextView)findViewById(R.id.txt_header_library);
        Typeface tf = Typeface.createFromAsset(getAssets(),"DB Helvethaica X Blk Cond.ttf");
        txt.setTypeface(tf,Typeface.BOLD);
        txt.setTextSize(28); 
        txt.setText("Library");
        CURRENT_SORT = sort;
        new loadSavedFavorite().execute(CURRENT_SORT);
    }
    
    private boolean isNetworkConnected() {
    	  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	  NetworkInfo ni = cm.getActiveNetworkInfo();
    	  if (ni == null) {
    	   return false;
    	  } else
    	   return true;
    }
    
    private void showUpdatedLib(final ArrayList<HashMap<String, String>> compare){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>() {
    		@Override
    		public void callback(String url, JSONObject json,
    				AjaxStatus status) {
    			super.callback(url, json, status);
    	      	ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
                   if(json != null){
               		try {
           			if (json.getString("status").equals("1")) {
           				JSONArray c = json.getJSONArray("data");
           				for(int i=0;i<c.length();i++){
           					HashMap<String, String> hash = new HashMap<String, String>();
           					JSONObject json2 = c.getJSONObject(i);
           					String id = json2.getString("id");
           					String re_vision = json2.getString("re_vision");	
           					hash.put("id",id);
           					hash.put("revision",re_vision);
           					arrayList.add(hash);
           				}          				     				
           			}
           			
           			else{
           				Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
           			}      
    	        	
    	        	GridView grid =  (GridView)findViewById(R.id.grid_library);      
    	        	LibraryAdapter adapter = new LibraryAdapter(MainActivity.this, compare, arrayList);
    		        grid.setAdapter(adapter);	
    	        	
           		} catch (JSONException e) {
           				
           		} catch (RuntimeException e) {
           			
           	} catch (Exception e) {
           		
           		}              		
            		
                   }else{                      
                       Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
               }                            
            }
		};
    	cb.header("User-Agent", "android");

    	//Toast.makeText(this, gid.substring(0, gid.length()-1), Toast.LENGTH_SHORT).show();
    	
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
        pairs.add(new BasicNameValuePair("type", "4")); 
    	if (compare.size()>0) {
        	String gid = "";
        	for (int i = 0; i < compare.size(); i++) {
            	gid += compare.get(i).get("id")+",";
    		}
            pairs.add(new BasicNameValuePair("gid", gid.substring(0, gid.length()-1))); 
		}

  
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
    
    private class loadSavedFavorite extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>>{
    	private BookHandler bookHandler;
    	
    	@Override
		protected void onPreExecute() {
			super.onPreExecute();
			bookHandler = new BookHandler(aq.getContext());
			bookHandler.open();
		}
		
		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
			int type = 0;
			if( params[0].equals(TITLE) )	type = 1;
			List<Book> data = bookHandler.sort(type);
			ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
			for (int i = 0; i < data.size(); i++) {
				HashMap<String, String> hash = new HashMap<String, String>();			
					hash.put("id", data.get(i).getId());
					hash.put("name", data.get(i).getTitle());
					hash.put("datetime", data.get(i).getDate());
					hash.put("description",data.get(i).getDetail());
					hash.put("category",data.get(i).getCategory());
					hash.put("image",data.get(i).getImage());
					hash.put("revision",data.get(i).getRevision());
					arrayList.add(hash);
			}
			
			return arrayList;
		}
		
		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
			super.onPostExecute(result);        		
	        GridView grid =  (GridView)findViewById(R.id.grid_library);        
	        if (isNetworkConnected()) {
	        	LibraryAdapter adapter = new LibraryAdapter(MainActivity.this, result, null);
		        grid.setAdapter(adapter);
		        
				showUpdatedLib(result);
			}
	        else{
		        LibraryAdapter adapter = new LibraryAdapter(MainActivity.this, result, null);
		        grid.setAdapter(adapter);	
	        }
		}
		
	}
       
    
    private void loadBanner(){      
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>() {
    		@Override
    		public void callback(String url, JSONObject json,
    				AjaxStatus status) {
    			super.callback(url, json, status);
    	      	ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
            	pb_banner.setVisibility(View.GONE);
                   if(json != null){
               		try {
           			if (json.getString("status").equals("1")) {
           				JSONArray c = json.getJSONArray("data");
           				for(int i=0;i<c.length();i++){
           					HashMap<String, String> hash = new HashMap<String, String>();
           					JSONObject json2 = c.getJSONObject(i);
           					String id = json2.getString("id");
           					String click = json2.getString("click");
           					String picture = json2.getString("picture");
           					String link = json2.getString("link");
           					hash.put("id",id);
           					hash.put("click",click);
           					hash.put("picture", picture);			
           					hash.put("link", link);	
           					arrayList.add(hash);
           				}          				     				
           			}
           			
           			else{
           				Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
           			}
           		} catch (JSONException e) {
           				
           		} catch (RuntimeException e) {
           			
           	} catch (Exception e) {
           		
           		}
               		
                	adapter_banner = new BannerAdapter(aq.getContext(), arrayList);
                	vp_banner.setAdapter(adapter_banner);
                	if (vp_banner.getChildCount()>0) {
                    	thread_banner = new BannerThread(vp_banner, adapter_banner);
                    	thread_banner.start();
					}
            		
                   }else{                      
                       Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
               }                            
            }
		};
    	cb.header("User-Agent", "android");
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "2"));    
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
  
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
    
    private void loadFeatures(){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	arrayList.clear();
        		 if(json != null){
            			try {
							if (json.getString("status").equals("1")) {
								JSONArray c = json.getJSONArray("data");
		           				for(int i=0;i<c.length();i++){
		           					HashMap<String, String> hash = new HashMap<String, String>();
		           					JSONObject json2 = c.getJSONObject(i);
		           					String id = json2.getString("id");
		           					String name = json2.getString("name");
		           					String image = json2.getString("image");
		           					String st = json2.getString("status");
		           					hash.put("id",id);
		           					hash.put("name",name);
		           					hash.put("image", image);	
		           					hash.put("status", st);	
		           					arrayList.add(hash);   
		           				}          				     				
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
            			
            			LinearLayout horz = (LinearLayout)findViewById(R.id.horz);
            			horz.removeAllViews();
            			
            			for (int a = 0; a < arrayList.size(); a++) {
            				View v = getLayoutInflater().inflate(R.layout.book, null);
                			ImageView img = (ImageView) v.findViewById(R.id.img_book);
                			ProgressBar pb = (ProgressBar) v.findViewById(R.id.pg_book);
                			ImageView tag = (ImageView) v.findViewById(R.id.img_status_book);
                			final int pos = a;
                			img.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(v.getContext(), DetailActivity.class);
									intent.putExtra("id", arrayList.get(pos).get("id"));
									startActivity(intent);
									
								}
							});
                			
                			LinearLayout.LayoutParams param0 = new LinearLayout.LayoutParams((int)utils.convertDpToPixel(150
                					, aq.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);                		
                			horz.addView(v,param0);
                			aq.id(img).progress(pb).image(arrayList.get(a).get("image"), true, true, 0, 0, null, 0); 
                			if (arrayList.get(a).get("status").equals("New")) {
								aq.id(tag).visible();
							}
						}

        		 }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("type", "1"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.progress(R.id.progressBar_horizontal).ajax(Config.LINK, params, JSONObject.class, cb);     
    }
    
    private void loadPopular(){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
            	final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	arrayList.clear();
        		 if(json != null){
            			try {
							if (json.getString("status").equals("1")) {
								JSONArray c = json.getJSONArray("data");
		           				for(int i=0;i<c.length();i++){
		           					HashMap<String, String> hash = new HashMap<String, String>();
		           					JSONObject json2 = c.getJSONObject(i);
		           					String id = json2.getString("id");
		           					String name = json2.getString("name");
		           					String image = json2.getString("image");
		           					String st = json2.getString("status");
		           					hash.put("id",id);
		           					hash.put("name",name);
		           					hash.put("image", image);	
		           					hash.put("status", st);				
		           					arrayList.add(hash);   
		           				}          				     				
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
            			
            			LinearLayout horz = (LinearLayout)findViewById(R.id.horz);
            			horz.removeAllViews();
            			for (int a = 0; a < arrayList.size(); a++) {
            				View v = getLayoutInflater().inflate(R.layout.book, null);
                			ImageView img = (ImageView) v.findViewById(R.id.img_book);
                			ProgressBar pb = (ProgressBar) v.findViewById(R.id.pg_book);
                			ImageView tag = (ImageView) v.findViewById(R.id.img_status_book);
                			final int pos = a;
                			img.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(v.getContext(), DetailActivity.class);
									intent.putExtra("id", arrayList.get(pos).get("id"));
									startActivity(intent);
									
								}
							});
                			
                			LinearLayout.LayoutParams param0 = new LinearLayout.LayoutParams((int)utils.convertDpToPixel(150
                					, aq.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);                		
                			horz.addView(v,param0);
                			aq.id(img).progress(pb).image(arrayList.get(a).get("image"), true, true, 0, 0, null, 0); 
                			if (arrayList.get(a).get("status").equals("New")) {
								aq.id(tag).visible();
							}
						}

        		 }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("type", "2"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.progress(R.id.progressBar_horizontal).ajax(Config.LINK, params, JSONObject.class, cb);
    }
    
    private void loadNewArrival(){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
            	final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	arrayList.clear();
        		 if(json != null){
            			try {
							if (json.getString("status").equals("1")) {
								JSONArray c = json.getJSONArray("data");
		           				for(int i=0;i<c.length();i++){
		           					HashMap<String, String> hash = new HashMap<String, String>();
		           					JSONObject json2 = c.getJSONObject(i);
		           					String id = json2.getString("id");
		           					String name = json2.getString("name");
		           					String image = json2.getString("image");
		           					String st = json2.getString("status");
		           					hash.put("id",id);
		           					hash.put("name",name);
		           					hash.put("image", image);	
		           					hash.put("status", st);				
		           					arrayList.add(hash);   
		           				}          				     				
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
            			
            			LinearLayout horz = (LinearLayout)findViewById(R.id.horz);
            			horz.removeAllViews();
            			for (int a = 0; a < arrayList.size(); a++) {
            				View v = getLayoutInflater().inflate(R.layout.book, null);
                			ImageView img = (ImageView) v.findViewById(R.id.img_book);
                			ProgressBar pb = (ProgressBar) v.findViewById(R.id.pg_book);
                			ImageView tag = (ImageView) v.findViewById(R.id.img_status_book);
                			final int pos = a;
                			img.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(v.getContext(), DetailActivity.class);
									intent.putExtra("id", arrayList.get(pos).get("id"));
									startActivity(intent);
									
								}
							});
                			
                			LinearLayout.LayoutParams param0 = new LinearLayout.LayoutParams((int)utils.convertDpToPixel(150
                					, aq.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);                		
                			horz.addView(v,param0);
                			aq.id(img).progress(pb).image(arrayList.get(a).get("image"), true, true, 0, 0, null, 0); 
                			if (arrayList.get(a).get("status").equals("New")) {
								aq.id(tag).visible();
							}
						}

        		 }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("type", "3"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.progress(R.id.progressBar_horizontal).ajax(Config.LINK, params, JSONObject.class, cb);
    }
    
    private void loadCategory(){       	
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){         
    		 @Override
             public void callback(String url, JSONObject json, AjaxStatus status) {
             	ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
             	pb_category.setVisibility(View.GONE);
                    if(json != null){
                		try {
            			if (json.getString("status").equals("1")) {

            				JSONArray c = json.getJSONArray("data");
            				for(int i=0;i<c.length();i++){
            					HashMap<String, String> hash = new HashMap<String, String>();
            					JSONObject json2 = c.getJSONObject(i);
            					String subcategory = json2.getString("subcategory");
            					if (subcategory.equals("1")) {
                					String id = json2.getString("id");
                					String name = json2.getString("name");
                					String image = json2.getString("image");        					
                					hash.put("id",id);
                					hash.put("name",name);
                					hash.put("image", image);			
                					arrayList.add(hash);      
                				}          				   
								}
  				
            			}
            			
            			else{
            				Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
            			}
            		} catch (JSONException e) {
            				
            		} catch (RuntimeException e) {
            			
            	} catch (Exception e) {
            		
            		}
                 	adapter_category = new CategoryAdapter(aq.getContext(), arrayList);
             		adapter_category_horizontal = new CategoryAdapterHorizontal(aq.getContext(), arrayList); 
               	 	
                 	if (getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT) {
                     	list.setAdapter(adapter_category);
                 	}
                 	else{
                     	list.setAdapter(adapter_category_horizontal);
                 	}
                		
                    }else{                      
                        Toast.makeText(aq.getContext(), "Please check  your internet connection", Toast.LENGTH_LONG).show();
                }                            
             }
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "0"));    
        pairs.add(new BasicNameValuePair("type", "1"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
  
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        aq.progress(R.id.progressbar_list_catagory).ajax(Config.LINK, params, JSONObject.class, cb);
            
    }
    
    private void loadCategoryDetail(String cat_id,String cat_name){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){     
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	arrayList.clear();
        		 if(json != null){
            			try {
							if (json.getString("status").equals("1")) {
								JSONArray c = json.getJSONArray("data");
		           				for(int i=0;i<c.length();i++){
		           					HashMap<String, String> hash = new HashMap<String, String>();
		           					JSONObject json2 = c.getJSONObject(i);
		           					String id = json2.getString("id");
		           					String name = json2.getString("name");
		           					String image = json2.getString("image");
		           					String st = json2.getString("status");
		           					hash.put("id",id);
		           					hash.put("name",name);
		           					hash.put("image", image);	
		           					hash.put("status", st);
		           					arrayList.add(hash);   
		           				}          				     				
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
   
            			GridAdapter adapter = new GridAdapter(aq.getContext(), arrayList);
            			GridView gv = (GridView)findViewById(R.id.gridview);
            			gv.setAdapter(adapter);

        		 }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("type", "5"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
        pairs.add(new BasicNameValuePair("cat_id", cat_id));
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
        
        aq.progress(R.id.progressbar_list_catagory).ajax(Config.LINK, params, JSONObject.class, cb);
    }
    
    private void loadAllCat(){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
    		 @Override
             public void callback(String url, JSONObject json, AjaxStatus status) {           	
             	pb_category.setVisibility(View.GONE);
                    if(json != null){                    	
                		try {
            			if (json.getString("status").equals("1")) { 
            				ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();
            				//ArrayList<HashMap<String, String>> arrayList2 = new ArrayList<HashMap<String,String>>();
            				JSONArray c = json.getJSONArray("data");  
        					Log.e("c", c.toString());
            				for(int i=0;i<c.length();i++){
            					HashMap<String, String> hash = new HashMap<String, String>();
            					JSONObject json2 = c.getJSONObject(i);
            					String id = json2.getString("id");
            					String name = json2.getString("name");
            					String image = json2.getString("image");
            					String subcategory = json2.getString("subcategory");
            					//String parent = json2.getString("parent");
            					hash.put("id",id);
            					hash.put("name",name);
            					hash.put("image", image);	
            					if (!subcategory.equals("1")) {
            						hash.put("subcategory", subcategory);
								}
            					//hash.put("sub_category",sub_category);
            					//hash.put("parent", parent);
           					
//            					if (!json2.isNull("subcategory")) {  
//            						JSONArray c1 = json2.getJSONArray("subcategory");
//            						for (int j = 0; j < c1.length(); j++) {
//            						    HashMap<String, String> hash2 = new HashMap<String, String>();
//            							JSONObject json3 = c1.getJSONObject(i);
//                    					String id1 = json3.getString("id");
//                    					String name1 = json3.getString("name");
//                    					String image1 = json3.getString("image");
//                    					hash2.put("id",id1);
//                    					hash2.put("name",name1);
//                    					hash2.put("image", image1);
//                    					arrayList2.add(hash2);      
//									}       						
//								}
            					
            					arrayList.add(hash); 
 
            				}       
            				addQuickactionbutton(arrayList);
            			}
            			
            			else{
            				Toast.makeText(aq.getContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
            			}
            		} catch (JSONException e) {
            				
            		} catch (RuntimeException e) {
            			
            	} catch (Exception e) {
            		
            		}
                		
                		
                    }else{                      
                        Toast.makeText(aq.getContext(), "Please check  your internet connection", Toast.LENGTH_LONG).show();
                }                            
             }
    	};
    	cb.header("User-Agent", "android");
    	 List<NameValuePair> pairs = new ArrayList<NameValuePair>();
         pairs.add(new BasicNameValuePair("t", "0"));    
         pairs.add(new BasicNameValuePair("type", "1"));  
         pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
   
         HttpEntity entity = null;
 		try {
 			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
         Map<String, Object> params = new HashMap<String, Object>();
         params.put(AQuery.POST_ENTITY, entity);
         
         aq.ajax(Config.LINK, params, JSONObject.class, cb);
         aq.id(R.id.layout_login).invisible();
         aq.id(R.id.layout_sort).invisible();
    }
    
    private void loadSearchDetail(String word){
    	AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){     
    		@Override
        	public void callback(String url, JSONObject json,
        			AjaxStatus status) {
        		super.callback(url, json, status);
        		final ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	arrayList.clear();
        		 if(json != null){
            			try {
							if (json.getString("status").equals("1")) {
								JSONArray c = json.getJSONArray("data");
		           				for(int i=0;i<c.length();i++){
		           					HashMap<String, String> hash = new HashMap<String, String>();
		           					JSONObject json2 = c.getJSONObject(i);
		           					String id = json2.getString("id");
		           					String name = json2.getString("name");
		           					String image = json2.getString("image");
		           					String st = json2.getString("status");
		           					hash.put("id",id);
		           					hash.put("name",name);
		           					hash.put("image", image);	
		           					hash.put("status", st);
		           					arrayList.add(hash);   
		           				}          				     				
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
   
            			GridAdapter adapter = new GridAdapter(aq.getContext(), arrayList);
            			GridView gv = (GridView)findViewById(R.id.gridsearch);
            			gv.setAdapter(adapter);

        		 }
        	}
    	};
    	cb.header("User-Agent", "android");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("t", "1"));    
        pairs.add(new BasicNameValuePair("type", "6"));  
        pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE)); 
        pairs.add(new BasicNameValuePair("q", word));
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
        
        aq.progress(R.id.progressbar_list_catagory).ajax(Config.LINK, params, JSONObject.class, cb);
        aq.id(R.id.layout_login).invisible();
        aq.id(R.id.layout_sort).invisible();
    }
    
	private void addQuickactionbutton(final ArrayList<HashMap<String, String>> category){	
		quickAction = new QuickAction(this, QuickAction.VERTICAL);
		
		for (int a = 0; a < category.size(); a++) {
			File ext = Environment.getExternalStorageDirectory();
			File target = new File(ext, "Android/data/"+getPackageName()+"/cache/"+String.valueOf(category.get(a).get("image").hashCode()));        
			aq.download(category.get(a).get("image"), target, new AjaxCallback<File>(){

		        public void callback(String url, File file, AjaxStatus status) {
		                if(file != null){

		                }
		        }     
		});		
			ActionItem ac = null;
			if (category.get(a).get("subcategory")!=null) {
				if (category.get(a).get("subcategory").equals("2")) {
					ac = new ActionItem(a, "     "+category.get(a).get("name"), null);
				}
				if (category.get(a).get("subcategory").equals("3")) {
					ac = new ActionItem(a, "          "+category.get(a).get("name"), null);
				}
				
			}
			else{
				ac = new ActionItem(a, category.get(a).get("name"), Drawable.createFromPath(target.getAbsolutePath()));
			}
			quickAction.addActionItem(ac); 
		}
		
	    
		quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {			
			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {				
				ActionItem actionItem = quickAction.getActionItem(pos);
				//Toast.makeText(MainActivity.this, category.get(actionItem.getActionId()).get("id"), Toast.LENGTH_SHORT).show();
				showCategoryDetail(category.get(actionItem.getActionId()).get("id"), category.get(actionItem.getActionId()).get("name"));
			}
		});
        aq.id(R.id.header_showcategory).clickable(true);
	}
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	   if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
    		   if (quickAction!=null) {
    			   quickAction.dismiss();
    		   }
    		   layout_banner.getLayoutParams().width = (int) (utils.getScreenWidth()-utils.convertDpToPixel(20, this));
    		   layout_banner.getLayoutParams().height = (int) (layout_banner.getLayoutParams().width*(1.0f/2.0f)); 
           		
           	RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
        			,RelativeLayout.LayoutParams.WRAP_CONTENT);
        	params1.addRule(RelativeLayout.BELOW,R.id.layout_banner);
        	params1.addRule(RelativeLayout.ALIGN_LEFT,R.id.layout_banner);
        	params1.addRule(RelativeLayout.ALIGN_RIGHT,R.id.layout_banner);
        	params1.setMargins(0, (int)utils.convertDpToPixel(10, this), 0, (int)utils.convertDpToPixel(10, this));
        	layout_features.setLayoutParams(params1);
    		   
            	RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
            			,RelativeLayout.LayoutParams.WRAP_CONTENT);
            	params2.addRule(RelativeLayout.BELOW, R.id.layout_features);   
            	params2.setMargins((int)utils.convertDpToPixel(10, this), 0, (int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this));
            	layout_category.setLayoutParams(params2); 
            	
            	list.setAdapter(adapter_category);
           	
           } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		   if (quickAction!=null) {
    			   quickAction.dismiss();
    		   }
           	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
        			,RelativeLayout.LayoutParams.WRAP_CONTENT);
           		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);   
           		params.setMargins((int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this), (int) utils.convertDpToPixel(10, this), 0);
           		layout_banner.setLayoutParams(params);
           		layout_banner.getLayoutParams().width = (int) (utils.getScreenHeight()-utils.convertDpToPixel(20, this));
           		layout_banner.getLayoutParams().height = (int) (layout_banner.getLayoutParams().width*(1.0f/2.0f));
           		  
               	RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
            			,RelativeLayout.LayoutParams.MATCH_PARENT);
            	params1.addRule(RelativeLayout.BELOW,R.id.layout_banner);
            	params1.addRule(RelativeLayout.ALIGN_LEFT,R.id.layout_banner);
            	params1.addRule(RelativeLayout.ALIGN_RIGHT,R.id.layout_banner);
            	params1.setMargins(0, (int)utils.convertDpToPixel(10, this), 0, (int)utils.convertDpToPixel(10, this));
            	layout_features.setLayoutParams(params1);
           		
            	RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
            			,RelativeLayout.LayoutParams.WRAP_CONTENT);
            	params2.addRule(RelativeLayout.RIGHT_OF, R.id.layout_banner);   
            	params2.setMargins(0, (int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this), (int)utils.convertDpToPixel(10, this));
            	layout_category.setLayoutParams(params2);      
            	
            	list.setAdapter(adapter_category_horizontal);
           }
    }
    
    public void showCategoryDetail(String cat_id,String cat_name){
    	vf.setDisplayedChild(2);
        TextView txt = (TextView)findViewById(R.id.txt_header_showcategory);
        Typeface tf = Typeface.createFromAsset(getAssets(),"DB Helvethaica X Blk Cond.ttf");
        txt.setTypeface(tf,Typeface.BOLD);
        txt.setTextSize(28); 
    	txt.setText(cat_name);
        loadCategoryDetail(cat_id, cat_name);
        loadAllCat();
        
		  aq.id(R.id.header_showcategory).clicked(new OnClickListener() {
				
			@Override
			public void onClick(View v) {
				if (quickAction!=null) {
					quickAction.show(v);
				}
			
			}
		});
    }
    
    @Override
    protected void onDestroy(){       
        super.onDestroy();  
        	if(isTaskRoot()){
                AQUtility.cleanCacheAsync(this);
        	}
}
    
    @Override
	public void onClick(View v) {
		if (v==btnHome) {
			vf.setDisplayedChild(0);
			aq.id(R.id.layout_login).visible();
			aq.id(R.id.layout_sort).invisible();
		}
		if (v==btnLibrary) {
	        showLibrary(CURRENT_SORT);
			vf.setDisplayedChild(1);
			aq.id(R.id.layout_login).invisible();
			aq.id(R.id.layout_sort).visible();
		}	
		if (v==btnFeature) {
			loadFeatures();
			aq.id(R.id.navigate_features_home).visible();
			aq.id(R.id.navigate_popular_home).invisible();
			aq.id(R.id.navigate_newarrival_home).invisible();
		}
		if (v==btnPopular) {
			loadPopular();
			aq.id(R.id.navigate_features_home).invisible();
			aq.id(R.id.navigate_popular_home).visible();
			aq.id(R.id.navigate_newarrival_home).invisible();
		}
		if (v==btnNewArrival) {
			loadNewArrival();
			aq.id(R.id.navigate_features_home).invisible();
			aq.id(R.id.navigate_popular_home).invisible();
			aq.id(R.id.navigate_newarrival_home).visible();
		}
		if(v==btnSearch){
			search(edtSearch);
		}
		if(v==btnSearch2){
			search(edtSearch2);
		}
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if ( keyCode == KeyEvent.KEYCODE_BACK ) {
    		if( vf.getDisplayedChild() == 2 || vf.getDisplayedChild() == 3 ){
				vf.setDisplayedChild(0);
				aq.id(R.id.layout_login).visible();
				aq.id(R.id.layout_sort).invisible();
				return true;
    		}
		}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	showLibrary(CURRENT_SORT);
    	if( Config.USERNAME.equals("") )
    	{
    		user.setText("");
			login.setImageResource(R.drawable.sign_in);
    	}
    	else
    	{
			user.setText(Config.NICKNAME);
			login.setImageResource(R.drawable.sign_out);
    	}
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if( requestCode == 0 && resultCode == RESULT_OK )
		{
			Config.USERNAME = data.getStringExtra("username");
			Config.NICKNAME = data.getStringExtra("nickname");
			Config.GROUP = data.getStringExtra("group");
			user.setText(Config.NICKNAME);
			login.setImageResource(R.drawable.sign_out);
		}
	}
    
    @Override
	public void finish() {
		// TODO Auto-generated method stub
    	Config.USERNAME = "";
    	Config.NICKNAME = "";
		Config.GROUP = "0";
		super.finish();
	}

	private void search(EditText edt)
    {
    	String word = edt.getText().toString();
		if( !word.isEmpty() )
		{
			if( edt == edtSearch )
			{
				vf.setDisplayedChild(3);
				edtSearch2.setText(edtSearch.getText().toString());
			}
			loadSearchDetail(word);
		}
		hideSoftKeyboard(edt);
    }
    
    private void hideSoftKeyboard(EditText edt)
    {
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
        }
    }
    
	class LoginOnClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if( user.getText().toString().equals("") ){
				Intent i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivityForResult(i, 0);
			}else{
				AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
				dialog.setTitle("Sign out");
				dialog.setMessage("คุณต้องการ Sign out ใช่ไหม?");
				dialog.setNegativeButton("Yes", new android.content.DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						user.setText("");
						login.setImageResource(R.drawable.sign_in);
						Config.USERNAME = "";
						Config.NICKNAME = "";
						Config.GROUP = "0";
						dialog.dismiss();
					}
				});
				dialog.setPositiveButton("No", new android.content.DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		}
    }
	
	class SearchOnClickListener implements OnEditorActionListener{

		public int index;
		public SearchOnClickListener(int index)
		{
			this.index = index;
		}
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// TODO Auto-generated method stub
			if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                // NOTE: In the author's example, he uses an identifier
                // called searchBar. If setting this code on your EditText
                // then use v.getWindowToken() as a reference to your 
                // EditText is passed into this callback as a TextView

                in.hideSoftInputFromWindow(v
                        .getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                
                switch (index) {
					case 1:		search(edtSearch);	break;
					default:	search(edtSearch2);	break;
				}
                
               // Must return true here to consume event
               return true;
            }
			return false;
		}
	}
	
	class TextWatcherListener implements TextWatcher{

		private ImageView delete;
		public TextWatcherListener(ImageView delete)
		{
			this.delete = delete;
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if( s.length() == 0 )	delete.setVisibility(View.INVISIBLE);
        	else					delete.setVisibility(View.VISIBLE);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}
	
	class DeleteOnClickListener implements OnClickListener{

		private EditText edt;
		public DeleteOnClickListener(EditText edt)
		{
			this.edt = edt;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			edt.setText("");
			v.setVisibility(View.INVISIBLE);
		}
	}
	
	class SortOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if( v == date )
			{
				showLibrary(DATE);
			}
			if( v == title )
			{
				showLibrary(TITLE);
			}
		}
	}
}
