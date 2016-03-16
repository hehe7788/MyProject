package myproject.ycc.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class CrimeCameraFragment extends Fragment {

    private static final String TAG = "CrimeCameraFragment";
    public static final String EXTRA_PHOTO_FILENAME = "byr.criminalintent.photo_filename";
    public static final String EXTRA_PHOTO_DEGREE = "byr.criminalintent.photo_degree";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private View mProgressContainer;
    private String mExternalStoragePath = "/criminal_intent_camera";
    private SensorManager sm;
    private OrientationEventListener mOrientationEventListener;
    private int degree;
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            //显示progressbar
            mProgressContainer.setVisibility(View.VISIBLE);
        }
    };
    private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String fileName = UUID.randomUUID().toString() + ".jpg";

            boolean success = true;
            //外部存储 获取外部存储设备（SD卡）的路径
            File sdCardDictionary = Environment.getExternalStorageDirectory();
            File sdCardFile = new File(sdCardDictionary + mExternalStoragePath + "/" + fileName);
            FileOutputStream sdOut = null;
            //如果文件目录不存在，则创建目录
            if (!sdCardFile.getParentFile().exists()) {
                sdCardFile.getParentFile().mkdirs();
            }
            try {
                sdOut = new FileOutputStream(sdCardFile);
                sdOut.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file" + fileName, e);
                success = false;
            } finally {
                try {
                    if (sdOut != null) {
                        sdOut.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file" + fileName, e);
                    success = false;
                }
            }
            if (success) {
                Log.e(TAG, "JPEG saved at " + fileName);
                //将文件名回传给CrimePageActivity!
                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, fileName);
                i.putExtra(EXTRA_PHOTO_DEGREE, degree);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
            getActivity().finish();
        }
    };

    public CrimeCameraFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        mProgressContainer = v.findViewById(R.id.crime_camera_progressContainer);
        mProgressContainer.setVisibility(View.INVISIBLE);

        Button takePictureButton = (Button) v.findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    mCamera.takePicture(mShutterCallback, null, mJpegCallback);
                }
            }
        });

        mSurfaceView = (SurfaceView) v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //setPreviewDisplay
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null) {
                    //Surface大小改变时，改变camera preview大小，改变图片尺寸大小
                    Camera.Parameters parameters = mCamera.getParameters();
                    Camera.Size s = getBestSupportSize(parameters.getSupportedPreviewSizes(), width, height);
                    parameters.setPreviewSize(s.width, s.height);
                    s = getBestSupportSize(parameters.getSupportedPictureSizes(), width, height);
                    parameters.setPictureSize(s.width, s.height);
                    mCamera.setParameters(parameters);
                    try {
                        mCamera.startPreview();
                    } catch (Exception e) {
                        Log.e(TAG, "Could not start preview", e);
                        mCamera.release();
                        mCamera = null;
                    }
                }

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }
        });

        mOrientationEventListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                // orientation的范围是0～359
                // 右旋，屏幕左边在顶部的时候 orientation = 90;
                // 倒置，屏幕顶部在底部的时候 orientation = 180;
                // 左旋，屏幕右边在顶部的时候 orientation = 270;
                // 竖直，正常情况默认orientation = 0;
                if(orientation >= 350 || orientation <= 10) {
                    //竖直OK
                    degree = 90;
                } else if(orientation >= 80 && orientation <= 100) {
                    //右旋OK
                    degree = 180;
                } else if(orientation >= 170 && orientation <= 190) {
                    //倒转OK
                    degree = 270;
                } else if(orientation >= 260 && orientation <= 280){
                    //左旋OK
                    degree = 0;
                }
            }
        };
        mOrientationEventListener.enable();
        return v;
    }

    /**
     * 不优雅的计算最佳surface预览尺寸
     */
    private Camera.Size getBestSupportSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
    }

    //保证用户能够同fragment视图交互时，相机才可使用
    @Override
    public void onResume() {
        super.onResume();
        mCamera = Camera.open(0);
    }

    //及时释放资源
    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            //停止sensor
        }
        mOrientationEventListener.disable();
    }
}
