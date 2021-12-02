package com.reivai.testapp.morefun;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.reivai.testapp.helper.GlobalParam;
import com.reivai.testapp.sunmiv1s.AidlUtil;
import com.morefun.yapi.engine.DeviceServiceEngine;

public class MyApplication extends Application {
    private final String TAG = GlobalParam.TAG;

    // morefun 919
    private DeviceServiceEngine deviceServiceEngine;

    // sunmi v1s
    private boolean isAidl;
    public boolean isAidl() {
        return isAidl;
    }
    public void setAidl(boolean aidl) {
        isAidl = aidl;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindDeviceService();
        isAidl = true;
        AidlUtil.getInstance().connectPrinterService(this);
    }

    public DeviceServiceEngine getDeviceService() {
        return deviceServiceEngine;
    }

    public void bindDeviceService() {
        String SERVICE_ACTION = "com.morefun.ysdk.service";
        String SERVICE_PACKAGE = "com.morefun.ysdk";

        if (null != deviceServiceEngine) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(SERVICE_ACTION);
        intent.setPackage(SERVICE_PACKAGE);

        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            deviceServiceEngine = null;
            Log.e(TAG, "service : ======onServiceDisconnected======");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            deviceServiceEngine = DeviceServiceEngine.Stub.asInterface(service);
            Log.d(TAG, "service : ======onServiceConnected======");

            try {
                DeviceHelper.reset();
                DeviceHelper.initDevices(MyApplication.this);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            linkToDeath(service);
        }

        private void linkToDeath(IBinder service) {
            try {
                service.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        Log.d(TAG, "service : ======binderDied======");
                        deviceServiceEngine = null;
                        bindDeviceService();
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
}
