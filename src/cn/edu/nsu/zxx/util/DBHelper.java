package cn.edu.nsu.zxx.util;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private String Tab_friend="create table friend(_id integer primary key autoincrement,word varchar,imagePath varchar)";
	private String Tab_lover="create table lover(_id integer primary key autoincrement,word varchar,imagePath varchar)";
	private String Tab_person="create table person(_id integer primary key autoincrement,word varchar,imagePath varchar,time varchar,content varchar,mood varchar,fakeTable varchar)";

	public DBHelper(Context context)
	{
		super(context, "mydata.db",null, 2);
	}
	public DBHelper(Context context, String name, CursorFactory factory,
			int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(Tab_friend);
		arg0.execSQL(Tab_lover);
		arg0.execSQL(Tab_person);

		//arg0.execSQL(Tab_information);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		/*db.execSQL("drop table if exists friend");
		db.execSQL("drop table if exists lover");
		db.execSQL("drop table if exists person");*/
		onCreate(db);

	}


}
