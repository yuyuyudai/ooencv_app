package com.example.youtube_opencv;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.opencv.core.Scalar;

public class SettingActivity extends AppCompatActivity {

    private Scalar dr_lowerBound;

    private double slope_of_predict_line;

    private double Sizethreshold_S_M;
    private double Sizethreshold_M_L;

    private double weightPredictionFactor;

    private EditText lowerBoundHueInput;
    private EditText lowerBoundSaturationInput;
    private EditText lowerBoundValueInput;


    // SeekBarの設定
    private SeekBar sizethresholdSMSeekBar;
    private SeekBar sizethresholdMLSeekBar;
    private TextView sizethresholdSMValue;
    private TextView sizethresholdMLValue;

    private SeekBar weightSeekBar;
    private TextView weightValue;

    private EditText slopeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // EditTextのIDを取得
        lowerBoundHueInput = findViewById(R.id.lower_bound_input_hue);
        lowerBoundSaturationInput = findViewById(R.id.lower_bound_input_saturation);
        lowerBoundValueInput = findViewById(R.id.lower_bound_input_value);
        slopeInput = findViewById(R.id.slope_input);

        // SeekBarのIDを取得
        sizethresholdSMSeekBar = findViewById(R.id.sizethreshold_s_m_seekbar);
        sizethresholdMLSeekBar = findViewById(R.id.sizethreshold_m_l_seekbar);
        sizethresholdSMValue = findViewById(R.id.sizethreshold_s_m_value);
        sizethresholdMLValue = findViewById(R.id.sizethreshold_m_l_value);
        weightSeekBar= findViewById(R.id.weight_seekbar);
        weightValue = findViewById(R.id.weight_value);


        //sizeseekbar
        float initialDisplayValue_SM = 3.0f;
        float initialDisplayValue_ML = 5.0f;
        float initialDisplayValue_Weight = 3.0f;

        int initialProgress_SM = (int)(initialDisplayValue_SM * 10);
        int initialProgress_ML = (int)(initialDisplayValue_ML*10);
        int initialProgress_Weight = (int)(initialDisplayValue_Weight*10);


//        //weightseekbar
//        float initialDisplay_weight = 3.0f;
//        int initialProgress_weight = (int)(initialDisplay_weight*10);

        // SharedPreferencesからデータを読み込む
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        float savedHue = sharedPref.getFloat("dr_lowerBound_1", 0);  // デフォルト値は 0
        float savedSaturation = sharedPref.getFloat("dr_lowerBound_2", 120);
        float savedValue = sharedPref.getFloat("dr_lowerBound_3", 78);
        float savedSlope = sharedPref.getFloat("slope_of_predict_line", 0.0199f);
        float defaultThresholdSM = sharedPref.getFloat("Sizethreshold_S_M", 3.0f);
        float defaultThresholdML = sharedPref.getFloat("Sizethreshold_M_L", 5.0f);
        float defaultWeight = sharedPref.getFloat("weightPredictionFactor", 3.0f);


        // 保存した値をUIコンポーネントに表示
        lowerBoundHueInput.setText(String.valueOf(savedHue));
        lowerBoundSaturationInput.setText(String.valueOf(savedSaturation));
        lowerBoundValueInput.setText(String.valueOf(savedValue));
        slopeInput.setText(String.valueOf(savedSlope));
        sizethresholdSMSeekBar.setProgress(initialProgress_SM);
        sizethresholdMLSeekBar.setProgress(initialProgress_ML);
        sizethresholdSMValue.setText(String.format("%.1f", defaultThresholdSM));
        sizethresholdMLValue.setText(String.format("%.1f", defaultThresholdML));
        weightSeekBar.setProgress(initialProgress_Weight);
        weightValue.setText(String.format("%.1f", defaultWeight));

        // 画像リソースをセット
//        ImageView strawberrySmall = findViewById(R.id.strawberry_small_image);
//        ImageView strawberryMedium = findViewById(R.id.strawberry_medium_image);
//        ImageView strawberryLarge = findViewById(R.id.strawberry_large_image);
//        strawberrySmall.setImageResource(R.drawable.strawberry_small);
//        strawberryMedium.setImageResource(R.drawable.strawberry_medium);
//        strawberryLarge.setImageResource(R.drawable.strawberry_large);


        // lowerBoundDetailsのセクションの設定
        final LinearLayout lowerBoundDetails = findViewById(R.id.lower_bound_details);
        TextView lowerBoundLabel = findViewById(R.id.lower_bound_label);

        // predict_weight_detailsセクションの設定
        final LinearLayout predictWeightDetails = findViewById(R.id.predict_weight_details);
        TextView predictWeightLabel = findViewById(R.id.predict_weight_label);

        // slopeセクションの設定
        final LinearLayout slopeDetails = findViewById(R.id.slope_details);
        TextView slopeLabel = findViewById(R.id.slope_label);

        // sizeセクションのIDを取得
        final LinearLayout sizeThresholdDetails = findViewById(R.id.size_threshold_details);
        TextView sizeLabel = findViewById(R.id.size_threshold_title);


        // 初期は詳細部分を隠す設定
        lowerBoundDetails.setVisibility(View.GONE);
        predictWeightDetails.setVisibility(View.GONE);
        slopeDetails.setVisibility(View.GONE);

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

        // Predict Sizeラベルのクリックで詳細を表示/非表示
        predictWeightLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (predictWeightDetails.getVisibility() == View.GONE) {
                    predictWeightDetails.setVisibility(View.VISIBLE);
                } else {
                    predictWeightDetails.setVisibility(View.GONE);
                }
            }
        });

        // Slopeラベルのクリックで詳細を表示/非表示
        slopeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slopeDetails.getVisibility() == View.GONE) {
                    slopeDetails.setVisibility(View.VISIBLE);
                } else {
                    slopeDetails.setVisibility(View.GONE);
                }
            }
        });

        sizeLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sizeThresholdDetails.getVisibility() == View.GONE) {
                    sizeThresholdDetails.setVisibility(View.VISIBLE);
                } else {
                    sizeThresholdDetails.setVisibility(View.GONE);
                }
            }
        });


        // SeekBarのリスナー設定
        sizethresholdSMSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0.1倍して0～10の範囲の値に変換
                float displayedValue = progress / 10.0f;
                sizethresholdSMValue.setText(String.format("%.1f", displayedValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sizethresholdMLSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0.1倍して0～10の範囲の値に変換
                float displayedValue = progress / 10.0f;
                sizethresholdMLValue.setText(String.format("%.1f", displayedValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        // weightSeekBarのリスナー設定
        weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 0.1倍して0～10の範囲の値に変換
                float displayedValue = progress / 10.0f;
                weightValue.setText(String.format("%.1f", displayedValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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

        // 表面積と熟度予測
        slope_of_predict_line = Double.parseDouble(slopeInput.getText().toString());

        // SeekBarの値を閾値として取得
        Sizethreshold_S_M = sizethresholdSMSeekBar.getProgress()/10.0f;
        Sizethreshold_M_L= sizethresholdMLSeekBar.getProgress()/10.0f;
        weightPredictionFactor = weightSeekBar.getProgress()/10.0f;

        // SharedPreferencesに保存
        SharedPreferences sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear(); // すべての保存されたデータを削除
        editor.putFloat("dr_lowerBound_1", (float) hue);
        editor.putFloat("dr_lowerBound_2", (float) saturation);
        editor.putFloat("dr_lowerBound_3", (float) value);
        editor.putFloat("slope_of_predict_line", (float) slope_of_predict_line);
        editor.putFloat("Sizethreshold_S_M", (float) Sizethreshold_S_M);
        editor.putFloat("Sizethreshold_M_L", (float) Sizethreshold_M_L);
        editor.putFloat("weightPredictionFactor", (float) weightPredictionFactor);


        editor.apply();


        Toast.makeText(this, "設定が保存されました", Toast.LENGTH_SHORT).show();
    }
}