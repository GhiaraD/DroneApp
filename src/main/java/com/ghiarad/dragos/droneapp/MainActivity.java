package com.ghiarad.dragos.droneapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{
    // Gravity rotational data
    private float gravity[];
    // Magnetic rotational data
    private float magnetic[]; //for magnetic rotational data
    private float accels[] = new float[3];
    private float mags[] = new float[3];
    private float gyros[] = new float[3];
    private float[] values = new float[3];
    int k=0;

    private float acc_sens = 8192.0f,gyro_sens=65.536f,dt=0.1f;

    // azimuth, pitch and roll
    private float azimuth,gazimuth,finalyaw;
    private float pitch,gpitch,finalpitch;
    private float roll,groll,finalroll;

    private TextView tv;
    private Button btn;
    private Sensor gyroS,accelS,magneticS;
    private SensorEventListener eventListener;
    private SensorManager sManager;

    TextView front,side,yawn,up;

    private final int REQ_CODE = 100;
    TextView textView,voice;
    ImageView speak;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        btn = (Button) findViewById(R.id.btn);
        tv.setText("Waitinig for start");

        speak = (ImageView) findViewById(R.id.speak);

        voice = (TextView) findViewById(R.id.voice);
        front = (TextView) findViewById(R.id.fataspate);
        side = (TextView) findViewById(R.id.stdr);
        yawn = (TextView) findViewById(R.id.yawn);
        up = (TextView) findViewById(R.id.susjos);

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                gyroS = sManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                accelS = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magneticS = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

                eventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {

                        switch (event.sensor.getType()) {
                            case Sensor.TYPE_MAGNETIC_FIELD:
                                mags = event.values.clone();
                                break;
                            case Sensor.TYPE_ACCELEROMETER:
                                accels = event.values.clone();
                                break;
                            case Sensor.TYPE_GYROSCOPE:
                                gyros = event.values.clone();
                                break;
                        }

                        if (mags != null && accels != null && gyroS != null) {

                            gazimuth = gyros[0]/ gyro_sens *dt;
                            gpitch = gyros[1]/ gyro_sens *dt ;
                            groll = gyros[2]/ gyro_sens *dt;

                            gravity = new float[9];
                            magnetic = new float[9];
                            SensorManager.getRotationMatrix(gravity, magnetic, accels, mags);
                            float[] outGravity = new float[9];
                            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X, SensorManager.AXIS_Z, outGravity);
                            SensorManager.getOrientation(outGravity, values);

                            azimuth = values[0] ;
                            pitch = values[1] ;
                            roll = values[2] ;

                            finalyaw = azimuth*57.2957795f;
                            finalpitch = (float)(gpitch*0.98+pitch*0.02)*57.2957795f*50;
                            finalroll = (float)(groll*0.98+roll*0.02)*57.2957795f*50;

                            tv.setText("roll " + finalroll + "\n"
                                    + "pitch " + finalpitch  + "\n"
                                    + "yaw " + finalyaw + "\n");

                            //*57.2957795f

                            if (finalyaw > 0f) { // anticlockwise
                                side.setText("Dreapta");
                            } else if (finalyaw < -80f) { // clockwise
                                side.setText("Stanga");
                            } else side.setText("Stam");

                            if (finalpitch > 45f) { // anticlockwise
                                yawn.setText("Rotire dreapta");
                            } else if (finalpitch < -45f) { // clockwise
                                yawn.setText("Stanga pe loc");
                            } else yawn.setText("Stam");

                            if (finalroll > -55f) { // anticlockwise
                                front.setText("Spate");
                            } else if (finalroll < -105f) { // clockwise
                                front.setText("Fata");
                            } else front.setText("Stam");
                        }

                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }

                };
                sManager.registerListener(eventListener, gyroS, SensorManager.SENSOR_DELAY_NORMAL);
                sManager.registerListener(eventListener, accelS, SensorManager.SENSOR_DELAY_NORMAL);
                sManager.registerListener(eventListener, magneticS, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voice.setText(result.get(0).toString());
                }
                break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(k==-1)
                    {up.setText("Stam");
                     k=0;
                    }
                    else
                    {
                        up.setText("Sus");
                        k=-1;
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    if(k==-1)
                    {up.setText("Stam");
                        k=0;
                    }
                    else
                    {
                        up.setText("Jos");
                        k=-1;
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

}