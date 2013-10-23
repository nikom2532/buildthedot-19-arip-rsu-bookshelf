package th.co.arip.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

public class BookHandler {
	private Context mContext;
	private AQuery aq;
	private SQLiteDatabase database;
	private SQLiteHandler sqliteHandler;
	private String[] allColumns = { 
			  SQLiteHandler.COLUMN_ID,
		      SQLiteHandler.COLUMN_TITLE ,
		      SQLiteHandler.COLUMN_DETAIL ,
		      SQLiteHandler.COLUMN_DATE ,
		      SQLiteHandler.COLUMN_CATEGORY ,
		      SQLiteHandler.COLUMN_IMAGE ,
		      SQLiteHandler.COLUMN_REVISION};

	
	public BookHandler(Context context){
		sqliteHandler = new SQLiteHandler(context);
		aq = new AQuery(context);
		mContext = context;
	}
	
	public void open(){
		database = sqliteHandler.getWritableDatabase();
	}
	
	public void close() {
		sqliteHandler.close();
	}
	
	public List<Book> getAllFavorite() {
	    List<Book> favorites = new ArrayList<Book>();

	    Cursor cursor = database.query(SQLiteHandler.TABLE_BOOK,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	Book favorite = cursorToFavorite(cursor);
	    	favorites.add(favorite);
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    close();
	    return favorites;
	  }
	
	public void updateBook(String id,String revision){
		ContentValues args = new ContentValues();
		args.put(SQLiteHandler.COLUMN_REVISION, revision);
		database.update(SQLiteHandler.TABLE_BOOK, args, SQLiteHandler.COLUMN_ID + "=" + id, null);
	}
	
	public List<Book> sort(int type)
	{
		List<Book> favorites = new ArrayList<Book>();
		Cursor cursor = null;
		switch (type) {
			case 1:	cursor = database.query(SQLiteHandler.TABLE_BOOK, allColumns, null, null, null, null, SQLiteHandler.COLUMN_TITLE);
				break;
	
			default: cursor = database.query(SQLiteHandler.TABLE_BOOK, allColumns, null, null, null, null, null);
				break;
		}
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
	    	Book favorite = cursorToFavorite(cursor);
	    	favorites.add(favorite);
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    close();
	    return favorites;
	}
	
	public Boolean check(String id){
	    List<Book> favorites = new ArrayList<Book>();
		String[] column = {SQLiteHandler.COLUMN_ID};
	    Cursor cursor = database.query(SQLiteHandler.TABLE_BOOK,
	    		column, null, null, null, null, null);
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	Book favorite = cursorToFavoriteCheck(cursor);
	    	favorites.add(favorite);
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    
	    for (int i = 0; i < favorites.size(); i++) {
			if (favorites.get(i).getId().equals(id)) {
				return false;
			}
		}
	    return true;
	}
	
	public String getPDF(String id){
	    List<Book> favorites = new ArrayList<Book>();
	    Cursor cursor = database.query(SQLiteHandler.TABLE_BOOK,
	    		allColumns, null, null, null, null, null);
	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	Book favorite = cursorToFavorite(cursor);
	    	favorites.add(favorite);
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    
	    for (int i = 0; i < favorites.size(); i++) {
			if (favorites.get(i).getId().equals(id)) {
				return favorites.get(i).getCategory();			
			}
		}
		return null;
	}
	
	public void addFavorite(final String id,String title, String detail, String date, String category, String image, String revision){
		final ContentValues values = new ContentValues();
		values.put(SQLiteHandler.COLUMN_ID, id);
		values.put(SQLiteHandler.COLUMN_TITLE, title);
		values.put(SQLiteHandler.COLUMN_DETAIL, detail);
		values.put(SQLiteHandler.COLUMN_DATE, date);
		values.put(SQLiteHandler.COLUMN_CATEGORY, category);
		values.put(SQLiteHandler.COLUMN_REVISION, revision);
		
		final ProgressDialog pd = ProgressDialog.show(mContext, null, "Saving");
		File ext = Environment.getExternalStorageDirectory();
		File target = new File(ext, "Android/data/"+mContext.getPackageName()+"/cache/"+String.valueOf(image.hashCode()));              

		aq.download(image, target, new AjaxCallback<File>(){
		        
		        @Override
				public void callback(String url, File file, AjaxStatus status) {
		                pd.dismiss();
		                if(file != null){
		            			values.put(SQLiteHandler.COLUMN_IMAGE, file.getAbsolutePath());
		            				database.insert(SQLiteHandler.TABLE_BOOK, null, values);
			                        Toast.makeText(mContext, "Saved", Toast.LENGTH_LONG).show();         			
		                }
		        }
		        
		});
	}
	
	public void deleteComment(String id) {
	    //System.out.println("Comment deleted with id: " + id);
	    database.delete(SQLiteHandler.TABLE_BOOK, SQLiteHandler.COLUMN_ID
	        + " = " + id, null);
	  }
	
	private Book cursorToFavoriteCheck(Cursor cursor){
	    Book favorite = new Book();
	    favorite.setId(cursor.getString(0));
	    return favorite;
	}
	
	
	 private Book cursorToFavorite(Cursor cursor) {
		    Book favorite = new Book();
		    favorite.setId(cursor.getString(0));
		    favorite.setTitle(cursor.getString(1));
		    favorite.setDetail(cursor.getString(2));
		    favorite.setDate(cursor.getString(3));
		    favorite.setCategory(cursor.getString(4));
		    favorite.setImage(cursor.getString(5));
		    favorite.setRevision(cursor.getString(6));
		    return favorite;
		  }
}
