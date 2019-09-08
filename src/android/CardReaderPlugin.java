package com.stc.CardReaderPlugin;

import org.apache.cordova.*;
import org.json.*;

import java.util.List;
import me.cosmodro.app.rhombus.AudioMonitor;
import me.cosmodro.app.rhombus.MessageType;
import me.cosmodro.app.rhombus.decoder.*;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Context;
import android.media.AudioManager;
import com.stc.CardReaderPlugin.PluginActivity;

public class CardReaderPlugin extends CordovaPlugin {
	public static String TAG = "CardDump";
	private Handler mHandler;
	private AudioMonitor monitor;
	private AudioDecoder decoder;
	private Thread listenerThread;

	private CallbackContext ctx;
	
	public static CordovaWebView cWebView;
		
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		ctx = callbackContext;
    	if(action.equals("initReader")){
			//Init the reader
			try{
				Context context = this.cordova.getActivity().getApplicationContext();
				AudioManager audioManager = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE));
				audioManager.setMode(AudioManager.MODE_IN_CALL);
				audioManager.setMicrophoneMute(true);

				cWebView = this.webView;
				PluginActivity.dongleReady = false;		

                mHandler = new Handler(){
                	public void handleMessage(Message msg){
                		MessageType message = MessageType.values()[msg.what];
    					String swipedData = "";
    					Boolean withErrors = true;
                		switch (message){
                			case DATA_PRESENT:
    							withErrors = false;
                				break;
                			case NO_DATA_PRESENT:
    							withErrors = false;
                        		break;
                			case RECORDING_ERROR:
                				swipedData = "";
                				startListening();
                				break;
                			case INVALID_SAMPLE_RATE:
                				swipedData = "Unsupported Sample Rate";
                				break;
                			case DATA:
                				List<Integer> data = (List<Integer>) msg.obj;
    							swipedData = processSwipe(decoder.processData(data)).toString();
    	                		cWebView.sendJavascript("javascript:CardReaderPlugin.onSwipeDataReceived('" + swipedData + "')");
    							withErrors = false;
                				startListening();
                				break;
                		}
                	}
                };
                
                monitor = new AudioMonitor(mHandler);
                decoder = new AudioDecoder();
                
                PluginActivity.dongleReady = true;
                startListening();
                
				PluginResult pluginRes = new PluginResult(PluginResult.Status.OK, "");
				ctx.sendPluginResult(pluginRes);
			}
			catch(Exception ex){
				PluginResult pluginRes = new PluginResult(PluginResult.Status.ERROR, ex.getMessage());
				ctx.sendPluginResult(pluginRes);
			}
		}
		else if(action.equals("startReader")){
			try{
				startListening();
				PluginResult pluginRes = new PluginResult(PluginResult.Status.OK, "Started!");
				ctx.sendPluginResult(pluginRes);
			}
			catch(Exception ex){
				PluginResult pluginRes = new PluginResult(PluginResult.Status.ERROR, ex.getMessage());
				ctx.sendPluginResult(pluginRes);
			}
		}
		else{
			try{
				stopListening();
				PluginResult pluginRes = new PluginResult(PluginResult.Status.OK, "Stopped!");
				ctx.sendPluginResult(pluginRes);
			}
			catch(Exception ex){
				PluginResult pluginRes = new PluginResult(PluginResult.Status.ERROR, ex.getMessage());
				ctx.sendPluginResult(pluginRes);
			}
		}
		       
        return true;
    }
    
    private void startListening(){
    	Log.d(TAG, "in StartListening, dongleReady:"+PluginActivity.dongleReady);
    	if (!PluginActivity.dongleReady){
    		return;
    	}else{
    		stopListening();
    		monitor.setFrequency(getFrequency());
    		decoder.setMinLevelCoeff(getMinLevelCoeff()/100f);
    		monitor.setSilenceLevel(getSilenceLevel());
    		decoder.setSilenceLevel(getSilenceLevel());
    		decoder.setSmoothing(getSmoothing()/100f);
	        listenerThread = new Thread(new Runnable() {
	        	public void run() {
		        	monitor.monitor();
	        	}
	    	});
	    	listenerThread.start();
    	}
    }
    
    private void stopListening(){
      if (listenerThread != null){
    	monitor.stopRecording();
    	try{
    		listenerThread.join(5000);
    	}catch(InterruptedException ie){
    		Log.d(TAG, "Interruped Exception in CardDumpActivity.stopListening");
    	}finally{
    		listenerThread = null;
    	}
      }
    }
    
    private String processSwipe(SwipeData swipe){
    	String raw = swipe.content;
    	//set decoded
		if (swipe.isBadRead()){
			return "Bad Read";
		}else{
			return raw.toString();
		}
    }
    
    private int getFrequency(){
    	return 44100;
    }
    
    private int getMinLevelCoeff(){
    	return 18;
    }
    
    private int getSilenceLevel(){
    	return 310;
    }
    
    private int getSmoothing(){
    	return 22;
    }
}