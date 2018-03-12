package com.vism.lib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vism.gethlibrary.HttpUtils;
import com.vism.gethlibrary.VismGeth;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity{
    VismGeth vismGeth =null;
    Context mContext = null;

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage( Message msg ){
           switch (msg.what){
               case VismGeth.HANDLER_MSG_WHAT:
                   Log.e(TAG,"收到的信息是:"+msg.getData().getString(VismGeth.HANDLER_MSG_BALANCE));
                   break;
                   default:
                       break;
           }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        vismGeth = new VismGeth(mContext,mHandler);
        vismGeth.getBalanceAsyncHandle("0x909Aa5ad1e3BB554347c763c06b8681E9C5A9694","0x56623108b27efc888c8c2f95ad93c1dc7113be4e");
    }
}
