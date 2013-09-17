package th.co.arip.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHandler extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "book.db";
	private static final int DATABASE_VERSION = 3;
	
	  public static final String TABLE_BOOK = "book";
	  public static final String COLUMN_PID = "_id";
	  public static final String COLUMN_ID = "_bid";
	  public static final String COLUMN_TITLE = "_title";
	  public static final String COLUMN_DETAIL = "_detail";
	  public static final String COLUMN_DATE = "_date";
	  public static final String COLUMN_CATEGORY = "_category";
	  public static final String COLUMN_IMAGE = "_image";
	  public static final String COLUMN_REVISION = "_revision";
	  
	  private static final String DATABASE_CREATE = "create table "
		      + TABLE_BOOK + "(" + COLUMN_PID
		      + " integer primary key autoincrement, " + COLUMN_ID
		      + " text not null," + COLUMN_TITLE + " text not null," 
		      + COLUMN_DETAIL + " text not null," + COLUMN_DATE 
		      + " text not null," + COLUMN_CATEGORY + " text not null," 
		      + COLUMN_IMAGE + " text not null," + COLUMN_REVISION + " text not null" + " );";
	
	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK);
	    onCreate(database);
		
	}

}
