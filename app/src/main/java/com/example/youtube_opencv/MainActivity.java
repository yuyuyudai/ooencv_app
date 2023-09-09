package com.example.youtube_opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    static{
        if(OpenCVLoader.initDebug()){
            Log.d("Check","OpenCv configured successfully");
       }
        else{
            Log.d("Check","OpenCv doesn't configured successfully");
        }
    }
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
