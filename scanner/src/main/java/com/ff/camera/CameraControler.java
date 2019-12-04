package com.ff.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.util.List;

public class CameraControler {

    private static Boolean mDebugAble = true;
    private static ICallBack mCallback;
    private static Context mContext;
    static CameraControler mCameraControler = new CameraControler();
    private static int mPicSize = 3;

    public CameraControler setDebugAble(Boolean debugAble) {
        mDebugAble = debugAble;
        return mCameraControler;
    }

    public static boolean isDebugAble() {
        return mDebugAble;
    }

    public CameraControler setPicSize(int size) {
        mPicSize = size;
        return mCameraControler;
    }

    public static int getPicSize() {
        return mPicSize;
    }

    public static CameraControler with(Context context) {
        mContext = context;
        return mCameraControler;
    }


    public void show() {
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
    }

    /**
     * 关闭扫描页面
     */
    public static void close() {
        if (MainActivity.instance != null) {
            MainActivity.instance.finish();
            MainActivity.instance = null;
        }
    }

    /**
     * 停止图片抓取
     */
    public static void stopPick() {
        if (MainActivity.instance != null) {
            MainActivity.instance.stopTimmer();
        }
    }

    private static void startPick() {
        if (MainActivity.instance != null) {
            MainActivity.instance.startDataPickTimmer();
            MainActivity.instance.startFocusTimmer();
        }
    }

    /**
     * 继续抓取
     */
    public static void restartPick() {
        stopPick();
        startPick();
    }


    public CameraControler setCallback(ICallBack callback) {
        mCallback = callback;
        return mCameraControler;
    }

    public static ICallBack getCallback() {
        return mCallback;
    }


    public interface ICallBack {
        void callback(List<Bitmap> bitmaps);
    }


}
