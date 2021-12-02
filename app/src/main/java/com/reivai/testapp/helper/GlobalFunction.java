package com.reivai.testapp.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.morefun.yapi.device.printer.FontFamily;
import com.morefun.yapi.device.printer.MulPrintStrEntity;
import com.morefun.yapi.device.printer.OnPrintListener;
import com.reivai.testapp.MainActivity;
import com.reivai.testapp.R;
import com.reivai.testapp.morefun.DeviceHelper;
import com.reivai.testapp.sunmiv1s.AidlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class GlobalFunction {

    // MoreFun check YSDK lib
    public static boolean checkAppInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // MoreFun login YSDK lib
    public static void login() {
        Bundle bundle = new Bundle();
        try {
            showInDebugMode("get device service: " + DeviceHelper.getDeviceService());
            int ret = DeviceHelper.getDeviceService().login(bundle, "09000000");
            if (ret == 0) {
                DeviceHelper.setLoginFlag(true);
                showInDebugMode("login flag TRUE");
                return;
            }
            DeviceHelper.setLoginFlag(false);
            showInDebugMode("login flag FALSE");
            return;

        } catch (RemoteException e) {
            showInDebugMode("Login Service: remote exception error, " + e.toString());
        } catch (NullPointerException e) {
            showInDebugMode("Login Service: Please restart the application, " + e.getLocalizedMessage());
        }
    }

    // sunmi v1s
    public static void connectSumniPrinter(Context context){
        // Cara Connect Printer Sunmi
        AidlUtil.getInstance().connectPrinterService(context);
        AidlUtil.getInstance().initPrinter();
        AidlUtil.getInstance().connectPrinterService(context);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void noInternetConnection(Context context, SweetAlertDialog alertDialog){
        alertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        alertDialog.setTitleText(context.getString(R.string.connection_error)).
                setContentText(context.getString(R.string.connection_error_hint)).show();
    }

    public static void errorDialog(Context context, SweetAlertDialog alertDialog, String message){
        alertDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
        alertDialog.setTitleText(context.getString(R.string.dialog_error)).
                setContentText(message).show();
    }

    // Get image asset
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showInDebugMode("getImageFromAssetsFile Bitmap =" + image);
        return image;
    }

    // Log Tag function
    public static void showInDebugMode(String message){
        Log.d(GlobalParam.TAG, message);
    }

    public static void simpleIntent(Context oldActivity, Class newActivity, Boolean clearTop){
        Intent intent = new Intent(oldActivity, newActivity);
        if(clearTop) intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        oldActivity.startActivity(intent);
    }

    public static void showKeyboard(Context context, EditText editText){
        editText.requestFocus();
        editText.setFocusable(true);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Context context, EditText editText){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String formatRupiah(String text){
        Double rupiah               = Double.parseDouble(text);
        Locale localeID             = new Locale("in", "ID");
        NumberFormat numberFormat   = NumberFormat.getCurrencyInstance(localeID);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setMinimumFractionDigits(0);
        return numberFormat.format(rupiah).replace("Rp", "Rp ");
    }

    public static Bitmap encodeAsBitmap(String str, int width, int height) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, width, height, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
        return bitmap;
    }

    //@SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        String serialNumber;

        try {
            /*@SuppressLint("PrivateApi")*/ Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
//            if (serialNumber.equals(""))
//                get = c.getMethod("get", String.class, String.class);
//                serialNumber = (String) get.invoke(c, "sys.serialnumber", "unknown");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;
//            if (serialNumber.equals(""))
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    serialNumber = Build.getSerial();
//                }
            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }

    public static void printMorefun(Context context, String deviceName, String deviceSN) {
        String test = "Test Print Morefun";
        MainActivity activity = new MainActivity();

        try {
            Bundle config = new Bundle();
            int fontSize = FontFamily.BIG;

            MulPrintStrEntity entity = new MulPrintStrEntity(test,fontSize);
            List<MulPrintStrEntity> entityList = new ArrayList<>();

            entityList.add(entity);
            fontSize = FontFamily.MIDDLE;

            entityList.add(new MulPrintStrEntity(deviceName, fontSize));
            entityList.add(new MulPrintStrEntity(deviceSN, fontSize));
            entityList.add(new MulPrintStrEntity("\n\n", fontSize));

            DeviceHelper.getPrinter().printStr(entityList, new OnPrintListener.Stub() {
                @Override
                public void onPrintResult(int i) {
//                    activity.runOnUiThread(() -> activity.binding.btnPrint.setEnabled(true));
                }
            }, config);
        } catch (RemoteException e) {
            e.printStackTrace();
            showInDebugMode("Please use MoreFun Device, ");
            connectSumniPrinter(context);
            printSunmi(context,
                    deviceName,
                    deviceSN);
        }
    }

    public static void printSunmi(Context context, String deviceName, String deviceSN) {
        String test = "Test Print Sunmi";

        AidlUtil.getInstance().printTitleText(test, 24, true, false, 1, 1);
        AidlUtil.getInstance().printText(deviceName, 20, false, false, 1);
        AidlUtil.getInstance().printText(deviceSN, 20, false, false, 1);
        AidlUtil.getInstance().print3Line();
    }
}
