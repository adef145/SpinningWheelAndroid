package com.teslacode.spinningwheelandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.teslacode.spinningwheel.SpinningWheelView;

public class MainActivity extends AppCompatActivity {

    private SpinningWheelView wheelView;

    private Button rotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wheelView = (SpinningWheelView) findViewById(R.id.wheel);
        rotate = (Button) findViewById(R.id.rotate);

        wheelView.setItems(R.array.dummy);

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // max angle 50
                // duration 10 second
                // every 50 ms rander rotation
                wheelView.rotate(50, 10000, 50);
            }
        });
    }
}
