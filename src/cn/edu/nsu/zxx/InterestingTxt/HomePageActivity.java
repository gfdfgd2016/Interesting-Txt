package cn.edu.nsu.zxx.InterestingTxt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class HomePageActivity extends Activity {

	private ImageButton btnLife,btnFriends,btnLov;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_page);
		btnLife=(ImageButton) findViewById(R.id.imgBtnLife);
		btnFriends=(ImageButton) findViewById(R.id.imgBtnFriends);
		btnLov=(ImageButton) findViewById(R.id.imgBtnLov);	
		btnLife=(ImageButton) findViewById(R.id.imgBtnLife);
		BtnClickListener btl=new BtnClickListener();
		btnFriends.setOnClickListener(btl);
		btnLov.setOnClickListener(btl);
		btnLife.setOnClickListener(btl);
		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.Intro:
			Intent intentTro=new Intent(HomePageActivity.this,IntroductionActivity.class);
			startActivity(intentTro);
		}
		return super.onOptionsItemSelected(item);
	}

	class BtnClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			switch(arg0.getId()){
			case R.id.imgBtnFriends:
				Intent intent=new Intent(HomePageActivity.this,orderActivity.class);
				intent.putExtra("tableName", "friend");
				startActivity(intent);
				break;
			case R.id.imgBtnLov:
				Intent intent2=new Intent(HomePageActivity.this,orderActivity.class);
				intent2.putExtra("tableName", "lover");
				startActivity(intent2);
				break;
			case R.id.imgBtnLife:
				Intent intent3=new Intent(HomePageActivity.this,TxtListActivity.class);
				intent3.putExtra("tableName", "person");
				startActivity(intent3);
				break;
			}
			
		}
		
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_page, menu);
		return true;
	}

}
