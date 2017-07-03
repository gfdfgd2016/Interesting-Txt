package cn.edu.nsu.zxx.InterestingTxt;


import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class orderActivity extends Activity {

	private ListView classify;
	private ArrayList ablum,temporary;
	private List DBwords,DBpathes;
	EditText word2;
	int ImageId=R.drawable.yamisese;
	private byte[] imageByte=null;
	Bitmap bitmap ;
	SimpleAdapter adapter,adapterTemp;
	private String ImagePath,words="";
	private ImageView view1;
	private DBHelper dbHelper;
	private EditText edtSearch;
	private ImageButton btnSearch;
	SQLiteDatabase db;
	AlertDialog Addlog,updateLog,deleteLog;
	int ID,LogNum,deleteNum,ArrayNum=1;
	String tbName,itemTable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//(闪退)可能还有线程在跑，更新了mAdapter.getCount();的返回值  改变listview的内容要在UI线程
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		classify=(ListView) findViewById(R.id.listViw1);
		view1=(ImageView) findViewById(R.id.imageView1);
		edtSearch=(EditText) findViewById(R.id.edtSearch);
		btnSearch=(ImageButton) findViewById(R.id.imgBtnSearch);
		
		ablum=new ArrayList();
		temporary=new ArrayList();
		DBwords=new ArrayList();
		DBpathes=new ArrayList();
		
		
		Intent intent=getIntent();
		tbName=intent.getStringExtra("tableName");
		
		//弹窗
				ID=-1;
				LogNum=-1;
				deleteNum=-1;
			//	word=new EditText(orderActivity.this);
				word2=new EditText(orderActivity.this);
				//database
				dbHelper=new DBHelper(orderActivity.this);
				db=dbHelper.getReadableDatabase();
			
				Cursor	cursor=db.rawQuery("select * from "+tbName+"", null);
				//cursor中的数据
			//	cursor.moveToFirst();
				while(cursor.moveToNext()){
				
					DBwords.add(cursor.getString(1));
					DBpathes.add(cursor.getString(2));
					Log.i("life","   DBpathes"+cursor.getString(2)+"                 DBwords"+cursor.getString(1));
				} 
				db.close();
				cursor.close();
		
		
		adapterTemp=new mySimpleAdapter(orderActivity.this,temporary, R.layout.classify,new String[]{"intro","imageBtn"},new int[]{R.id.txtIntroduce,R.id.imageBtnList});
		adapter=new mySimpleAdapter(orderActivity.this,ablum, R.layout.classify,new String[]{"intro","imageBtn"},new int[]{R.id.txtIntroduce,R.id.imageBtnList});
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
		
		classify.setAdapter(adapter);
		initial();
		int num=allNumber();
		//查询
		btnSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				String listWord=edtSearch.getText().toString();
				temporary.clear();
				if(listWord==null)
				{
					Toast.makeText(orderActivity.this, "输入数据为空！！",Toast.LENGTH_LONG).show();
					classify.setAdapter(adapter);
				}
					
				else
				{
					String newlistWord="\""+listWord+"\"";
						ArrayNum=2;
						classify.setAdapter(adapterTemp);
						int address=position(listWord,ablum);
						if(address!=-1){
							/*HashMap map=new HashMap();
							map.put("intro", listWord);
							map.put("imageBtn",(Bitmap) ((HashMap)ablum.get(address)).get("imageBtn"));
							temporary.add(map);*/
							temporary.add(ablum.get(address));	
							Log.i("lov",""+address);
							db=dbHelper.getReadableDatabase();
							String ifexsitImg_sql="select imagePath from "+tbName+" where word="+newlistWord+"";
							Cursor cursor=db.rawQuery(ifexsitImg_sql, null);
							cursor.moveToFirst();
							if(cursor.getString(0).equals("default"))
							{	
								view1.setImageResource(R.drawable.xiamu);
							}
							else
							view1.setImageBitmap((Bitmap) ((HashMap)ablum.get(address)).get("imageBtn"));
							cursor.close();
							db.close();
							adapterTemp.notifyDataSetChanged();
						}
						
					else{
						Toast.makeText(orderActivity.this, "查无此数据！ 手动翻翻", Toast.LENGTH_LONG).show();	
						classify.setAdapter(adapter);
					}
				}
				
			}
		});
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
			new LoadImage().execute( DBpathes.get(i).toString(),DBwords.get(i).toString());
		}

	}
	
	//查询数据库中的总条数. 

	public int allNumber( ){  
	    String sql = "select count(*) from "+tbName+"";  
	    db=dbHelper.getReadableDatabase();
	    Cursor cursor = db.rawQuery(sql, null);  
	    cursor.moveToFirst();  
	    int count = cursor.getInt(0);  
	    cursor.close(); 
	    db.close();
	    return count;  
	} 
	//查询 是否存在  返回图片路径
	public String isExsit(String word){
		String ImgPath=null;
		String sql_look="select imagePath from "+tbName+" where word="+word+"";
		db=dbHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery(sql_look, null);
		//Log.i("look", " "+cursor.isNull(1));
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
	}
	
	//listviewItem 一系列点击事件
	private class mySimpleAdapter extends SimpleAdapter{

		orderActivity mcontext;
		public mySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			this.mcontext=(orderActivity) context;
			
		}
		@Override
		public View getView( int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view= super.getView(position, convertView, parent);
			final int num=position;
			Log.i("update"," position "+ position);
			if(view!=null)
			{	
				Log.i("lov", "view"+num);
				//找Id 记得加view
				ImageButton imgBtn=(ImageButton)view.findViewById(R.id.imageBtnList);
				imgBtn.getBackground().setAlpha(100);
				TextView txt=(TextView)view.findViewById(R.id.txtIntroduce);
				imgBtn.setOnClickListener(new OnClickListener(){
				String symWord=((HashMap)ablum.get(num)).get("intro").toString();
					@Override
					public void onClick(View arg0) {
						Log.i("life", " "+symWord+num);
						Intent intent=new Intent(orderActivity.this,TxtListActivity.class);
						intent.putExtra("tableName",symWord);
						//intent.putExtra("word",symWord);
						startActivity(intent);
					}
					});
				//删除事件
				view.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View arg0) {
						
						deleteLog=new AlertDialog.Builder(mcontext)
						.setTitle("删除")
						.setMessage("确认删除？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								String oldListword=((HashMap)ablum.get(num)).get("intro").toString();
								String Listword="\""+oldListword+"\"";
								Log.i("update", num+"");
								
								if(ArrayNum==2)
								{
									String tempWord=((HashMap)temporary.get(0)).get("intro").toString();
									int address=position(tempWord, ablum);
									if(address!=-1){
										ablum.remove(address);
									}
									temporary.remove(num);
									
									adapterTemp.notifyDataSetChanged();
									
									ArrayNum=1;
								}
								else{
									ablum.remove(num);
								}	
								adapter.notifyDataSetChanged();
							    db=dbHelper.getReadableDatabase();
								String sql_delete="delete from "+tbName+" where word="+Listword+"";
								String sql_perDelete="delete from person where fakeTable="+Listword+"";
					
								 db.execSQL( sql_perDelete);
							
								db.execSQL(sql_delete);
								Toast.makeText(orderActivity.this, "已删除"+oldListword+"  "+Listword, Toast.LENGTH_LONG).show();
								db.close();
							}
						})
						.setNegativeButton("取消", null).show();
					
						return false;
					}});
			
				txt.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						
						//修改 弹窗
						final EditText word=new EditText(orderActivity.this);
						updateLog=new  AlertDialog.Builder(orderActivity.this)
						.setTitle("修改")
						.setView(word)
						.setNegativeButton("取消", null)
						.setPositiveButton("确定", new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
							db=dbHelper.getReadableDatabase();
							runOnUiThread(new Runnable(){
								public void run(){
									String oldWord;
									String newWord=word.getText().toString();
									int address=position(newWord,ablum);
								
									if(address==-1)
									{	
										if(ArrayNum==2)
										{
											oldWord=(((HashMap)temporary.get(0)).get("intro")).toString();
											address=position(oldWord,ablum);
										}
										else
										{
											address=num;
										}
									}
									else
									{
										Toast.makeText(mcontext, "已存在", Toast.LENGTH_LONG).show();
									}
									
									oldWord="\""+(((HashMap)ablum.get(address)).get("intro")).toString()+"\"";
									if(address!=-1){
										Log.i("lov", oldWord);
										
										newWord="\""+word.getText().toString()+"\"";
										String update_person=" update person set fakeTable="+newWord+" where fakeTable="+oldWord +"";
										String update_sql="update "+tbName+" set word="+newWord+" where word="+oldWord+"";
										db.execSQL(update_sql);
										db.execSQL(update_person);
									 	db.close();
									 	if(ArrayNum==2){
									 		ArrayNum=1;
											adapterTemp.notifyDataSetChanged();
									 	}
									 	newWord=word.getText().toString();
									 	Map<String, Object>map=(Map<String, Object>) ablum.get(address);
										map.put("intro", newWord);//将新的文件名添加到Map以替换旧文件名
									 	//替换arrayList某一元素的值
									 	ablum.set(address, map);//替换listItems中原来的map
										adapter.notifyDataSetChanged();//通知SimpleAdapter数据改变	
										word.setText("");
									}		
								}
							});
						
							}
						}).show();
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
				Addlog=new  AlertDialog.Builder(orderActivity.this)
				.setTitle("新增")
				.setView(word2)
				.setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						
						words= word2.getText().toString();
						if(words==null)
						{
							Toast.makeText(orderActivity.this, "it can not be null!", Toast.LENGTH_LONG).show();
						}
						else
						{
							int address=position(words,ablum);
							if(address!=-1)
							{
								Toast.makeText(orderActivity.this, "已经存在！请重新输入",Toast.LENGTH_LONG).show();
							}
							else
							{
								word2.setText("");
								findImage();
							}
						
						}
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
	    	Toast.makeText(orderActivity.this, "so sad fail", Toast.LENGTH_LONG).show();
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
		}
		db=dbHelper.getReadableDatabase();
		String insert_sql="insert into "+tbName+"(word,imagePath) values(?,?)";
		db.execSQL(insert_sql, new String[]{words,ImagePath});
		db.close();
		new LoadImage().execute(ImagePath,words);
	}
	
	class LoadImage extends AsyncTask<String,Void,Bitmap >{
		@Override
		protected Bitmap doInBackground( final String... arg0) {
			try {
				if(!arg0[0].equals("default"))
				{
					FileInputStream fosfrom = new FileInputStream( arg0[0]);
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
			}
			else
			{
				map.put("intro", arg0[1]);
				map.put("imageBtn",R.drawable.xiamu);
			}
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					ablum.add(map);
					if(!arg0[0].equals("default")){
						int num=allNumber();
						if(ablum.size()==num)
						view1.setImageBitmap(bitmap);
					}
				}});
			
			
			return bitmap;
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			
	//	super.onPostExecute(result);
		
			if(imageByte!=null)
			{	
				adapter.notifyDataSetChanged();
			
			}
		}
	}
	
	
	//通过URI得到文件路径
	public Uri getRealPathFromURI(Uri contentUri) {
	    String res = null;
	    String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
	    if(cursor.moveToFirst()){
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

}
