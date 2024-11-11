package com.example.youtube_opencv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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
    public TextView targetsize;

    public TextView Size;
    public TextView Weight;
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

    // いちごの二値化画像に対していちごを囲む矩形領域を見つけて描画するメソッド
    public void BoundingBox_strawberry(Mat image, MatOfPoint contour) {
        // 輪郭を囲む矩形領域を取得
        Rect rect = Imgproc.boundingRect(contour);
        // 矩形を描画
        Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 0), 5);
    }
    public void BoundingBox_marker_correction(Mat image, MatOfPoint contour) {
        // 輪郭を囲む矩形領域を取得
        Rect rect = Imgproc.boundingRect(contour);
        // 矩形を描画
        Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 255, 255), 2);
    }
    public void BoundingBox_marker(Mat image, MatOfPoint contour) {
        // 輪郭を囲む矩形領域を取得
        Rect rect = Imgproc.boundingRect(contour);
        // 矩形を描画
        Imgproc.rectangle(image, rect.tl(), rect.br(), new Scalar(0, 0, 255), 5);
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

        pixelCountTextView = (TextView)findViewById(R.id.pixelsInMask1str);//熟度のid
        predictTextView = (TextView)findViewById(R.id.predict);//予測時間のid
        targetsize = (TextView)findViewById(R.id.targetsize);
        Size = (TextView)findViewById(R.id.Size);
        Weight = (TextView)findViewById(R.id.Weight);
        coloredText_overripe = findViewById(R.id.color_box_overripe_text);
        coloredBox_harf = findViewById(R.id.color_box_harf);
        coloredBox_ripe = findViewById(R.id.color_box_ripe);
        introduct = findViewById(R.id.introduction_text);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        //requesting previewsize option (maxWidth > maxHeight)
        mOpenCvCameraView.setMaxFrameSize(960, 720);

        //ボタンを押した処理
        findViewById(R.id.seni_button01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        // @+id/seni_button01_right のクリックリスナーを設定
        Button settingsButton = findViewById(R.id.seni_button01_right);
        settingsButton.setOnClickListener(v -> {
            // Intentを使ってSettingActivityに遷移
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });


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

    private static final int SUBSAMPLING_FACTOR=5;
    private int frameCounter=0;
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {


        frameCounter++;
        if (frameCounter % SUBSAMPLING_FACTOR != 0) {
            return null; // フレームを処理せずに返す
        }

        Mat inputImage = inputFrame.rgba();  // inputFrame に元の画像が含まれていると仮定

        Log.i(TAG, "元の画像の幅: " + inputImage.width());
        Log.i(TAG, "元の画像の高さ: " + inputImage.height());
        Mat hsvImage_input = new Mat();//input
        Mat median_input = new Mat();
        Mat maskRed = new Mat();//input
        Mat mask_maker = new Mat();//input
        Mat maskGreen = new Mat();
        Mat maskStrawberry = new Mat();


        //inputimageでの処理
        Imgproc.medianBlur(inputImage, median_input, 1);

        // RGBからHSVに変換
        //inputimage
        Imgproc.cvtColor(median_input, hsvImage_input, Imgproc.COLOR_RGB2HSV);


        //補正確認用コード
        // 画面中央の中心座標を取得
        int centerX = inputImage.cols() / 2;
        int centerY = inputImage.rows() / 2;

        double[] centerHSVValueArrayBefore = hsvImage_input.get(centerY, centerX);
        Scalar centerHSVValueBefore = new Scalar(centerHSVValueArrayBefore);
        Log.d("MarkerHSV", "補正前の中心のHSV値: " + centerHSVValueBefore.toString());




//
//        //-------------------補正用マーカー取得---------------
//        Mat maskMarker = new Mat();
//        //青色マーカー抽出
//        Scalar lowerBlue = new Scalar(100, 150, 0);
//        Scalar upperBlue = new Scalar(140, 255, 255);
//        Core.inRange(hsvImage_input, lowerBlue, upperBlue, maskMarker);
//        // 輪郭抽出
//        List<MatOfPoint> contours_correction = new ArrayList<>();
//        Mat hierarchy_correction = new Mat();
//        Imgproc.findContours(maskMarker, contours_correction, hierarchy_correction, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//        // 最大の輪郭を見つける（円形度が0.9以上のものを対象）
//        double maxArea = 0;
//        MatOfPoint largestContour = null;
//        for (MatOfPoint contour : contours_correction) {
//            double area = Imgproc.contourArea(contour);
//            double circularity = calculateCircularity(contour); // 円形度を計算
//            if (area > maxArea && circularity >= 0.9) {
//                maxArea = area;
//                largestContour = contour;
//            }
//        }
//        // 最大の輪郭の中心座標を計算
//        Point center_correction = new Point();
//        if (largestContour != null) {
//            //中心座標の取得
//            Rect boundingRect = Imgproc.boundingRect(largestContour);
//            center_correction.x = boundingRect.x + boundingRect.width / 2;
//            center_correction.y = boundingRect.y + boundingRect.height / 2;
//
//            // マーカーの矩形領域を描画
//            // バウンディングボックスを一回り大きくする
//            int increase = 10; // 10ピクセルずつ増やす
//            int x = Math.max(boundingRect.x - increase, 0);
//            int y = Math.max(boundingRect.y - increase, 0);
//            int width = boundingRect.width + 2 * increase;
//            int height = boundingRect.height + 2 * increase;
//            // 画像の境界を越えないように調整
//            width = Math.min(width, inputImage.cols() - x);
//            height = Math.min(height, inputImage.rows() - y);
//            // 拡大されたバウンディングボックス
//            Rect expandedBoundingRect = new Rect(x, y, width, height);
//            // マーカーの矩形領域を描画
//            Imgproc.rectangle(inputImage, expandedBoundingRect.tl(), expandedBoundingRect.br(), new Scalar(0, 255, 255), 2);
//
//            // マーカーの中心点のHSV値を取得
//            Mat centerHSV = new Mat();
//            Imgproc.cvtColor(inputImage, centerHSV, Imgproc.COLOR_RGB2HSV);
//            Scalar centerHSVValue = new Scalar(centerHSV.get((int) center_correction.y, (int) center_correction.x));
//            centerHSV.release();
////            Log.d("MarkerHSV", "中心のHSV値: " + centerHSVValue.toString());
//
//            // 基準HSV値（青マーカーの中心座標のHSV値）を設定
//            Scalar baseHSV = new Scalar(110, 255, 120); // 例: [H, S, V]
//            // HSV値の差分を計算（SとVの差分のみ）
//
//            double sDiff = centerHSVValue.val[1] - baseHSV.val[1];
//            double vDiff = centerHSVValue.val[2] - baseHSV.val[2];
//
//            Scalar correctionHSV = new Scalar(
//                    0, // Hは補正しない
//                    centerHSVValue.val[1] - baseHSV.val[1],
//                    centerHSVValue.val[2] - baseHSV.val[2]
//            );
//
//            // HSVの補正
//            for (int row = 0; row < hsvImage_input.rows(); row++) {
//                for (int col = 0; col < hsvImage_input.cols(); col++) {
//                    double[] hsvValue = hsvImage_input.get(row, col);
//                    // Hはそのまま、SとVだけ補正を適用
//
//                    // Sの補正
//                    if (sDiff > 0) {
//                        hsvValue[1] -= Math.abs(sDiff);
//                    } else {
//                        hsvValue[1] += Math.abs(sDiff);
//                    }
//                    //Vの補正
//                    if (vDiff > 0){
//                        hsvValue[2] -= correctionHSV.val[2];
//                    }
//                    else {
//                        hsvValue[2] += correctionHSV.val[2];
//                    }
//                    // 範囲を保持するために値をクランプする
//                    hsvValue[1] = Math.max(0, Math.min(255, hsvValue[1]));
//                    hsvValue[2] = Math.max(0, Math.min(255, hsvValue[2]));
//                    hsvImage_input.put(row, col, hsvValue);
//                }
//            }
//
//            double[] centerHSVValuesAfter = hsvImage_input.get((int)center_correction.y, (int)center_correction.x);
//            Scalar centerHSVValueAfter = new Scalar(centerHSVValuesAfter);
////            Log.d("MarkerHSV", "補正後の中心のHSV値: " + centerHSVValueAfter.toString());
//
//
//        }
//        // 補正後の中心のHSV値を取得
//
//        double[] centerHSVValueArrayAfter = hsvImage_input.get(centerY, centerX);
//        Scalar centerHSVValueAfter = new Scalar(centerHSVValueArrayAfter);
//        Log.d("MarkerHSV", "補正後の中心のHSV値: " + centerHSVValueAfter.toString());
//
////        マーカー補正終了





        // 赤色の抽出(Hueが0付近)
        Mat maskRed_0 = new Mat();
        //input

        //イチゴ用パラメータ
        Scalar dr_lowerBound = new Scalar(0,120,78);//本物(0, 127, 108)　　レプリカ(0,120,78)
        Scalar dr_upperBound = new Scalar(13, 255, 255);//本物(15, 251,255)　レプリカ(13, 255, 255)

        //inputimage
        Core.inRange(hsvImage_input, dr_lowerBound, dr_upperBound, maskRed_0);


        // 赤色の抽出(Hueが179付近)
        Mat maskRed_180 = new Mat();

        //イチゴ用パラメータ
        Scalar high_dr_lowerBound = new Scalar(160,50,24);//本物(177,127,108)　　レプリカ(172,120,34)
        Scalar high_dr_upperBound = new Scalar(179,255,255);//本物(179,251,255)　　レプリカ(179,255,255)

        //inputimage
        Core.inRange(hsvImage_input, high_dr_lowerBound, high_dr_upperBound, maskRed_180);


        //maskRed_0とmaskRed_180を合わせてmaskRedを作成
        //inputimage
        Core.bitwise_or(maskRed_0, maskRed_180,maskRed);


        //マーカー用パラメータ
        Scalar marker_lower = new Scalar(100,150,0);
        Scalar marker_higher = new Scalar(140,255,255);
        //inputimage
        Core.inRange(hsvImage_input,marker_lower, marker_higher,mask_maker);

        Mat maskWhite_st = new Mat();
        Scalar wh_st_lowerBound = new Scalar(0, 50, 50);//本物(20, 11, 150)　　レプリカ(20, 29, 103)
        Scalar wh_st_upperBound = new Scalar(30, 255, 255);//本物(40, 199, 255)　レプリカ(60, 137, 255)
        Core.inRange(hsvImage_input, wh_st_lowerBound , wh_st_upperBound, maskWhite_st);

        // いちごの緑
        Mat maskGreen_st = new Mat();
        Scalar gr_st_lowerBound = new Scalar(20, 29, 103);//本物(20, 11, 150)　　レプリカ(20, 29, 103)
        Scalar gr_st_upperBound = new Scalar(30, 255, 255);//本物(40, 199, 255)　レプリカ(60, 137, 255)
        Core.inRange(hsvImage_input, gr_st_lowerBound , gr_st_upperBound, maskGreen_st);
        //いちごの緑（境界）
        Mat maskGreen_border = new Mat();
        Scalar gr_border_lowerBound = new Scalar(14,120,78);//本物(15, 100, 150)　　レプリカ(14,254,103)
        Scalar gr_border_upperBound = new Scalar(20,255,255);//本物(20, 251,255)　　レプリカ(20,255,255)
        Core.inRange(hsvImage_input, gr_border_lowerBound, gr_border_upperBound, maskGreen_border);
        //maskGreen_stとmaskGreen_borderを合わせる

        Core.bitwise_or(maskGreen_st, maskGreen_border,maskGreen);
        //white用
        Core.bitwise_or(maskWhite_st, maskGreen,maskGreen);

        //maskRedとmaskGreenをあわせてmaskStrawberryを作成
        Core.bitwise_or(maskRed, maskGreen, maskStrawberry);

        //輪郭を抽出し、中央から一番近い領域のみを残す-------------
        // 輪郭を検出
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(maskStrawberry, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // 画像の中心点を計算
        Point center = new Point(maskStrawberry.cols() / 2.0, maskStrawberry.rows() / 2.0);

        // 中心から最も近い輪郭を見つける
        double minDistance = Double.MAX_VALUE;
        MatOfPoint closestContour = null;

        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            Point contourCenter = new Point(boundingRect.x + boundingRect.width / 2.0, boundingRect.y + boundingRect.height / 2.0);
            double distance = Imgproc.pointPolygonTest(new MatOfPoint2f(contour.toArray()), center, true);

            if (Math.abs(distance) < minDistance) {
                minDistance = Math.abs(distance);
                closestContour = contour;
            }
        }
        // 最も近い輪郭のみを残す新しいマスクを作成
        Mat mask = Mat.zeros(maskStrawberry.size(), CvType.CV_8UC1);
        if (closestContour != null) {
            List<MatOfPoint> closestContourList = new ArrayList<>();
            closestContourList.add(closestContour);
            Imgproc.drawContours(mask, closestContourList, -1, new Scalar(255), Core.FILLED);
        }

        // 最も近い輪郭のみを残した画像
        Mat maskStrawberry_find = new Mat();
        Mat maskRed_find = new Mat();
        Core.bitwise_and(maskStrawberry, mask, maskStrawberry_find);
        Core.bitwise_and(maskRed, mask, maskRed_find);
        //maskStrawberrryからmaskStrawberry_findを作成

        //---------------------------------------



        // モルフォロジー変換
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(maskStrawberry_find, maskStrawberry_find, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(maskRed_find, maskRed_find, Imgproc.MORPH_OPEN, kernel);

        // 輪郭抽出とフィルタリング
        List<MatOfPoint> contoursStrawberry = new ArrayList<>();
        List<MatOfPoint> contoursRed = new ArrayList<>();
        //マーカー領域計算用のリスト
        List<MatOfPoint> contours_mark = new ArrayList<>();

        //イチゴ全体
        Imgproc.findContours(maskStrawberry_find, contoursStrawberry, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //イチゴの赤の部分
        Imgproc.findContours(maskRed_find, contoursRed, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //マーカー領域
        Imgproc.findContours(mask_maker, contours_mark, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //イチゴ全体
        List<MatOfPoint> filteredContoursStrawberry = new ArrayList<>();
        //イチゴの赤の部分
        List<MatOfPoint> filteredContoursRed = new ArrayList<>();
        //マーカー
        List<MatOfPoint> filteredContoursMark = new ArrayList<>();


        boolean flag=false;

        //マーカー検出部
        for (MatOfPoint contour : contours_mark) {
            //マーカーの円形度計算
            double mark_cercle = calculateCircularity(contour);
            // mark_cercleの値をログに出力
            Log.d("MyApp", "円形度: " + mark_cercle);
            double mark_area = Imgproc.contourArea(contour);

            if (mark_area > 2000) {

                if(mark_cercle > 0.9){
                    //mark_area,mark_cercleの条件クリアしたら
                    filteredContoursMark.add(contour);
                    BoundingBox_marker(inputImage,contour);
                }


            }


        }

        //イチゴ全体を検出
        for (MatOfPoint contour : contoursStrawberry) {
            double area = Imgproc.contourArea(contour);
            double cercle = calculateCircularity(contour);
            if (area > 3000) {
                //イチゴ全体のマスクに円形度を入れると熟度が100を超えてしまう
                //二値化画像の抽出の際に画面の中央に一番近いものを選択するプログラムを書く
                //if(cercle > 0.6){
                filteredContoursStrawberry.add(contour);
                BoundingBox_strawberry(inputImage,contour);
                flag=true;

                //}

            }

        }



        //条件クリアするイチゴが見つからなかったとき
        double all = 0.0;
        double red = 0.0;
        double marker = 0.0;//マーカーのピクセル数
//        if(!flag){
//            return inputImage;
//        }







        //いちごの赤の部分検出
        for (MatOfPoint contour : contoursRed) {
            double area = Imgproc.contourArea(contour);

            if (area > 3000) {
                filteredContoursRed.add(contour);
            }
        }

        // フィルタリングされた輪郭を描画
        Mat resultStrawberry = Mat.zeros(maskStrawberry_find.size(), CvType.CV_8U);
        Mat resultRed = Mat.zeros(maskRed_find.size(), CvType.CV_8U);
        Mat resultMark = Mat.zeros(mask_maker.size(), CvType.CV_8U);


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
        double marker_size=3.14;
        double predict_size=0.0;

        //makerが検出されない場合（面積が0の場合）
        if (marker > 2000) {
            Log.d("test", "red: " + all+ " blue: "+marker);
            //1cm^2あたりのピクセル数
            double base_pixel = marker/marker_size;
            Log.d("base_pixel", "サイズ: " + base_pixel);
            //予測サイズ
            predict_size = all/base_pixel;
            Log.d("predictsize", "予想値: " + predict_size);

        }


        BigDecimal round = new BigDecimal(predict_size);
        BigDecimal predict_size_round = round.setScale(2, BigDecimal.ROUND_HALF_UP);
        // predict_size_roundをdoubleに変換してからfinal変数に格納する
        final double finalPredict_Size = predict_size_round.doubleValue();







        // 一方のマスクがもう一方のマスクに占める割合を計算
        double ratio =red/all*100;



        BigDecimal bd = new BigDecimal(ratio);
        BigDecimal ripe_ratio = bd.setScale(2,BigDecimal.ROUND_HALF_UP);

        double slope_of_predict_line = 0.0199;
        double predict_time_80 = 80.0 / slope_of_predict_line;

        double predict_time = ratio / slope_of_predict_line;

        int predict_time_dif =(int)(predict_time_80-predict_time);

        int predict_time_hour = predict_time_dif / 60;

        int predict_time_minutes = predict_time_dif % 60;


        //初期表示値
        int initialDisplayValue_SM = 30;
        int initialDisplayValue_ML = 50;
        int initialDisplayValue_weight = 30;

        // プリファレンスから取得（スケール済み整数値として保存）
        //サイズ（S,M,L)
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        int Sizethreshold_S_M = sharedPref.getInt("Sizethreshold_S_M", initialDisplayValue_SM);
        int Sizethreshold_M_L = sharedPref.getInt("Sizethreshold_M_L", initialDisplayValue_ML);
        //weight
        int weightPredictionFactor = sharedPref.getInt("weightPredictionFactor", initialDisplayValue_weight);

        // 必要な箇所で元のfloat値に戻す
        float smValue = Sizethreshold_S_M / 10.0f;
        float mlValue = Sizethreshold_M_L / 10.0f;
        float weightFactor = weightPredictionFactor / 10.0f;

        //イチゴの重さ推定のための変数
        final double weight = finalPredict_Size * weightFactor;
        String formattedWeight = String.format("%.2f", weight);



        runOnUiThread(new Runnable(){

            @Override
            public void run() {




                pixelCountTextView.setText(String.valueOf(ripe_ratio)+"%");


                if(predict_time_dif > 0 && ratio == 0){
                    predictTextView.setText(String.valueOf(""));
                    Size.setText(String.valueOf(""));
                    Weight.setText(String.valueOf(""));
                    targetsize.setText(String.valueOf(""));

                }
                else if (predict_time_dif > 0 && ratio != 0){
                    //予測時間表示
                    predictTextView.setText(String.valueOf(predict_time_hour)+"時間"+String.valueOf(predict_time_minutes)+"分後");

                    //サイズ表示（S,M,L）
                    if(finalPredict_Size < smValue){
                        Size.setText("S");
                    }
                    else if (smValue <= finalPredict_Size && finalPredict_Size <= mlValue ){
                        Size.setText("M");
                    }
                    else{
                        Size.setText("L");
                    }

                    //重さ表示
                    Weight.setText(String.valueOf(formattedWeight)+"g");


                    //表面積表示
                    String sizeText = String.valueOf(finalPredict_Size);
                    String baseText = "cm";
                    String superscriptText = "2";

// SpannableStringBuilderを使用してテキストを構築
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(sizeText);
                    builder.append(baseText);

// 上付き文字部分をSpannableで作成
                    SpannableString superscriptSpannable = new SpannableString(superscriptText);

// 相対サイズを指定して上付き文字を小さくする (0.6fはサイズを60%に)
                    superscriptSpannable.setSpan(new RelativeSizeSpan(0.6f), 0, superscriptText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 上付き文字にする
                    superscriptSpannable.setSpan(new SuperscriptSpan(), 0, superscriptText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 上付き文字をbuilderに追加
                    builder.append(superscriptSpannable);

// TextViewにセット
                    targetsize.setText(builder);
                }
                else{
                    predictTextView.setText("収穫可能");


                    //サイズ表示（S,M,L）
                    if(finalPredict_Size < smValue){
                        Size.setText("S");
                    }
                    else if (smValue <= finalPredict_Size && finalPredict_Size <= mlValue ){
                        Size.setText("M");
                    }
                    else{
                        Size.setText("L");
                    }

                    //重さ表示
                    Weight.setText(String.valueOf(formattedWeight)+"g");

                    //表面積表示
                    String sizeText = String.valueOf(finalPredict_Size);
                    String baseText = "cm";
                    String superscriptText = "2";

// SpannableStringBuilderを使用してテキストを構築
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(sizeText);
                    builder.append(baseText);

// 上付き文字部分をSpannableで作成
                    SpannableString superscriptSpannable = new SpannableString(superscriptText);

// 相対サイズを指定して上付き文字を小さくする (0.6fはサイズを60%に)
                    superscriptSpannable.setSpan(new RelativeSizeSpan(0.6f), 0, superscriptText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 上付き文字にする
                    superscriptSpannable.setSpan(new SuperscriptSpan(), 0, superscriptText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// 上付き文字をbuilderに追加
                    builder.append(superscriptSpannable);

// TextViewにセット
                    targetsize.setText(builder);
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
        return inputImage;
        //return resultStrawberry;
        //return maskGreen;
        //return maskWhite_st;
        //return mask_maker;


    }
}