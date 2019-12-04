# ocrscanner
相机扫描拍照识别sad

使用方法:
CameraControler.with(this).setDebugAble(true).setPicSize(3).setCallback(new CameraControler.ICallBack() {
            @Override
            public void callback(List<Bitmap> list) {

            }
        }).show();
