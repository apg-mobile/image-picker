package com.apg.library.imagepicker.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.apg.library.imagepicker.AlphaImagePicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlphaImagePicker.startPickImage(this, 19, false);
    }
}
