package com.example.youtube_opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //ボタンを押した時
        findViewById(R.id.seni_button01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //遷移ロジック
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);

            }
        });



    }



}

