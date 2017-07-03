package cn.edu.nsu.zxx.InterestingTxt;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import cn.edu.nsu.zxx.util.DBHelper;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

public class TxtListActivity extends Activity {

	private ListView darilyList;
	private ArrayList ablum,temporary;
	private List DBwords,DBpathes,DBtime;
	EditText word,word2;
	int ImageId=R.drawable.yamisese;
	private byte[] imageByte=null;
	Bitmap bitmap ;
	SimpleAdapter adapter,adapterTemp;
	private String ImagePath,words="";
	private DBHelper dbHelper;

	SQLiteDatabase db;
	AlertDialog Addlog,updateLog,deleteLog;
	int ID,LogNum,deleteNum,ArrayNum=1;
	String tbName, choose;
	SimpleDateFormat sDateFormat;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_txt_list);
		darilyList=(ListView) findViewById(R.id.listDarilyView);
		
		ablum=new ArrayList();
		temporary=new ArrayList();
		DBwords=new ArrayList();
		DBpathes=new ArrayList();
		DBtime=new ArrayList();
		
		//时间
		sDateFormat=new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");       
		
		
		//
		Intent intent=getIntent();
		tbName=intent.getStringExtra("tableName");
		
		//弹窗
				ID=-1;
				LogNum=-1;
				deleteNum=-1;
				word=new EditText(TxtListActivity.this);
				word2=new EditText(TxtListActivity.this);
				
				
				//database
				dbHelper=new DBHelper(TxtListActivity.this);
				db=dbHelper.getReadableDatabase();
				choose="\""+tbName+"\"";
				Cursor	cursor=db.rawQuery("select * from person where fakeTable="+choose+"", null);
				//cursor中的数据
			//	cursor.moveToFirst();
				while(cursor.moveToNext()){
				
					DBwords.add(cursor.getString(1));
					DBpathes.add(cursor.getString(2));
					DBtime.add(cursor.getString(3));
					Log.i("life","   DBpathes"+cursor.getString(2)+"                 DBwords"+cursor.getString(1));
				} 
				db.close();
				cursor.close();
		
		
		adapterTemp=new mySimpleAdapter(TxtListActivity.this,temporary, R.layout.darily,new String[]{"intro","imageBtn","time"},new int[]{R.id.dairlyTxt,R.id.imgBtnDa,R.id.textTime});
		adapter=new mySimpleAdapter(TxtListActivity.this,ablum, R.layout.darily,new String[]{"intro","imageBtn","time"},new int[]{R.id.dairlyTxt,R.id.imgBtnDa,R.id.textTime});
		//不支持Bitmap 所以重写方法
		adapter.setViewBinder(new ViewBinder(){

			@Override
			public boolean setViewValue(View arg0, Object arg1, String arg2) {
				  if(arg0 instanceof ImageView && arg1 instanceof Bitmap){  
                      ImageView iv=(ImageView)arg0;  
                      iv.setImageBitmap((Bitmap) arg1);  
                      return true;  
              }else{  
                      return false;  
              }     
			}});
		
		darilyList.setAdapter(adapter);
		initial();
		
	}
	
	//ablum temporary判等
	public int position(String word,ArrayList list){
		int address=-1;
		for(int i=0;i<list.size();i++){
			if(word.equals((((HashMap)list.get(i)).get("intro")).toString())){
				address=i;
				break;
			}
		}
		return address;		
	}
	
	//初始化列表
	public void initial(){
	
		int num=allNumber();
		for(int i=0;i<num;i++){
			new LoadImage().execute( DBpathes.get(i).toString(),DBwords.get(i).toString(),DBtime.get(i).toString());
		}
		
			
	}
	
	//查询数据库中的总条数. 

	public int allNumber( ){  
	    String sql = "select count(*) from person where fakeTable="+choose+"";  
	    db=dbHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery(sql, null);  
	    cursor.moveToFirst();  
	    int count = cursor.getInt(0);  
	    cursor.close(); 
	    db.close();
	    return count;  
	} 
	//查询 是否存在  返回图片路径
	/*public String isExsit(String word){
		String ImgPath=null;
		String sql_look="select imagePath from "+tbName+" where word="+word+"";
		db=dbHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery(sql_look, null);
		if( cursor.getCount()>0)
		{
			cursor.moveToFirst();
			if(!cursor.getString(0).isEmpty())
			ImgPath=cursor.getString(0);
		Log.i("look", " "+cursor.getString(0).isEmpty()+"   "+ImgPath);
		}
		cursor.close();
		db.close();
		return ImgPath;
	}*/
	
	//listviewItem 一系列点击事件
	private class mySimpleAdapter extends SimpleAdapter{

		TxtListActivity mcontext;
		public mySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.mcontext=(TxtListActivity) context;
			
		}
		@Override
		public View getView( final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view= super.getView(position, convertView, parent);
			if(view!=null)
			{
				//找Id 记得加view
				ImageButton imgBtn=(ImageButton)view.findViewById(R.id.imgBtnDa);
				imgBtn.getBackground().setAlpha(100);
				TextView txt=(TextView)view.findViewById(R.id.dairlyTxt);
				imgBtn.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						String word=((HashMap)ablum.get(position)).get("intro").toString();
						Intent intent=new Intent(TxtListActivity.this,MainActivity.class);
						intent.putExtra("tbName", tbName);
						intent.putExtra("word",word);
						startActivity(intent);
						
					}
					});
				//删除事件
				view.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View arg0) {
						if(deleteNum==-1)
						{
							deleteNum=1;
						deleteLog=new AlertDialog.Builder(mcontext)
						.setTitle("删除")
						.setMessage("确认删除？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String Listword=((HashMap)ablum.get(position)).get("intro").toString();
								Listword="\""+Listword+"\"";
								Log.i("update", position+"");
								
								if(ArrayNum==2)
								{
									String tempWord=((HashMap)temporary.get(0)).get("intro").toString();
									int address=position(tempWord, ablum);
									if(address!=-1){
										ablum.remove(address);
									}
									temporary.remove(position);
									runOnUiThread(new Runnable(){

										@Override
										public void run() {
											// TODO Auto-generated method stub
											adapterTemp.notifyDataSetChanged();
										}});
									
									ArrayNum=1;
								}
								else{
									ablum.remove(position);
								
									
								}	
								runOnUiThread(new Runnable(){

										@Override
										public void run() {
											adapter.notifyDataSetChanged();
									}});
							    db=dbHelper.getReadableDatabase();
								String sql_delete="delete from person where word="+Listword+"";
								db.execSQL(sql_delete);
								Toast.makeText(TxtListActivity.this, "已删除", Toast.LENGTH_LONG).show();
								db.close();
							}
						})
						.setNegativeButton("取消", null).create();
						}
						deleteLog.show();
						
						
						return false;
					}});
				
						
					
				
				txt.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						//修改 弹窗
						if(ID==-1)
						{
						ID=position+1;
						updateLog=new  AlertDialog.Builder(TxtListActivity.this)
						.setTitle("修改")
						.setView(word)
						.setNegativeButton("取消", null)
						.setPositiveButton("确定", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							
							db=dbHelper.getReadableDatabase();
							
							runOnUiThread(new Runnable(){
								public void run(){
									String newWord=word.getText().toString();
									ContentValues values=new ContentValues();
									values.put("word", newWord);
									String dbWord,oldWord;
									if(ArrayNum==2)
									{
										oldWord=(((HashMap)temporary.get(0)).get("intro")).toString();
									    dbWord="\""+oldWord+"\"";
									    db.update("person", values, "word="+dbWord+"", null);
									  //替换arrayList某一元素的值
										Map<String, Object>map=(Map<String, Object>) ablum.get(position);
										map.put("intro", newWord);//将新的文件名添加到Map以替换旧文件名
										temporary.set(position, map);//替换listItems中原来的map
										adapterTemp.notifyDataSetChanged();
										ArrayNum=1;
										int address=position(oldWord,ablum);
										if(address!=-1){
											ablum.set(address,map);
									    adapter.notifyDataSetChanged();
										}
										
									}
									else
									{
										oldWord=(((HashMap)ablum.get(position)).get("intro")).toString();
										dbWord="\""+oldWord+"\"";
										db.update("person", values, "word="+dbWord+"", null);
										//替换arrayList某一元素的值
										Map<String, Object>map=(Map<String, Object>) ablum.get(position);
										map.put("intro", newWord);//将新的文件名添加到Map以替换旧文件名
										ablum.set(position, map);//替换listItems中原来的map
									    adapter.notifyDataSetChanged();//通知SimpleAdapter数据改变
									}
									Log.i("update", ""+oldWord +"  "+dbWord );
							     	db.close();
										
								}
							});
							word.setText("");
							
							
							}
							
						}).create();
						}
						updateLog.show();
					}});
		}
			return view;
		}
		
		
	}
	

	//新增
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.itemAdd:
	
			//新增弹窗和修改弹窗不兼容原因是"一女不可二嫁"即setView(word)用的同一个
			
			if(LogNum==-1)
			{
				LogNum=1;
				Addlog=new  AlertDialog.Builder(TxtListActivity.this)
				.setTitle("新增")
				.setView(word2)
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
						words= word2.getText().toString();
						int address=position(words,ablum);
						if(address!=-1)
						{
							Toast.makeText(TxtListActivity.this, "已经存在！请重新输入",Toast.LENGTH_LONG).show();
						}
						else
						findImage();	
						word2.setText("");
	
					}
				
			})
			.create();
			}
		
			Addlog.show();
			
		}
		return super.onOptionsItemSelected(item);
	}
	private void findImage(){
		Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		try{
			startActivityForResult(Intent.createChooser(intent, "选择图片"), 0x102);
		}
	    catch(android.content.ActivityNotFoundException ex){
	    	Toast.makeText(TxtListActivity.this, "so sad fail", Toast.LENGTH_LONG).show();
	    };
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	  
		
        //这是获取的图片保存在sdcard中的位置  
		
		if(resultCode==Activity.RESULT_OK){
			if(requestCode==0x102)
			{	
				Uri uri = data.getData();
        		ImagePath=getRealPathFromURI(uri).getPath();
			}
		}
		else
		{
				ImagePath="default";
			
			   Log.i("life", "onActivityResult() error, resultCode: " + resultCode);
		}
		db=dbHelper.getReadableDatabase();
		String date=sDateFormat.format(new    java.util.Date());  
		String insert_sql="insert into person(word,imagePath,time,fakeTable) values(?,?,?,?)";	
		db.execSQL(insert_sql, new String[]{words,ImagePath,date,""+tbName+""});
		db.close();
		new LoadImage().execute(ImagePath,words,date);
		//super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	class LoadImage extends AsyncTask<String,Void,Bitmap >{

		@Override
		protected Bitmap doInBackground( String... arg0) {
			try {
				Log.i("life", "288 "+arg0[0]);
		
				if(!arg0[0].equals("default"))
				{
					FileInputStream  fosfrom = new FileInputStream( arg0[0]);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] buffer=new byte[1024];
					int len=-1;
					while((len=fosfrom.read(buffer))>0){
							bos.write(buffer, 0, len);
					}
				
					fosfrom.close();
					imageByte=bos.toByteArray();
					bos.close();
				}
				
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final HashMap map=new HashMap();
			if(!arg0[0].equals("default"))
			{
			
					bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
					
						map.put("intro", arg0[1]);
						map.put("imageBtn",bitmap);
						map.put("time",arg0[2]);
			}                      
		   else if(arg0[0].equals("default"))
			{
				map.put("intro", arg0[1]);
				map.put("imageBtn",R.drawable.yamisese);
				map.put("time",arg0[2]);
			}
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					ablum.add(0, map);
					
				}});
			return bitmap;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			
				adapter.notifyDataSetChanged();
			
			
		
		}
	}
	
	//通过URI得到文件路径
	public Uri getRealPathFromURI(Uri contentUri) {
	    String res = null;
	    String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
	    if(cursor.moveToFirst()){;
	       int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	       res = cursor.getString(column_index);
	    }
	    cursor.close();
	    File file = new File(res);
	    Uri fileUri = Uri.fromFile(file);
	    return fileUri;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.txt_list, menu);
		return true;
	}

}
