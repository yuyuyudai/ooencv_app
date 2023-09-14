package com.example.youtube_opencv;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        //requesting previewsize option (maxWidth > maxHeight)
        mOpenCvCameraView.setMaxFrameSize(960, 720);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mMat = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mMat.release();
    }
    private Mat mMat;
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //-------https://coskxlabsite.stars.ne.jp/html/android/OpenCVpreview/OpenCVpreview_A.html
        //return inputFrame.rgba();
        //mMat= inputFrame.rgba();    //color
        //mMat= inputFrame.gray();    //grayscale
        //Core.bitwise_not(inputFrame.rgba(), mMat); //reversed
        //Core.bitwise_not(inputFrame.gray(), mMat); //grayscale reversed
        //Imgproc.Canny(inputFrame.gray(), mMat, 100, 200); //grayscale canny filtering
        //Imgproc.threshold(inputFrame.gray(), mMat, 0.0, 255.0, Imgproc.THRESH_OTSU); //grayscale binarization with Ohtsu
        //return mMat;//これで表示


        //--------------hsv変換
        /*Mat rgbImage = inputFrame.rgba();//rgb画像を取得
        Mat hsvImage = new Mat();//空のhsv画像を作成

        // RGBからHSVに変換
        Imgproc.cvtColor(rgbImage, hsvImage, Imgproc.COLOR_RGB2HSV);

        // ここでhsvImageを使用してさまざまな処理を行うことができます

        // 画像を表示
        return hsvImage;*/



        Mat rgbaImage = inputFrame.rgba(); // RGB画像を取得
        Mat hsvImage = new Mat(); // 空のHSV画像を作成
        Mat hueChannel = new Mat(); // 色相チャンネル画像を格納
        Mat binaryImage = new Mat(); // 二値化画像を格納
        Mat combineImage = new Mat();

        // RGBからHSVに変換
        Imgproc.cvtColor(rgbaImage, hsvImage, Imgproc.COLOR_RGB2HSV);

        // 色相チャンネルを抽出
        List<Mat> channels = new ArrayList<>();
        Core.split(hsvImage, channels);
        hueChannel = channels.get(0);

        // 濃い赤色の抽出
        Scalar dr_lowerBound = new Scalar(0, 50, 0);
        Scalar dr_upperBound = new Scalar(1, 255, 255);
        Core.inRange(hsvImage, dr_lowerBound, dr_upperBound, binaryImage);

        // 赤色の抽出
        Scalar r_lowerBound = new Scalar(2, 50, 50);
        Scalar r_upperBound = new Scalar(10, 255, 255);
        Core.inRange(hsvImage, r_lowerBound, r_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);




        // オレンジ色の抽出
        Scalar o_lowerBound = new Scalar(11, 0, 0);
        Scalar o_upperBound = new Scalar(25, 255, 255);
        Core.inRange(hsvImage, o_lowerBound, o_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);


        Mat redImage = new Mat();
        rgbaImage.copyTo(redImage, binaryImage);

        // 緑色の抽出
        Scalar gr_lowerBound = new Scalar(26, 30, 50);
        Scalar gr_upperBound = new Scalar(48, 255, 255);
        Core.inRange(hsvImage, gr_lowerBound, gr_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);





        // 二値化画像をもとのRGB画像と合成
        Mat resultImage = new Mat();
        rgbaImage.copyTo(resultImage, binaryImage);

        //TextView myTextView = findViewById(R.id.mytextView);
        //int pixelsInMask1 = Core.countNonZero(redImage);
        //int pixelsInMask2 = Core.countNonZero(resultImage);
        // 一方のマスクがもう一方のマスクに占める割合を計算
        //double ratio = (double) pixelsInMask1 / pixelsInMask2;
        //myTextView.setText("Variable Value: " + ratio);





        // 画像を表示
        return resultImage;



    }
}