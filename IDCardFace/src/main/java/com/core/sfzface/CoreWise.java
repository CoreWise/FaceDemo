package com.core.sfzface;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;

/**
 * 时间：2018/10/16
 * 描述：
 * <p>
 * 型号封装,后续产品都可以在这里添加型号，
 * 然后在SerialPortManager.java添加相关的串口打开关闭处理
 * <p>
 * 1.在getModel里添加产品判别分类
 * 2.检测getModel使用位置，在各位置添加相应逻辑处理
 */
public class CoreWise {


    /**
     * 肯麦思产品设备型号接口
     */
    public interface device {
        int A370 = 0;
        int CFON640 = 1;
        int other = 2;
        int U3_640 = 3;
        int U3_A370 = 4;
    }


    public interface scrren {

        int screen_5_height = 1280;
        int screen_5_width = 720;

        int screen_43_height = 800;
        int screen_43_with = 480;


        int screen_8_height = 1776;
        int screen_8_with = 1200;

        int screen_7_height = 800;
        int screen_7_with = 480;
    }

    public static int getScreenH(Context context) {

        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        return height;

    }


    public static int getScreenW(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        return width;

    }


    /**
     * 肯麦思产品设备型号系统版本接口
     */
    public interface deviceSysVersion {

        /**
         * 海派之类老产品
         */
        int O = Build.VERSION_CODES.M;

        /**
         * U系列的新产品
         */
        int U = Build.VERSION_CODES.N_MR1;
    }


    /**
     * 肯麦思产品设备功能接口
     */
    public interface type {
        int DEFAULT = 100;
        int sfz = 111;
        int uhf = 112;
    }

    public static String[] deviceName = new String[]{"A370", "CFON640", "其他机型", "msm8953 for arm64"};


    /**
     * 获取设备型号，用来判断使用什么指令
     *
     * @return 0: A370 1:CFON640 2:其他机型,3:msm8953 for arm64
     */

    @Deprecated
    public static int getModel() {
        String model = android.os.Build.MODEL;
        Log.i("TAG", "---2---" + model);
        File file = new File("/sys/class/fbicode_gpios/fbicoe_state/control");
        if (model.contains("CFON640") || model.contains("COREWISE_V0")) {
            return device.CFON640;
        } else if (model.equals("A370") || file.exists()) {
            return device.A370;
        } else if (model.equals("msm8953 for arm64")) {
            return device.U3_640;
        } else if (model.equals("msm8909")) {
            return device.U3_A370;
        } else /*if (model.equals("U3")) {
            return device.U3_640;
        }else*/ {
            return 2;
        }
    }

    public static int getAndroidVersion() {
        int sdkInt = Build.VERSION.SDK_INT;
        return sdkInt;
    }

}
