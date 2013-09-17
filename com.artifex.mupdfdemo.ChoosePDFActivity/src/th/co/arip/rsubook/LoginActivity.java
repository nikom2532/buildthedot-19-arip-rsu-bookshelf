package th.co.arip.rsubook;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import th.co.arip.rsubook.utils.Config;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{

	private EditText username;
	private EditText password;
	private ImageView signin;
	
	private AQuery aq;
	private String user = "";
	private String nickname = "";
	private String group = "0";
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        aq = new AQuery(this);
        username = (EditText)findViewById(R.id.edt_username);
        password = (EditText)findViewById(R.id.edt_password);
        signin = (ImageView)findViewById(R.id.img_signin);
        
        signin.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Toast.makeText(aq.getContext(), "Please wait", Toast.LENGTH_SHORT).show();
		Login();
	}
	
	private void hideSoftKeyboard(EditText edt)
    {
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
        }
    }
	
	private void Login( )
	{
		AjaxCallback<JSONObject> cb = new AjaxCallback<JSONObject>(){
			
   		 	@Override
            public void callback(String url, JSONObject json, AjaxStatus status) {
   		 		//ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String,String>>();   
            	//pb_category.setVisibility(View.GONE);
                if(json != null){
                	try {
                		if (json.getString("status").equals("1")) 
                		{
                			user = json.getString("username");
                			nickname = json.getString("nickname");
                			group = json.getString("group");
                		}
	           		} catch (JSONException e) { 
	           			Log.e("Login", e.getMessage());
	           		}
                }
                else
                {            
                	Toast.makeText(aq.getContext(), "Please check  your internet connection", Toast.LENGTH_LONG).show();
	            }
                Intent data = new Intent();
                data.putExtra("username", user);
        		data.putExtra("nickname", nickname);
        		data.putExtra("group", group);
        		if( !user.equals("") )
        		{
        			setResult(RESULT_OK, data);
        			Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_LONG).show();
        			hideSoftKeyboard(username);
        			finish();
        		}
        		else
        		{
        			setResult(RESULT_CANCELED, data);
        			Toast.makeText(getApplicationContext(), "Login fail, sometime username or password is wrong", Toast.LENGTH_LONG).show();
        		}
            }
		};
		cb.header("User-Agent", "android");
   		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
   		pairs.add(new BasicNameValuePair("t", "3"));
   		pairs.add(new BasicNameValuePair("library", Config.LIBRARY_CODE));
   		pairs.add(new BasicNameValuePair("login", username.getText().toString()));
   		pairs.add(new BasicNameValuePair("password", password.getText().toString()));
 
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
}
