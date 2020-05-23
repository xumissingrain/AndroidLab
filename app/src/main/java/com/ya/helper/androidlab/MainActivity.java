package com.ya.helper.androidlab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ya.helper.util.LimitRequester;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TEST";
    private Button btn_test_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_test_request = findViewById(R.id.btn_test_request);
        btn_test_request.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_test_request: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LimitRequester requester = new LimitRequester(new Handler(Looper.getMainLooper()), 1000);
                        final int[] count = {1};
                        for (int i = 0; i < 10; i++) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            requester.postLimit(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "执行第" + count[0]++ + "次");
                                }
                            });
                        }
                    }
                }).start();
            }
            break;
            default:
                break;
        }
    }
}
