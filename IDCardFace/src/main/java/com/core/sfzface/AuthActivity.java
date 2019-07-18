package com.core.sfzface;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zkteco.android.biometric.liveface56.ZKLiveFaceService;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 作者：李阳
 * 时间：2019/3/6
 * 描述：
 */
public class AuthActivity extends AppCompatActivity {


    private TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("人脸比对激活界面!");
        textView = (TextView) findViewById(R.id.txtViewResult);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1000);
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1000);
        }
    }


    public void OnBnGetDevFP(View view) {

        if (!isNetworkAvailable()) {
            textView.setText("请确保机器能上网!");
            Toast.makeText(this, "请确保机器能上网!", Toast.LENGTH_SHORT).show();

            return;
        }


        if (ZKLiveFaceService.isAuthorized() || ZKLiveFaceService.getChipAuthStatus()) {
            textView.setText("设备已激活或者有加密芯片!");
            return;
        }
        String strHwid = "";
        String strDevFP = "";
        byte[] hwid = new byte[256];
        int retLen[] = new int[1];
        retLen[0] = 256;

        int retVal = ZKLiveFaceService.getHardwareId(hwid, retLen);
        if (0 != retVal) {
            textView.setText("获取机器码失败!");
            return;
        }
        strHwid = new String(hwid, 0, retLen[0]);
        byte[] bufDevFp = new byte[32 * 1024];
        retLen[0] = 32 * 1024;
        retVal = ZKLiveFaceService.getDeviceFingerprint(bufDevFp, retLen);
        if (0 != retVal) {
            textView.setText("获取设备指纹信息失败!");
            return;
        }
        strDevFP = new String(bufDevFp, 0, retLen[0]);
        String fileName = "/sdcard/" + strHwid + ".txt";
        saveFile(fileName, strDevFP);
        textView.setText("设备机器码：" + strHwid + "\r\n请从" + fileName + "下载设备指纹信息文件，并发送给商务申请授权文件！");
    }

    public void OnBnTestAuth(View view) {
        int retVal = 0;
        if (!ZKLiveFaceService.isAuthorized() && !ZKLiveFaceService.getChipAuthStatus())    //设备未激活或无加密芯片
        {
            String strHwid = "";
            byte[] hwid = new byte[256];
            int retLen[] = new int[1];
            retLen[0] = 256;

            retVal = ZKLiveFaceService.getHardwareId(hwid, retLen);
            if (0 != retVal) {
                textView.setText("获取机器码失败!");
                return;
            }
            strHwid = new String(hwid, 0, retLen[0]);
            String fileName = "/sdcard/" + strHwid + ".lic";
            retVal = ZKLiveFaceService.setParameter(0, 1011, fileName.getBytes(), fileName.length());
            if (0 != retVal) {
                textView.setText("设置许可文件失败!");
                return;
            }
        }

        long[] context = new long[1];
        retVal = ZKLiveFaceService.init(context);
        if (0 != retVal) {
            textView.setText("激活失败!");
            return;
        }
        ZKLiveFaceService.terminate(context[0]);
        textView.setText("激活成功!");
    }

    public void OnBnCheckLic(View view) {
        if (ZKLiveFaceService.isAuthorized()) {
            textView.setText("设备已激活");
        } else {
            textView.setText("设备未激活");
        }
    }

    public void OnBnCheckIC(View view) {
        if (ZKLiveFaceService.getChipAuthStatus()) {
            textView.setText("设备有加密芯片");
        } else {
            textView.setText("设备无加密芯片");
        }
    }

    private void saveFile(String filename, String data) {
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream(filename);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFile(String filename) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(filename);
            byte temp[] = new byte[1024];
            StringBuilder sb = new StringBuilder("");
            int len = 0;
            while ((len = inputStream.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            Log.d("msg", "readLicenseFile: \n" + sb.toString());
            inputStream.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 检测网络是否连接
     *
     * @return
     */
    private boolean isNetworkAvailable() {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;

    }
}
