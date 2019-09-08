package com.stc.CardReaderPlugin;

import me.cosmodro.app.rhombus.HeadsetStateReceiver;
import me.cosmodro.app.rhombus.RhombusActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class PluginActivity extends Activity implements RhombusActivity {
	HeadsetStateReceiver dongleListener;
	static boolean dongleReady;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        dongleListener = new HeadsetStateReceiver(this);
        registerReceiver(dongleListener,receiverFilter);
	}


	public void setDongleReady(boolean ready){
    	PluginActivity.dongleReady = ready;
    	Log.d("------------------------","Dongle Ready" + String.valueOf(ready));
    }
}