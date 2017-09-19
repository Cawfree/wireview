package io.github.cawfree;

import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WireView w = (WireView)this.findViewById(R.id.wv_main);

        WireView.Wire f = new WireView.Wire(new Point(0, 0), new Point(200, 200), 0xFF00FF00);

        w.getWires().add(f);
//        w.getWires().add(new WireView.Wire(new Point(0, 100), new Point(100, 200), 0xFFFF0000));
//        w.getWires().add(new WireView.Wire(new Point(100, 200), new Point(0, 100), 0xFFFF0000));
//        w.getWires().add(new WireView.Wire(new Point(100, 100), new Point(0, 200), 0xFFFF0000));
        w.getWires().add(new WireView.Wire(new Point(100, 100), new Point(0, 0), 0xFFFF0000));

//        Random rnd = new Random();
//
//        for(int i = 0; i < 50; i++) {
//
//            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//
//            w.getWires().add(new WireView.Wire(new Point((int)(500*Math.random()), (int)(500*Math.random())), new Point((int)(500*Math.random()), (int)(500*Math.random())), color));
//        }
    }
}
