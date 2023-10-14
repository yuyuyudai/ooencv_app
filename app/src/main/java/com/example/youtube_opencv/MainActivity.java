package com.example.youtube_opencv;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";


    public TextView pixelCountTextView;
    public View coloredBox_harf;
    public View coloredBox_ripe;

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



        pixelCountTextView = (TextView)findViewById(R.id.pixelsInMask1str);
        coloredBox_harf = findViewById(R.id.color_box_harf);
        coloredBox_ripe = findViewById(R.id.color_box_ripe);

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
    private static int ratio = 0;

    private static final int SUBSAMPLING_FACTOR=5;
    private int frameCounter=0;
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

        frameCounter++;
        if (frameCounter % SUBSAMPLING_FACTOR != 0) {
            return null; // フレームを処理せずに返す
        }

        Mat inputImage = inputFrame.rgba();  // inputFrame に元の画像が含まれていると仮定

        //トリミング処理
        // トリミングおよびリサイズのパラメータを定義
        // 中心座標の計算
        int centerX = inputImage.width() / 2;
        int centerY = inputImage.height() / 2;

        // 1/2サイズの画像の切り出し
        int halfWidth = inputImage.width()/2;
        int halfHeight = inputImage.height() / 2;

        int startX = centerX - halfWidth / 2;
        int startY = centerY - halfHeight / 2;
        Rect cropRect = new Rect(startX, startY, halfWidth,halfHeight );  // トリミングする領域を定義
        Size newSize = new Size(inputImage.width(),inputImage.height()); // 出力サイズを定義

        // 入力画像をトリミングおよびリサイズ
        Mat croppedResizedImage = new Mat();
        //Mat inputImage = inputFrame.rgba();  // inputFrame に元の画像が含まれていると仮定
        Mat croppedImage = new Mat(inputImage, cropRect);  // 画像をトリミング
        Imgproc.resize(croppedImage, croppedResizedImage, newSize);  // トリミングした画像をリサイズ

        // これで croppedResizedImage 上でさらなる処理を実行できます

        Log.i(TAG, "元の画像の幅: " + inputImage.width());
        Log.i(TAG, "元の画像の高さ: " + inputImage.height());
        //Mat rgbaImage = inputFrame.rgba(); // RGB画像を取得
        Mat hsvImage = new Mat(); // 空のHSV画像を作成
        Mat hueChannel = new Mat(); // 色相チャンネル画像を格納
        Mat binaryImage = new Mat(); // 二値化画像を格納
        Mat combineImage = new Mat();

        // RGBからHSVに変換
        Imgproc.cvtColor(croppedResizedImage, hsvImage, Imgproc.COLOR_RGB2HSV);

        // 色相チャンネルを抽出
        List<Mat> channels = new ArrayList<>();
        Core.split(hsvImage, channels);
        hueChannel = channels.get(0);

        // 濃い赤色の抽出(Hueが0付近)
        Scalar dr_lowerBound = new Scalar(0, 50, 0);
        Scalar dr_upperBound = new Scalar(1, 255, 155);
        Core.inRange(hsvImage, dr_lowerBound, dr_upperBound, binaryImage);
        //Core.bitwise_or(binaryImage, combineImage, binaryImage);

        // 濃い赤色の抽出(Hueが179付近)
        Scalar high_dr_lowerBound = new Scalar(178,50,0);
        Scalar high_dr_upperBound = new Scalar(179,255,255);

        Core.inRange(hsvImage, high_dr_lowerBound, high_dr_upperBound, combineImage);
        Core.bitwise_or(binaryImage, combineImage, binaryImage);

        //赤の条件厳しく(大)
        double red = Core.countNonZero(binaryImage);


        // 赤色の抽出
        Scalar r_lowerBound = new Scalar(2, 50, 50);
        Scalar r_upperBound = new Scalar(25, 255, 255);
        Core.inRange(hsvImage, r_lowerBound, r_upperBound, combineImage);
        Core.bitwise_or(binaryImage, combineImage, binaryImage);

        /*//赤色の抽出（Hueが179付近）
        Scalar high_r_lowerBound = new Scalar(2, 50, 50);
        Scalar high_r_upperBound = new Scalar(25, 255, 255);
        Core.inRange(hsvImage, high_r_lowerBound, high_r_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);
*/
        //赤の条件厳しく(中)
        //double red = Core.countNonZero(binaryImage);


        // オレンジ色の抽出
        Scalar o_lowerBound = new Scalar(11, 0, 0);
        Scalar o_upperBound = new Scalar(25, 255, 255);
        Core.inRange(hsvImage, o_lowerBound, o_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);

        //赤の条件（ゆるく）
        //double red = Core.countNonZero(binaryImage);


        // 緑色の抽出
        Scalar gr_lowerBound = new Scalar(26, 30, 50);
        Scalar gr_upperBound = new Scalar(48, 255, 255);
        Core.inRange(hsvImage, gr_lowerBound, gr_upperBound, combineImage);

        Core.bitwise_or(binaryImage, combineImage, binaryImage);


        double all = Core.countNonZero(binaryImage) + 0.1;


        // 二値化画像をもとのRGB画像と合成
        Mat resultImage = new Mat();
        croppedResizedImage.copyTo(resultImage, binaryImage);

        //TextView text = (TextView) findViewById(R.id.pixelsInMask1str);

        // 一方のマスクがもう一方のマスクに占める割合を計算
        double ratio =red/all*100;

        BigDecimal bd = new BigDecimal(ratio);
        BigDecimal ripe_ratio = bd.setScale(2,BigDecimal.ROUND_HALF_UP);



        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                pixelCountTextView.setText(String.valueOf(ripe_ratio)+"%");

                if (ratio > 10) {
                    coloredBox_harf.setVisibility(View.VISIBLE); // 表示
                    coloredBox_ripe.setVisibility(View.GONE);
                } else {
                    coloredBox_harf.setVisibility(View.GONE); // 非表示
                    coloredBox_ripe.setVisibility(View.VISIBLE);
                }
            }


        });


        //pixelCountTextView.setText(String.valueOf(a));
        //pixelCountTextView.setText(abc);







        // 画像を表示////
        return inputImage;
        //return croppedResizedImage;


    }
}