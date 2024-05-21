package com.example.youtube_opencv;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
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
    public TextView predictTextView;
    public TextView coloredText_overripe;
    public TextView coloredBox_harf;
    public TextView coloredBox_ripe;
    public TextView introduct;

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

    // 円形度を計算するメソッド
    private double calculateCircularity(MatOfPoint contour) {
        // 輪郭の面積
        double area = Imgproc.contourArea(contour);
        // 輪郭の外接円
        Point center = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()), center, radius);
        // 外接円の面積
        double circleArea = Math.PI * radius[0] * radius[0];
        // 円形度を計算
        return area / circleArea;
    }


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



        //((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(1000);

        pixelCountTextView = (TextView)findViewById(R.id.pixelsInMask1str);
        predictTextView = (TextView)findViewById(R.id.predict);
        coloredText_overripe = findViewById(R.id.color_box_overripe_text);
        coloredBox_harf = findViewById(R.id.color_box_harf);
        coloredBox_ripe = findViewById(R.id.color_box_ripe);
        introduct = findViewById(R.id.introduction_text);

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
        Rect cropRect = new Rect(startX, startY-50, halfWidth,halfHeight );  // トリミングする領域を定義
        Size newSize = new Size(inputImage.width(),inputImage.height()); // 出力サイズを定義

        // 入力画像をトリミングおよびリサイズ
        Mat croppedResizedImage = new Mat();
        Mat croppedImage = new Mat(inputImage, cropRect);  // 画像をトリミング
        //リサイズせずにトリミング画像で画像処理を行ったほうが良い？
        Imgproc.resize(croppedImage, croppedResizedImage, newSize);  // トリミングした画像をリサイズ

        // これで croppedResizedImage 上でさらなる処理を実行できます

        Log.i(TAG, "元の画像の幅: " + inputImage.width());
        Log.i(TAG, "元の画像の高さ: " + inputImage.height());
        //Mat rgbaImage = inputFrame.rgba(); // RGB画像を取得
        Mat hsvImage_crop = new Mat(); // 空のHSV画像を作成//crop
        Mat hsvImage_input = new Mat();//input
        Mat median_crop = new Mat();
        Mat median_input = new Mat();
        Mat maskRed_crop = new Mat();//crop
        Mat mask_maker = new Mat();//input
        Mat maskGreen = new Mat();
        Mat maskStrawberry = new Mat();

        //croppedResizeImageの場合------
        //croppedResizeImageをメディアンフィルタをかける
        Imgproc.medianBlur(croppedResizedImage, median_crop, 1); // フィルタのカーネルサイズを調整可能
        //inputimageでの処理
        Imgproc.medianBlur(inputImage, median_input, 1);

        // RGBからHSVに変換
        //croppedResizeImage
        Imgproc.cvtColor(median_crop, hsvImage_crop, Imgproc.COLOR_RGB2HSV);
        //inputimage
        Imgproc.cvtColor(median_input, hsvImage_input, Imgproc.COLOR_RGB2HSV);



        // 赤色の抽出(Hueが0付近)
        //crop
        Mat maskRed_0_crop = new Mat();

        //イチゴ用パラメータ
        Scalar dr_lowerBound = new Scalar(0, 127, 108);//(0, 127, 108)
        Scalar dr_upperBound = new Scalar(15, 251,255);//(11, 251, 255)

        //cropimage
        Core.inRange(hsvImage_crop, dr_lowerBound, dr_upperBound, maskRed_0_crop);


        // 赤色の抽出(Hueが179付近)
        //crop
        Mat maskRed_180_crop = new Mat();

        //イチゴ用パラメータ
        Scalar high_dr_lowerBound = new Scalar(177,127,108);//(178,127,108
        Scalar high_dr_upperBound = new Scalar(179,251,255);//(179,251,255
        //cropimage
        Core.inRange(hsvImage_crop, high_dr_lowerBound, high_dr_upperBound, maskRed_180_crop);


        // いちごの緑
        Mat maskGreen_st = new Mat();
        Scalar gr_st_lowerBound = new Scalar(20, 11, 150);//(11, 11, 84)
        Scalar gr_st_upperBound = new Scalar(40, 199, 255);//(45, 199, 239)
        Core.inRange(hsvImage_crop, gr_st_lowerBound , gr_st_upperBound, maskGreen_st);
        //いちごの緑（境界）
        Mat maskGreen_border = new Mat();
        Scalar gr_border_lowerBound = new Scalar(15, 100, 150);//(11, 11, 84)
        Scalar gr_border_upperBound = new Scalar(20, 251,255);//(45, 199, 239)
        Core.inRange(hsvImage_crop, gr_border_lowerBound, gr_border_upperBound, maskGreen_border);
        //maskGreen_stとmaskGreen_borderを合わせる
        Core.bitwise_or(maskGreen_st, maskGreen_border,maskGreen);

        //maskRedとmaskGreenをあわせてmaskStrawberryを作成
        Core.bitwise_or(maskRed_crop, maskGreen, maskStrawberry);


        //マーカー用パラメータ
        Scalar lower_marker = new Scalar(100,150,0);//(178,127,108
        Scalar high_marker = new Scalar(140,255,255);//(179,251,255

        //inputimage
        Core.inRange(hsvImage_input, lower_marker, high_marker, mask_maker);
        //maskRed_0とmaskRed_180を合わせてmaskRedを作成
        //cropimage
        Core.bitwise_or(maskRed_0_crop, maskRed_180_crop,maskRed_crop);


        // モルフォロジー変換
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(maskStrawberry, maskStrawberry, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(maskRed_crop, maskRed_crop, Imgproc.MORPH_OPEN, kernel);

        // 輪郭抽出とフィルタリング
        List<MatOfPoint> contoursStrawberry = new ArrayList<>();
        List<MatOfPoint> contoursRed = new ArrayList<>();
        //マーカー領域計算用のリスト
        List<MatOfPoint> contours_mark = new ArrayList<>();

        //イチゴ全体
        Imgproc.findContours(maskStrawberry, contoursStrawberry, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //イチゴの赤の部分
        Imgproc.findContours(maskRed_crop, contoursRed, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //マーカー領域
        Imgproc.findContours(mask_maker, contours_mark, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //イチゴ全体
        List<MatOfPoint> filteredContoursStrawberry = new ArrayList<>();
        //イチゴの赤の部分
        List<MatOfPoint> filteredContoursRed = new ArrayList<>();
        //マーカー
        List<MatOfPoint> filteredContoursMark = new ArrayList<>();

        boolean flag=false;

        for (MatOfPoint contour : contoursStrawberry) {
            double area = Imgproc.contourArea(contour);

            if (area > 3000) {

                filteredContoursStrawberry.add(contour);
                flag=true;
            }

        }



        //条件クリアするイチゴが見つからなかったとき
        double all = 0.0;//イチゴ全体のピクセル数
        double red = 0.0;//赤い部分のピクセル数
        double marker = 0.0;//マーカーのピクセル数
        //画像内にイチゴらしきものあれば以下の処理。なければ処理をスキップ
//        if(!flag){
//            return inputImage;
//        }

        //マーカー検出部
        //inputimage上で円形度による認識（赤の領域内で）
        for (MatOfPoint contour : contours_mark) {
            //マーカーの円形度計算
            double mark_cercle = calculateCircularity(contour);
            // mark_cercleの値をログに出力
            Log.d("MyApp", "円形度: " + mark_cercle);
            double mark_area = Imgproc.contourArea(contour);

            if (mark_area > 1000) {

                if(mark_cercle > 0.8){
                    //mark_area,mark_cercleの条件クリアしたら
                    filteredContoursMark.add(contour);
                }


            }

        }





        //閾値以上のいちご領域抽出
        for (MatOfPoint contour : contoursRed) {
            double area = Imgproc.contourArea(contour);

            if (area > 3000) {
                filteredContoursRed.add(contour);
            }
        }

        // フィルタリングされた輪郭を描画
        Mat resultStrawberry = Mat.zeros(maskStrawberry.size(), CvType.CV_8U);
        Mat resultRed = Mat.zeros(maskRed_crop.size(), CvType.CV_8U);
        Mat resultMark = Mat.zeros(mask_maker.size(), CvType.CV_8U);

        //輪郭内を塗りつぶす
        Imgproc.drawContours(resultStrawberry, filteredContoursStrawberry, -1, new Scalar(255), Core.FILLED);
        Imgproc.drawContours(resultRed, filteredContoursRed, -1, new Scalar(255), Core.FILLED);
        Imgproc.drawContours(resultMark, filteredContoursMark, -1, new Scalar(255), Core.FILLED);

        //maskStrawberryのピクセル計算
        all = Core.countNonZero(resultStrawberry) + 0.1;
        //maskRedのピクセル計算
        red = Core.countNonZero(resultRed);
        //マーカーのピクセル計算
        marker = Core.countNonZero(resultMark);

        //マーカーのサイズ
        double marker_size=20.0;

        //makerが検出されない場合（面積が0の場合）
        if (marker > 1000) {
            //1cm^2あたりのピクセル数
            double base_pixel = marker/marker_size;
            Log.d("base_pixel", "サイズ: " + base_pixel);
            //予測サイズ
            double predict_size = all/base_pixel;
            Log.d("predictsize", "サイズ: " + predict_size);

        }




        // maskRedの二値化画像をもとのRGB画像と合成
        Mat redImage = new Mat();

        croppedResizedImage.copyTo(redImage,maskGreen );


        // 一方のマスクがもう一方のマスクに占める割合を計算
        double ratio =red/all*100;



        BigDecimal bd = new BigDecimal(ratio);
        BigDecimal ripe_ratio = bd.setScale(2,BigDecimal.ROUND_HALF_UP);


        double predict_time_80 = 80.0 / 0.0199;

        double predict_time = ratio / 0.0199;

        int predict_time_dif =(int)(predict_time_80-predict_time);

        int predict_time_hour = predict_time_dif / 60;

        int predict_time_minutes = predict_time_dif % 60;







        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                pixelCountTextView.setText(String.valueOf(ripe_ratio)+"%");


                if(predict_time_dif > 0 && ratio == 0){
                    predictTextView.setText(String.valueOf(""));

                }
                else if (predict_time_dif > 0 && ratio != 0){
                    predictTextView.setText(String.valueOf(predict_time_hour)+"時間"+String.valueOf(predict_time_minutes)+"分後");
                }
                else{
                    predictTextView.setText("収穫可能");
                }





                if (ratio < 30 ) {//熟度が40%未満ならば説明文を表示

                    introduct.setVisibility(View.VISIBLE);
                    coloredBox_harf.setVisibility(View.GONE);
                    coloredBox_ripe.setVisibility(View.GONE);
                    coloredText_overripe.setVisibility(View.GONE);
                } else if (ratio >= 30 && ratio < 60) {//40～65はharf_ripeを表示
                    introduct.setVisibility(View.GONE);
                    coloredBox_harf.setVisibility(View.VISIBLE);
                    coloredBox_ripe.setVisibility(View.GONE);
                    coloredText_overripe.setVisibility(View.GONE);
                } else if (ratio >= 60 && ratio < 80){//65～85はripeを表示
                    introduct.setVisibility(View.GONE);
                    coloredBox_harf.setVisibility(View.GONE);
                    coloredBox_ripe.setVisibility(View.VISIBLE);
                    coloredText_overripe.setVisibility(View.GONE);
                }
                else{//85以上はover_ripeを表示
                    introduct.setVisibility(View.GONE);
                    coloredBox_harf.setVisibility(View.GONE);
                    coloredBox_ripe.setVisibility(View.GONE);
                    coloredText_overripe.setVisibility(View.VISIBLE);
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                }
            }


        });


        //pixelCountTextView.setText(String.valueOf(a));
        //pixelCountTextView.setText(abc);







        // 画像を表示////
        //return inputImage;
        //return mask_maker;
        return resultStrawberry;


    }
}