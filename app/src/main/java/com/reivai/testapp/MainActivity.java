package com.reivai.testapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import com.reivai.testapp.databinding.ActivityMainBinding;
import com.reivai.testapp.helper.GlobalFunction;
import com.reivai.testapp.morefun.DeviceHelper;

public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    Context context;
    String deviceName = "", deviceSN = "";
    MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = this;
        initView();
    }

    private void initView() {
        context = this;
        deviceName = Build.MODEL;

        if (GlobalFunction.checkAppInstalled(context, "com.morefun.ysdk")) {
            GlobalFunction.login();
        } else {
            GlobalFunction.showInDebugMode("Please install ysdk first");
        }

        binding.btnCheckSN.setOnClickListener(v -> {
            DeviceHelper.getLoginSuccess();
            deviceSN = GlobalFunction.getSerialNumber();
            binding.tvSN.setText(deviceSN);
            Log.d("wakacaw", "SN: " + deviceSN);
        });

        binding.btnPrint.setOnClickListener(v -> {

            try {
                GlobalFunction.printMorefun(context, deviceName, deviceSN);
            } catch (Exception e) {
                GlobalFunction.connectSumniPrinter(context);
                GlobalFunction.printSunmi(context, deviceName, deviceSN);
            }
        });
    }
}