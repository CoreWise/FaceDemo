package com.core.sfzface;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;



import com.cw.facelib.ZKLiveFaceManager;
import com.cw.idcardsdk.AsyncParseSFZ;
import com.cw.idcardsdk.ParseSFZAPI;
import com.cw.serialportsdk.cw;
import com.cw.serialportsdk.utils.DataUtils;
import com.zkteco.android.graphics.ImageConverter;

import java.io.IOException;

import android_serialport_api.SerialPortManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：李阳
 * 时间：2019/1/14
 * 描述：
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "MainActivity";

    @BindView(R.id.photo)
    ImageView sfzphoto;
    @BindView(R.id.surfaceview)
    SurfaceView mSurfaceView;
    @BindView(R.id.refresh)
    AppCompatImageView refresh;
    @BindView(R.id.iv_carema)
    AppCompatImageView ivCarema;
    @BindView(R.id.iv_activate)
    AppCompatImageView ivActivate;

    private AsyncParseSFZ api;

    private SurfaceHolder mSurfaceHolder;

    private Camera mCamera;

    private final int CAMERA_WIDTH = 640;
    private final int CAMERA_HEIGH = 480;


    private static final int FRONT = 0;//前置摄像头标记
    private static final int BACK = 1;//后置摄像头标记
    private int currentCameraType = -1;//当前打开的摄像头标记

    private byte[] mTemplate1;
    private boolean getSFZTemp;

    private int degree = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        api = new AsyncParseSFZ(getMainLooper(), this);


        if (ZKLiveFaceManager.getInstance().isAuthorized()) {
            if (ZKLiveFaceManager.getInstance().setParameterAndInit("")) {

                Log.e(TAG, getString(R.string.init_algorithm_success));
                Toast.makeText(this, getString(R.string.init_algorithm_success), Toast.LENGTH_SHORT).show();
                ivActivate.setBackgroundDrawable(null);

            } else {

                Toast.makeText(this, getString(R.string.init_algorithm_fail), Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(this, getString(R.string.unAuthorized), Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //无类型限制
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //startActivityForResult(intent, INIT_CODE);
        }

        initCamera();


    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();

        //SerialPortManager.getInstance().openSerialPort(CoreWise.type.sfz);

        api.openIDCardSerialPort(cw.getDeviceModel());

        api.setOnReadSFZListener(new AsyncParseSFZ.OnReadSFZListener() {
            @Override
            public void onReadSuccess(ParseSFZAPI.People people) {
                Log.e(TAG, people.toString());
                if (people.getPhoto() != null) {
                    Bitmap photo = BitmapFactory.decodeByteArray(people.getPhoto(), 0,
                            people.getPhoto().length);
                    sfzphoto.setBackgroundDrawable(new BitmapDrawable(photo));

                    mTemplate1 = ZKLiveFaceManager.getInstance().getTemplateFromBitmap(photo);
                    if (mTemplate1 == null) {
                        Log.e(TAG + "sfz", getString(R.string.extract_template1_fail));
                    } else {
                        Log.e(TAG + "sfz", getString(R.string.extract_template1_success));

                        getSFZTemp = true;
                    }


                } else {
                    Toast.makeText(MainActivity.this, "获取身份证图片失败!", Toast.LENGTH_SHORT).show();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                    sfzphoto.setBackgroundDrawable(new BitmapDrawable(bitmap));

                }

            }


            @Override
            public void onReadFail(int confirmationCode) {
                Log.e(TAG, "错误码:" + confirmationCode);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                sfzphoto.setBackgroundDrawable(new BitmapDrawable(bitmap));
                getSFZTemp = false;


            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPause() {
        super.onPause();
        //SerialPortManager.getInstance().closeSerialPort();
        api.closeIDCardSerialPort(cw.getDeviceModel());


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseCamera();
    }


    @OnClick({R.id.photo, R.id.refresh, R.id.iv_carema, R.id.iv_activate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.photo:
                //点击清空
                sfzphoto.setBackgroundDrawable(null);
                break;
            case R.id.refresh:
                sfzphoto.setBackgroundDrawable(null);

                //重新获取身份证模板
                api.readSFZ(ParseSFZAPI.THIRD_GENERATION_CARD);
                break;
            case R.id.iv_carema:
                //翻转摄像头
                try {

                    //setCamera();
                    // mSurfaceHolder = mSurfaceView.getHolder();
                    //mSurfaceHolder.addCallback(this);
                    changeCamera();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.iv_activate:

                startActivity(new Intent(this, AuthActivity.class));


                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "-----------------surfaceCreated------------------");
        if (mCamera == null) {
            return;
        }
        //mCamera.setPreviewDisplay(mSurfaceHolder);
        cameraConfig(degree);/**/

        mCamera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "-----------------surfaceChanged------------------");

        if (holder.getSurface() == null || mCamera == null) {
            return;
        }
        mCamera.stopPreview();

        cameraConfig(degree);
        //mCamera.setPreviewDisplay(mSurfaceHolder);

        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "-----------------surfaceDestroyed------------------");
    }

    private void initCamera() {

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        mCamera = openCamera(BACK);

        setCamera();

        //U8
        //cameraConfig(currentCameraType == FRONT ? 180 : 360);
    }


    private void setCamera() {

        switch (CoreWise.getAndroidVersion()) {

            case CoreWise.deviceSysVersion.O:
                //A370
                degree = 90;
                break;

            case CoreWise.deviceSysVersion.U:

                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    // 竖屏
                    Log.e(TAG, "-------------竖屏");

                    if (CoreWise.getScreenH(this) == CoreWise.scrren.screen_8_height) {
                        Log.i(TAG, "----U8----");

                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        degree = currentCameraType == FRONT ? 180 : 0;

                    } else {
                        Log.i(TAG, "----U3----");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        //U3
                        degree = 270;
                    }


                } else {
                    // 横屏时

                    Log.e(TAG, "-------------横屏");
                    if (CoreWise.getScreenW(this) == CoreWise.scrren.screen_8_height) {
                        Log.i(TAG, "----U8----");
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        degree = currentCameraType == FRONT ? 180 : 0;
                    } else {
                        Log.i(TAG, "----U3----");
                        //U3
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        degree = 270;
                    }


                }


                break;


        }

        cameraConfig(degree);

    }


    private void cameraConfig(int de) {

        Log.i(TAG, "----degree----" + de);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(CAMERA_WIDTH, CAMERA_HEIGH);
        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setPreviewCallback(new VerifyPreview());
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(de);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }


    /**
     * 释放mCamera
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    private void changeCamera() throws IOException {

        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount == 1) {
            Toast.makeText(this, "该设备只有一个摄像头!", Toast.LENGTH_SHORT).show();
            return;
        }


        releaseCamera();

        if (currentCameraType == FRONT) {
            mCamera = openCamera(BACK);

        } else if (currentCameraType == BACK) {
            mCamera = openCamera(FRONT);
        }


        setCamera();

        //U3
        // cameraConfig(270);

        //U8
        //cameraConfig(currentCameraType == FRONT ? 180 : 360);

    }


    @SuppressLint("NewApi")
    private Camera openCamera(int type) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if (type == FRONT && frontIndex != -1) {
            return Camera.open(frontIndex);
        } else if (type == BACK && backIndex != -1) {
            return Camera.open(backIndex);
        }
        return null;
    }


    class VerifyPreview implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            byte[] data2 = new byte[data.length];

            switch (degree) {
                case 90:
                    ImageConverter.rotateNV21Degree90(data, data2, CAMERA_WIDTH, CAMERA_HEIGH);

                    break;

                case 180:
                    ImageConverter.rotateNV21Degree180(data, data2, CAMERA_WIDTH, CAMERA_HEIGH);

                    break;
                case 270:
                    ImageConverter.rotateNV21Degree270(data, data2, CAMERA_WIDTH, CAMERA_HEIGH);

                    break;

            }


            Log.e("cameradata", "data2: " + DataUtils.bytesToHexString(data2));
            // TODO Auto-generated method stub

            byte[] mTemplate2 = ZKLiveFaceManager.getInstance().getTemplateFromNV21(data2, CAMERA_HEIGH, CAMERA_WIDTH);

            if (mTemplate2 == null) {
                Log.e("camera", getString(R.string.extract_template2_fail));
            } else {
                Log.e("camera", getString(R.string.extract_template2_success));

                //身份证模板获取成功了
                if (getSFZTemp) {

                    //比对
                    int score = ZKLiveFaceManager.getInstance().verify(mTemplate1, mTemplate2);
                    if (score >= ZKLiveFaceManager.getInstance().DEFAULT_VERIFY_SCORE) {
                        Log.e("camera" + "camera", getString(R.string.verify_success));

                        Toast.makeText(MainActivity.this, getString(R.string.verify_success), Toast.LENGTH_SHORT).show();
                        //
                        getSFZTemp = false;
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        sfzphoto.setBackgroundDrawable(new BitmapDrawable(bitmap));
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.verify_fail), Toast.LENGTH_SHORT).show();
                        Log.e("camera", getString(R.string.verify_fail));

                    }
                } else {
                    Log.e("camera", "身份证图片未采集!!!");
                }
            }


        }
    }


}
