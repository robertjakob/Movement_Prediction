// This app distinguishes between walking and running activities of the last 10 seconds based on accelerometer sensor data


package com.c4dhi.robertjakob.walkingrunning;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Sensor accelerometer;
    SensorManager sm;
    TextView activityText;
    double accelerationX;
    double accelerationY;
    double accelerationZ;
    double absAcceleration;
    double rootMeanSquare;
    double x;
    double frequency;
    double calculation;
    List<Double> acclist = new ArrayList<Double>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Code to adjust the Accelerometer. SENSOR_DELAY_GAME returns accelerometer data with a
        // 20,000 microsecond delay
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        activityText = (TextView)findViewById(R.id.activityText);

        // method that runs the activity tracker
        activityTracker();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    private void activityTracker() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int listSize = acclist.size();

                // calculate the Root Mean Square of absolute acceleration data collected in the last 10 seconds
                for (int z = 0; z<listSize; z++){
                    x = x + Math.pow(acclist.get(z), 2);
                }
                rootMeanSquare = Math.sqrt(x);

                // calculate the Frequency of absolute acceleration data collected in the last 10 seconds
                frequency = calculate(listSize,acclist);

                //The following function calculates, if the current data set
                //of "rootMeanSquare and "frequency" lies above or below the decision boundary
                // (y = 21.055387 -0.655539 * X1 + 2.184702 * X2)
                // The decision boundary was calculated in Octave Gui by applying Logistic
                // regression on the basis of "Data.txt". The data in "Data.txt" was aggregated
                // using a LG Leon H340n (thanks to Bosch IoT lab for letting me use the phone
                // for my master thesis ;)). During data gathering I wore a pair of jeans and
                // had the phone in my right pocket. Besides gathered data of "rootMeanSquare and "frequency"
                // values, "Data.txt" also contains a classifier (1 = walking, 0 =running).
                calculation = 21.055387 -0.655539 * rootMeanSquare + 2.184702 * frequency;
                if (calculation < 0){
                    activityText.setText("You are now running");

                    //Make a notification sound each ten seconds as long as the user runs
                    //This should facilitate testing, because the screen cannot be read as long as
                    // the phone is in the pocket
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();

                } else {
                    activityText.setText("You are walking. Please put the phone in your right pocket and start running. A notification sound will appear every 10 seconds to indicate your running activity. The sound will disappear once you start walking again. (Note: Don't forget to adjust the volume of your speakers)");
                }

                // set Root mean square back to zero
                x = 0;

                // clear list containing accelerometer data
                acclist.clear();

                // set timer to 10 seconds
                handler.postDelayed(this, 10000);
            }


            //method calculating the frequency based on accList
            private double calculate(int sampleRate, List<Double> dataList) {
                int numSamples = dataList.size();
                double numCrossing = 0;
                for (int p = 0; p < numSamples-1; p++)
                {
                    if ((dataList.get(p) > 0 && dataList.get(p + 1) <= 0) ||
                            (dataList.get(p) < 0 && dataList.get(p + 1) >= 0))
                    {
                        numCrossing++;
                    }
                }
                double numSecondsRecorded = (double)numSamples/(double)sampleRate;
                double numCycles = numCrossing/2;
                double frequency = numCycles/numSecondsRecorded;
                return(double)frequency;
            }

        }, 10000);  //// start timer at 10 seconds
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        //get accelerometer data for the x, y and z axis
        accelerationX = event.values[0];
        accelerationY = event.values[1];
        accelerationZ = event.values[2];

        // absAcceleration diminishes differences in orientation and subtracts the acceleration
        // of gravity.
        absAcceleration = (Math.sqrt(Math.pow(accelerationX, 2) + Math.pow(accelerationY, 2) + Math.pow(accelerationZ, 2))) - 9.81;

        //Add current absAcceleration value to a accList. This is done every 0.02 seconds microseconds
        //since SENSOR_DELAY_GAME was applied
        acclist.add(absAcceleration);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }


}


