package com.example.seekbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        Button btSeekBar = findViewById(R.id.bt_skip_seek_bar);
        btSeekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBarActivity.start(MainActivity.this);
            }
        });
    }
}
