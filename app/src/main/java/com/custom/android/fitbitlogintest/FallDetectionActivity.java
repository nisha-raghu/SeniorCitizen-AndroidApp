package com.custom.android.fitbitlogintest;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FallDetectionActivity extends Service implements SensorEventListener {
    private DecimalFormat df = new DecimalFormat("#.###");
    private String boundary_key;
    private String phoneNumber_key;
    private String notificationMethod_key;

    private TextView last_x_textView;
    private TextView last_y_textView;
    private TextView last_z_textView;
    private TextView x_textView;
    private TextView y_textView;
    private TextView z_textView;
    private TextView current_change_amount_textView;
    private TextView current_boundary_textView;

    private Sensor sensor;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;

    private double[] last_gravity = new double[3];
    private double[] gravity = new double[3];
    private boolean firstChange = true;
    private String phoneNumber;
    private String restoredText;
    private int warningBoundary = 50;
    private double changeAmount = 0;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String userID;
    private static final String TAG = "FallDetection";
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        //setContentView(R.layout.activity_main);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        Log.d(TAG, "USERID!!: " + userID);

        boundary_key = getString(R.string.key_boundary);

        // phoneNumber_key = getString(R.string.key_phone_number);
        notificationMethod_key = "phone";
        //notificationMethod_key = getString(R.string.key_notification_method);
        //  initialUI();
        //initialSensor();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "50"));

        SharedPreferences prefs = getSharedPreferences("FirebaseDB", MODE_PRIVATE);
        restoredText = prefs.getString("firebasephone", null);
        restoredText = "+16693775625";
        Log.d(TAG, "restoredText: " + restoredText);
        if (restoredText != null) {
            String name = prefs.getString("firebasephone", "No phone defined");//"No name defined" is the default value.
            Log.d(TAG, "name in restoredText: " + restoredText);
        }
        phoneNumber_key = restoredText;

        Log.d(TAG, "Phone  value: " + phoneNumber_key);

        if (restoredText.equals(""))
            showSetPhoneNumberWarning();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        return Service.START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        last_gravity[0] = gravity[0];
        last_gravity[1] = gravity[1];
        last_gravity[2] = gravity[2];

        gravity[0] = event.values[0];
        gravity[1] = event.values[1];
        gravity[2] = event.values[2];

        double changeAmount1 = Math.pow((gravity[0]), 2) +
                Math.pow((gravity[1]), 2) +
                Math.pow((gravity[2]), 2);

        changeAmount = Math.pow((gravity[0] - last_gravity[0]), 2) +
                Math.pow((gravity[1] - last_gravity[1]), 2) +
                Math.pow((gravity[2] - last_gravity[2]), 2);

        //updateSensorView();
        //Toast.makeText(getApplicationContext(), "va:"+String.valueOf(changeAmount), Toast.LENGTH_SHORT).show();
        String notificationMethod;
        warningBoundary = Integer.parseInt(sharedPreferences.getString(boundary_key, "50"));
        if (!firstChange && changeAmount >= warningBoundary) {
            Toast.makeText(getApplicationContext(), "Fall Detected", Toast.LENGTH_SHORT).show();
            // phoneNumber = sharedPreferences.getString("firebasephone", "");
            notificationMethod = sharedPreferences.getString(notificationMethod_key, "SMS");
            Log.d(TAG, "Notify type : " + notificationMethod);
            Log.d(TAG, "Inside SMS: " + restoredText);
            restoredText = "+16693775626";
            if (notificationMethod.equals("SMS"))
                sendSMS(restoredText);
            else if (notificationMethod.equals("phone"))
                makePhoneCall(restoredText);


        }
        firstChange = false;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void showSetPhoneNumberWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.set_phone_name_warning))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(((Dialog) dialog).getContext(), SettingsActivity.class));
                        dialog.cancel();
                    }
                }).show();
    }

    public void sendSMS(String phoneNumber) {
        /*try {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra("address", phoneNumber);
            sendIntent.putExtra("sms_body", "I fell. Please help me.");
            sendIntent.setType("vnd.android-dir/mms-sms");
            startService(sendIntent);
            } catch (Exception e){
            e.printStackTrace();
        }
*/

        String message="Assistance required! Your patient fell!! ";
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        //PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,new Intent(this, SmsSentReciever.class), 0);
        //PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(this, SmsDeliveredReceiver.class), 0);
        try {
            SmsManager sms = SmsManager.getDefault();
            //          ArrayList<String> mSMSMessage = sms.divideMessage(message);
//            Toast.makeText(getBaseContext(), (CharSequence) mSMSMessage,Toast.LENGTH_SHORT).show();
            // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "COming: " );
            stopService(new Intent(FallDetectionActivity.this,Main3Activity.class));

        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(getBaseContext(), "SMS sending failed...",Toast.LENGTH_SHORT).show();
        }


    }


    public void makePhoneCall(String phoneNumber) {


        //  startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber)));
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)).putExtra("sms_body", "default content"));
    }
}

