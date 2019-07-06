package com.example.seekbar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.seekbar.view.SeekBarView;

public class SeekBarActivity extends AppCompatActivity {
    private SeekBarView seekBarView;
    private TextView textView;

    public static void start(Context context) {
        context.startActivity(new Intent(context, SeekBarActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_bar);
        seekBarView = findViewById(R.id.seek_bar_view);
        textView = findViewById(R.id.tv_seek_bar_show);
        seekBarView.setOnSeekBarUpdateListener(new SeekBarView.SeekBarUpdateListener() {
            @Override
            public void onUpdateStart(int left, int right) {
                textView.setText("=====start======" + "left: " + left + " right: " + right);
            }

            @Override
            public void onUpdate(int left, int right) {
                textView.setText("=====update======" + "left: " + left + " right: " + right);
            }

            @Override
            public void onUpdateEnd(int left, int right) {
                textView.setText("=====end======" + "left: " + left + " right: " + right);
            }
        });
    }
}
