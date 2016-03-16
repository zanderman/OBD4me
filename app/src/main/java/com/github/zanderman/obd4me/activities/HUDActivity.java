package com.github.zanderman.obd4me.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd4me.R;



/**
 * Class:
 *      HUDActivity
 *
 * Description:
 *      ...
 *
 * Author:
 *      Alexander DeRieux
 */
public class HUDActivity extends AppCompatActivity {

    /**
     * Public members.
     */
    OBDAdapter device;
    TextView tempTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hud);

        /**
         * Initialize UI objects.
         */
        tempTextView = (TextView) findViewById(R.id.tempTextView);

        // Get passed OBD adapter object.
        getIntent().getSerializableExtra("OBDAdapter");

        // print device name.
        tempTextView.setText(device.name);
    }
}
