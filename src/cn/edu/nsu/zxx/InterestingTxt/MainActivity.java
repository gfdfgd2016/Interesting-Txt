package cn.edu.nsu.zxx.InterestingTxt;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.nsu.zxx.util.DBHelper;

import com.emokit.sdk.InitListener;
import com.emokit.sdk.basicinfo.AdvancedInformation;
import com.emokit.sdk.heartrate.EmoRateListener;
import com.emokit.sdk.heartrate.RateDetect;

import com.emokit.sdk.record.EmotionDetect;
import com.emokit.sdk.record.EmotionVoiceListener;
import com.emokit.sdk.record.SpeechEmotionDetect;
import com.emokit.sdk.record.SpeechEmotionListener;
import com.emokit.sdk.senseface.ExpressionDetect;
import com.emokit.sdk.senseface.ExpressionListener;
import com.emokit.sdk.util.JsonParser;
import com.emokit.sdk.util.SDKAppInit;
import com.emokit.sdk.util.SDKConstant;

import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ImageButton btnVoice, btnCamera, btnVoicePlay;
	private List<String> mIra;
	private List<String> mEmo;
	private EditText edtVoice, edtEmo;
	private Context mcontext;
	// 语音识别
	private SpeechEmotionDetect speechEmotionDetect;
	EmotionDetect mEmotionDetect;
	RateDetect rt;// 心率
	ExpressionDetect expressdetect;// 表情
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private String tbName, word;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		super.onCreate(savedInstanceState);
		// 初始化 SDK
		SDKAppInit.createInstance(this);
		// 开启 Debug 模式
		SDKAppInit.setDebugMode(true);
		btnCamera = (ImageButton) findViewById(R.id.imgBtnCamera);
		btnVoice = (ImageButton) findViewById(R.id.imgBtnVoice);
		edtVoice = (EditText) findViewById(R.id.editTxtContent);
		edtEmo = (EditText) findViewById(R.id.edtEmo);

		// database
		dbHelper = new DBHelper(MainActivity.this);
		Intent intent = getIntent();
		tbName = intent.getStringExtra("tbName");
		word = intent.getStringExtra("word");

		// btnStop=(Button) findViewById(R.id.button2);
		mcontext = this;
		// 启动语音分析
		// mEmotionDetect = EmotionDetect.createRecognizer(MainActivity.this,
		// mInitListener);

		// 验证
		speechEmotionDetect = SpeechEmotionDetect.createRecognizer(this,
				mInitListener);
		rt = RateDetect.createRecognizer(this, mInitListener);
		expressdetect = ExpressionDetect.createRecognizer(this, mInitListener);

		btnVoice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mIra = new ArrayList<String>();
				mEmo = new ArrayList<String>();
			//	edtVoice.setText("");
				edtEmo.setText("");
				// mEmotionDetect.startListening(mRecognizerListener);
				// 停止监听 

				// mEmotionDetect.reAnalysisVoice("reason"); //返回情绪结果同上

				speechEmotionDetect.startListening(mSpeechEmotionListener,
						SDKConstant.RC_TYPE_5, false, "52c8cef6");
				// speechEmotionDetect.startListening(mSpeechEmotionListener,
				// SDKConstant.RC_TYPE_5, false, "52c8cef6");

			}
		});

		/*
		 * btnCamera.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * 
		 * rt.setParameter(SDKConstant.FACING, SDKConstant.CAMERA_FACING_FRONT);
		 * rt.startRateListening(recoginze_listener, SDKConstant.RC_TYPE_5); }
		 * });
		 */
		btnCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				expressdetect.setParameter(SDKConstant.FACING,
						SDKConstant.CAMERA_FACING_FRONT);
				expressdetect.startRateListening(expresslisten);
			}
		});
		initial();

	}

	private void initial() {
		db = dbHelper.getReadableDatabase();
		String symWord = "\"" + word + "\"";
		Cursor cursor = db.rawQuery("select count(content),content,mood from person where word="+ symWord + "", null);
		cursor.moveToFirst();
		if (cursor.getInt(0)>0) {
				
			edtEmo.setText(cursor.getString(2));
			edtVoice.setText(cursor.getString(1));
			
		}
        db.close();
		cursor.close();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.itemSave:

			String content ="\'"+ edtVoice.getText().toString()+"\'";
			String mood = "\'"+edtEmo.getText().toString()+"\'";
			/*ContentValues values = new ContentValues();
			values.put("content", content);
			values.put("mood", mood);*/
			String symWord = "\"" + word + "\"";
			db = dbHelper.getReadableDatabase();
			String update_sql=" update person set content="+content+", mood="+mood+" where word="+symWord+"";
			db.execSQL(update_sql);
			db.close();
			Toast.makeText(MainActivity.this, "success！"+mood, Toast.LENGTH_LONG).show();
			break;
		case R.id.ic_menu_share:
			break;
		}

		return super.onOptionsItemSelected(item);

	}

	// 人脸
	private ExpressionListener expresslisten = new ExpressionListener() {

		@Override
		public void endDetect(String result, String picFile) {

			printEmotionResult2(result);

			Log.e("picFile", picFile);
		}

		@Override
		public void beginDetect() {

		}
	};
	// 人脸 取眼睛部分
	EmoRateListener recoginze_listener = new EmoRateListener() {

		@Override
		public void beginDetect() {

		}

		@Override
		public void endDetect(String result) {

			printEmotionResult2(result);
		}

		@Override
		public void monitor(double rgb) {

			Log.i("recognizetag", "" + rgb);

		}

	};
	// 语音识别情绪
	private EmotionVoiceListener mRecognizerListener = new EmotionVoiceListener() {
		@Override
		public void onVolumeChanged(int volume) {
			// 实时分贝
			// Log.i("life", volume+"");
		}

		@Override
		public void onBeginOfSpeech() {
			// 开始说话
			Log.i("life", "start");
		}

		@Override
		public void onEndOfSpeech() {
			// 结束说话
			// edtVoice.setText("first step");
		}

		@Override
		public void onVoiceResult(String result) {
			// 返回情绪结果

		}
	};
	/*
	 * //启动监听 mEmotionDetect.startListening(mRecognizerListener); // 停止监听 
	 * mEmotionDetect.stopListening();
	 */

	// 初始化用户信息 
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			// 注册用户信息: platflag 应用名; userName 用户名或设备 ID;
			// password 用户登录密码(可为空)
			AdvancedInformation pp = AdvancedInformation.getSingleton(mcontext);
			SDKAppInit.registerforuid("Please", "jicheng", "924122771a");
		}
	};

	private SpeechEmotionListener mSpeechEmotionListener = new SpeechEmotionListener() {
		@Override
		public void onVolumeChanged(int volume) {
			Log.i("life", volume + "");
		}

		@Override
		public void onEndOfSpeech() {
			// Log.e("life", "end");
			// Toast.makeText(MainActivity.this, "ending",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onBeginOfSpeech() {
			// Log.e("life", "prepare Start");
			// Toast.makeText(MainActivity.this, "prepare Start",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onEmotionResult(String result) {
			printEmotionResult(result);
		}
		@Override
		public void onSpeechResult(String result) {
			printSpeechResult(result);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void printEmotionResult2(String results) {

		if (results == null)
			return;

		Log.e("printResult", results);

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(results);

			int resultcode = jsonObject.getInt("resultcode");
			if (resultcode == 200) {

				String emoCode = jsonObject.getString("rc_main");
				if (emoCode.equals("K")) {
					emoCode = "骚年，你很开心哟";
				} else if (emoCode.equals("C")) {
					emoCode = "少年不识愁滋味，欲上高楼强说愁";
				} else if (emoCode.equals("Y")) {
					emoCode = "答滴答滴答答";
				} else if (emoCode.equals("M")) {
					emoCode = "答滴答滴答答";
				} else if (emoCode.equals("W")) {
					emoCode = "答滴答滴答答";
				}

				String servertime = jsonObject.getString("servertime");

				mainhandler.sendMessage(mainhandler.obtainMessage(2, "你开心就好("
						+ servertime + "):" + emoCode));
			} else {
				String servertime = jsonObject.getString("servertime");
				mainhandler.sendMessage(mainhandler.obtainMessage(2, "你开心就好("
						+ servertime + ")"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	protected void printSpeechResult(String results) {
		// TODO Auto-generated method stub
		String text = JsonParser.parseIatResult(results);

		String sn = null;
	
		try {
			JSONObject resultJson = new JSONObject(results);
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIra.add(text);

		StringBuffer resultBuffer = new StringBuffer();

		for (String iterable_element : mIra) {
			resultBuffer.append(iterable_element);
		}

		Message msg = new Message();
		msg.what = 1;
		msg.obj = resultBuffer.toString();
		mainhandler.sendMessage(msg);
	}

	protected void printEmotionResult(String results) {
		// TODO Auto-generated method stub
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(results);

			int resultcode = jsonObject.getInt("resultcode");
			if (resultcode == 200) {

				String emoCode = jsonObject.getString("rc_main");
				if (emoCode.equals("K")) {
					emoCode = "happy";
				} else if (emoCode.equals("C")) {
					emoCode = "cool";
				} else if (emoCode.equals("Y")) {
					emoCode = "worry";
				} else if (emoCode.equals("M")) {
					emoCode = "i don't konw";
				} else if (emoCode.equals("W")) {
					emoCode = "sad";
				}

				mEmo.add(emoCode);

				StringBuffer resultBuffer = new StringBuffer();

				for (String iterable_element : mEmo) {
					resultBuffer.append(iterable_element).append("  ");
				}

				Message msg = new Message();
				msg.what = 0;
				msg.obj = resultBuffer.toString();
				mainhandler.sendMessage(msg);

				// Log.e("XUNFEI", resultBuffer.toString());
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	Handler mainhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 0:

				edtEmo.setText((String) msg.obj);
				break;

			case 1:
				edtVoice.setText(edtVoice.getText()+(String) msg.obj);
				edtEmo.setVisibility(View.VISIBLE);
				break;

			case 2:
				edtVoice.setText(edtVoice.getText()+(String) msg.obj);
				edtEmo.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}

		};
	};

}
