package com.example.youtube_opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Scalar;

public class SettingActivity extends AppCompatActivity {

    private Scalar dr_lowerBound;
    private double predict_size;
    private double slope_of_predict_line;

    private EditText lowerBoundHueInput;
    private EditText lowerBoundSaturationInput;
    private EditText lowerBoundValueInput;
    private EditText predictSizeInput;
    private EditText slopeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // EditTextのIDを取得
        lowerBoundHueInput = findViewById(R.id.lower_bound_input_hue);
        lowerBoundSaturationInput = findViewById(R.id.lower_bound_input_saturation);
        lowerBoundValueInput = findViewById(R.id.lower_bound_input_value);
        predictSizeInput = findViewById(R.id.predict_size_input);
        slopeInput = findViewById(R.id.slope_input);


        // SharedPreferencesからデータを読み込む
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        float savedHue = sharedPref.getFloat("dr_lowerBound_1", 0);  // デフォルト値は 0
        float savedSaturation = sharedPref.getFloat("dr_lowerBound_2", 120);
        float savedValue = sharedPref.getFloat("dr_lowerBound_3", 78);
        float savedPredictSize = sharedPref.getFloat("predict_size", 0.0f);
        float savedSlope = sharedPref.getFloat("slope_of_predict_line", 0.0199f);


        // 保存した値をEditTextに表示
        lowerBoundHueInput.setText(String.valueOf(savedHue));
        lowerBoundSaturationInput.setText(String.valueOf(savedSaturation));
        lowerBoundValueInput.setText(String.valueOf(savedValue));
        predictSizeInput.setText(String.valueOf(savedPredictSize));
        slopeInput.setText(String.valueOf(savedSlope));

        // 各セクションのレイアウトとラベルを取得
        final LinearLayout lowerBoundDetails = findViewById(R.id.lower_bound_details);
        TextView lowerBoundLabel = findViewById(R.id.lower_bound_label);

        // 初期は詳細部分を隠す設定
        lowerBoundDetails.setVisibility(View.GONE);

        // Lower Boundラベルのクリックで詳細を表示/非表示
        lowerBoundLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lowerBoundDetails.getVisibility() == View.GONE) {
                    lowerBoundDetails.setVisibility(View.VISIBLE);
                } else {
                    lowerBoundDetails.setVisibility(View.GONE);
                }
            }
        });


        // 設定画面で保存ボタンを押したときの処理
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // 戻るボタンの設定
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 前のアクティビティに戻る
                finish();
            }
        });

    }


    private void saveSettings() {
        // 入力された値を取得
        double hue = Double.parseDouble(lowerBoundHueInput.getText().toString());
        double saturation = Double.parseDouble(lowerBoundSaturationInput.getText().toString());
        double value = Double.parseDouble(lowerBoundValueInput.getText().toString());
        dr_lowerBound = new Scalar(hue, saturation, value);

        predict_size = Double.parseDouble(predictSizeInput.getText().toString());
        slope_of_predict_line = Double.parseDouble(slopeInput.getText().toString());

        // 値の確認のためにToast表示（デバッグ用）
//        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();

        // SharedPreferencesのインスタンスを取得
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // 各パラメータを保存
        editor.putFloat("dr_lowerBound_1", (float) hue);
        editor.putFloat("dr_lowerBound_2", (float) saturation);
        editor.putFloat("dr_lowerBound_3", (float) value);
        editor.putFloat("predict_size", (float) predict_size);
        editor.putFloat("slope_of_predict_line", (float) slope_of_predict_line);

        // 保存の確定
        editor.apply();

        // 値の確認のためにToast表示（デバッグ用）
        Toast.makeText(this, "設定が保存されました", Toast.LENGTH_SHORT).show();

    }







}