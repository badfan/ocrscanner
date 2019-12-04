package com.ff.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.ff.foucsurfaceview.FocusSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.Manifest.permission.CAMERA;
import static com.ff.camera.PictureFragment.CROP_PICTURE;
import static com.ff.camera.PictureFragment.ORIGIN_PICTURE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "moubiao";
    public static MainActivity instance;

    private FocusSurfaceView previewSFV;
    private Button mTakeBT, mThreeFourBT, mFourThreeBT, mNineSixteenBT, mSixteenNineBT, mFitImgBT, mCircleBT, mFreeBT, mSquareBT,
            mCircleSquareBT, mCustomBT;

    private Camera mCamera;
    private SurfaceHolder mHolder;
    private boolean focus = false;
    private RecyclerView recyclerView;
    private List<Bitmap> bitmaps = new ArrayList<>();
    List<Bitmap> bitmapsChecked = new ArrayList<>();
    private MyAdapter mAdapter;
    private HorizontalScrollView scrollView;
    byte[] mPreviewData;
    private boolean isInit = false;
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    @SuppressLint("HandlerLeak")
    Handler picHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                startDataPickTimmer();
                try {
                    Log.e("pic", "预览图片生成," + mPreviewData);
                    if (mPreviewData != null) {
                        Log.e("pic", "预览图片生成," + mPreviewData.length);

//                   Bitmap originBitmap = BitmapFactory.decodeByteArray(mPreviewData, 0, mPreviewData.length);
                        if (focus = true) {
//                            focus = false;
                            executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap cropBitmap = previewSFV.getPreviewPicture(mPreviewData, PRE_WIDTH, PRE_HEIGHT);

                                    Message message = picHandler.obtainMessage(2, cropBitmap);
                                    picHandler.sendMessage(message);
                                }
                            });

//                            if (bitmaps.size() >= CameraControler.getPicSize()) {
//                                bitmaps.clear();
//                            }
//                            bitmaps.add(cropBitmap);
//                            if (bitmaps.size() >= CameraControler.getPicSize()) {
//                                if (CameraControler.getCallback() != null) {
//                                    bitmapsChecked.clear();
//                                    bitmapsChecked.addAll(bitmaps);
//                                    CameraControler.getCallback().callback(bitmapsChecked);
//                                }
//                            }
//                            if (CameraControler.isDebugAble()) {
//                                mAdapter.notifyDataSetChanged();
//                                recyclerView.scrollToPosition(bitmaps == null ? 0 : bitmaps.size());
//                            }
//                            mPreviewData = null;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (msg.what == 2) {
                Bitmap cropBitmap = (Bitmap) msg.obj;
                if (bitmaps.size() >= CameraControler.getPicSize()) {
//                    for (Bitmap bitmap : bitmaps) {
//                        bitmap.recycle();
//                    }
                    bitmaps.clear();
                    i = 0;
                }
                i++;
                bitmaps.add(cropBitmap);
                Log.e("bitmap", "bitmaps===" + bitmaps.size());
                if (bitmaps.size() >= CameraControler.getPicSize()) {
                    if (CameraControler.getCallback() != null) {
                        bitmapsChecked.clear();
                        bitmapsChecked.addAll(bitmaps);
                        Log.e("bitmap", "bitmapsChecked===" + bitmapsChecked.size());
                        CameraControler.getCallback().callback(bitmapsChecked);
                    }
                }
                if (CameraControler.isDebugAble()) {
                    mAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(bitmaps == null ? 0 : bitmaps.size());
                }
                mPreviewData = null;
            }
        }
    };
    Handler preHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 2) {
                startFocusTimmer();
                onfocused();
            }
        }
    };
    int i = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);
        instance = this;
        initData();
        initView();
        initRecyclerView();
        setListener();
        startDataPickTimmer();
        startFocusTimmer();
        previewSFV.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

    }

    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!isInit) {
                previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_16_9);
                isInit = true;
            }
        }
    };

    private void initData() {
        DetectScreenOrientation detectScreenOrientation = new DetectScreenOrientation(this);
        detectScreenOrientation.enable();
    }

    private void initView() {
        previewSFV = (FocusSurfaceView) findViewById(R.id.preview_sv);
        mHolder = previewSFV.getHolder();
        mHolder.addCallback(MainActivity.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mTakeBT = (Button) findViewById(R.id.take_bt);
        mThreeFourBT = (Button) findViewById(R.id.three_four_bt);
        mFourThreeBT = (Button) findViewById(R.id.four_three_bt);
        mNineSixteenBT = (Button) findViewById(R.id.nine_sixteen_bt);
        mSixteenNineBT = (Button) findViewById(R.id.sixteen_nine_bt);
        mFitImgBT = (Button) findViewById(R.id.fit_image_bt);
        mCircleBT = (Button) findViewById(R.id.circle_bt);
        mFreeBT = (Button) findViewById(R.id.free_bt);
        mSquareBT = (Button) findViewById(R.id.square_bt);
        mCircleSquareBT = (Button) findViewById(R.id.circle_square_bt);
        mCustomBT = (Button) findViewById(R.id.custom_bt);
        scrollView = findViewById(R.id.scrollView);
        recyclerView = findViewById(R.id.recyclerView);


//        previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_16_9);

//        previewSFV.setCropEnabled(false);
        if (CameraControler.isDebugAble()) {
            scrollView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

        } else {
            scrollView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void setListener() {
        mTakeBT.setOnClickListener(this);
        mThreeFourBT.setOnClickListener(this);
        mFourThreeBT.setOnClickListener(this);
        mNineSixteenBT.setOnClickListener(this);
        mSixteenNineBT.setOnClickListener(this);
        mFitImgBT.setOnClickListener(this);
        mCircleBT.setOnClickListener(this);
        mFreeBT.setOnClickListener(this);
        mSquareBT.setOnClickListener(this);
        mCircleSquareBT.setOnClickListener(this);
        mCustomBT.setOnClickListener(this);
    }


    public void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.picture_item, viewGroup, false);
            return new MyViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
            viewHolder.setData(bitmaps.get(i));
        }

        @Override
        public int getItemCount() {
            return bitmaps.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView cropImg;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                cropImg = itemView.findViewById(R.id.img);
            }

            public void setData(Bitmap bitmap) {
                Log.e("recycleview", "bitmap" + bitmap.toString() + ",,,," + bitmaps.size());
                cropImg.setImageBitmap(bitmap);
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
        setCameraParams();
//        onfocused();
    }

    private void initCamera() {
        if (checkPermission()) {
            try {
                mCamera = Camera.open(0);//1:采集指纹的摄像头. 0:拍照的摄像头.
                mCamera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Snackbar.make(mTakeBT, "camera open failed!", Snackbar.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 10000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10000:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        initCamera();
                        setCameraParams();
                    }
                }

                break;
        }
    }

    public final static int PRE_WIDTH = 1920;
    public final static int PRE_HEIGHT = 1080;

    private void setCameraParams() {
        if (mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();

            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

            int width = 0;
            int height = 0;
            for (Camera.Size size : supportedPreviewSizes) {

                Log.e("size", size.width + ",," + size.height);
                if (size.width >= width) {
                    width = size.width;
                    height = size.height;
                }
            }


            int orientation = judgeScreenOrientation();
            if (Surface.ROTATION_0 == orientation) {
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
//                parameters.setPictureSize(width, height);
//                parameters.setPreviewSize(width, height);
            } else if (Surface.ROTATION_90 == orientation) {
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
//                parameters.setPictureSize(height, width);
//                parameters.setPreviewSize(height, width);
            } else if (Surface.ROTATION_180 == orientation) {
                mCamera.setDisplayOrientation(180);
                parameters.setRotation(180);
//                parameters.setPictureSize(width, height);
//                parameters.setPreviewSize(width, height);
            } else if (Surface.ROTATION_270 == orientation) {
                mCamera.setDisplayOrientation(180);
                parameters.setRotation(180);
//                parameters.setPictureSize(height, width);
//                parameters.setPreviewSize(height, width);

            }
//            parameters.setPictureSize(1280,720);
//            parameters.setPreviewSize(1280,720);
            parameters.setPictureSize(1920, 1080);
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);


//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            mCamera.setParameters(parameters);

            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
//                    Log.e("camera", "预览data=" + data.length);
//                    takePicture();
//                    Log.e("camera", "onPreviewFrame,,focus==" + focus);
                    if (focus) {
                        mPreviewData = data;
                    }

                }
            });
            mCamera.startPreview();
//            mCamera.cancelAutoFocus();//聚焦
//            onfocused();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onfocused() {
        if (mCamera == null) {
            return;
        }
        Log.e("camera", "---------onfocused()-------");
        focus = false;
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                focus = success;
                Log.e("camera", "focus==" + focus);
                if (success) {
                    mCamera.cancelAutoFocus();
                }
            }
        });
    }

    public void startDataPickTimmer() {
        Log.e("timmer", "--------------timmer--------------");
        picHandler.sendEmptyMessageDelayed(1, 1500);
    }

    public void startFocusTimmer() {
        Log.e("timmer", "--------------startFocusTimmer--------------");
        preHandler.sendEmptyMessageDelayed(2, 3000);

//        picHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                onfocused();
//            }
//        }, 3000);
    }

    public void stopTimmer() {
        picHandler.removeMessages(1);
        preHandler.removeMessages(2);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimmer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * 判断屏幕方向
     *
     * @return 0：竖屏 1：左横屏 2：反向竖屏 3：右横屏
     */
    private int judgeScreenOrientation() {
        return getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopTimmer();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.take_bt) {
            if (!focus) {
                takePicture();
            }

        } else if (i == R.id.three_four_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_3_4);

        } else if (i == R.id.four_three_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_4_3);

        } else if (i == R.id.nine_sixteen_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_9_16);

        } else if (i == R.id.sixteen_nine_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.RATIO_16_9);

        } else if (i == R.id.fit_image_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.FIT_IMAGE);

        } else if (i == R.id.circle_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.CIRCLE);

        } else if (i == R.id.free_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.FREE);

        } else if (i == R.id.square_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.SQUARE);

        } else if (i == R.id.circle_square_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.CIRCLE_SQUARE);

        } else if (i == R.id.custom_bt) {
            previewSFV.setCropMode(FocusSurfaceView.CropMode.CUSTOM);

        } else {
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                focus = success;
                if (success) {
                    mCamera.cancelAutoFocus();
                    mCamera.takePicture(new Camera.ShutterCallback() {
                        @Override
                        public void onShutter() {
                        }
                    }, null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Bitmap originBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap cropBitmap = previewSFV.getPicture(data);
                            PictureFragment pictureFragment = new PictureFragment();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(ORIGIN_PICTURE, originBitmap);
                            bundle.putParcelable(CROP_PICTURE, cropBitmap);
                            pictureFragment.setArguments(bundle);
                            pictureFragment.show(getFragmentManager(), null);

                            focus = false;
                            mCamera.startPreview();
                        }
                    });
                }
            }
        });
    }


    /**
     * 用来监测左横屏和右横屏切换时旋转摄像头的角度
     */
    private class DetectScreenOrientation extends OrientationEventListener {
        DetectScreenOrientation(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
//            Log.e("ggg", "orientation==" + orientation);
            if (260 < orientation && orientation < 290) {
                Log.e("setCameraParams", "setCameraParams===");
                setCameraParams();
            } else if (80 < orientation && orientation < 100) {
                setCameraParams();
            }
        }
    }


}
