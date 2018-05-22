package com.custom.android.fitbitlogintest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by nisha on 4/2/2018.
 */

public class CaretakerActivity extends AppCompatActivity {

    private static final String TAG = "AddToDatabase";

    private Button mAddToDB;

    private EditText mNewName;
    private EditText mPhone;

    //add Firebase Database stuff
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private  String userID;
    private String phoneFirebase;
    ArrayList<String> array  = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_caretaker);
        //declare variables in oncreate
        mAddToDB = (Button) findViewById(R.id.buttonSaveCareTaker);
        mNewName = (EditText) findViewById(R.id.editTextCareTakerName);
        mPhone = (EditText) findViewById(R.id.editTextCarePhoneNumber);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(CaretakerActivity.this, "Successfully signed in with:" + user.getEmail(), Toast.LENGTH_SHORT).show();
                    //toastMessage("Successfully signed in with: " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    //Toast.makeText(CaretakerActivity.this, "Successfully signed out.", Toast.LENGTH_SHORT).show();
                }
                // ...
            }
        };

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object value = dataSnapshot.getValue();
                showData(dataSnapshot);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mAddToDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Attempting to add object to database.");
                String newName = mNewName.getText().toString();
                String phone =mPhone.getText().toString();
                if(!newName.equals("") && !phone.equals("")){
                    FirebaseUser user = mAuth.getCurrentUser();
                    String userID = user.getUid();
                    Toast.makeText(CaretakerActivity.this, "USERID: " + userID + " PHONE:"+ phone, Toast.LENGTH_SHORT).show();

                    myRef.child(userID).child("Caretaker").child("name").setValue(newName);
                    myRef.child(userID).child("Caretaker").child("phone").setValue(phone);
                    Toast.makeText(CaretakerActivity.this, "Adding " + newName + " to database...", Toast.LENGTH_SHORT).show();

                    mNewName.setText("");
                    mPhone.setText("");
                }
            }
        });
    }

    private void showData(DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            UserInformation uInfo = new UserInformation();

            // Log.d(TAG, "Inside showData: " + ds.getKey());
            if(ds.getKey().equals(userID)){
                Log.d(TAG, "Caretaker value: " + ds.child("Caretaker"));

                uInfo.setName(ds.child("Caretaker").getValue(UserInformation.class).getName()); //set the name
                uInfo.setPhone(ds.child("Caretaker").getValue(UserInformation.class).getPhone()); //set the phone_num

                //display all the information
                Log.d(TAG, "showData: name: " + uInfo.getName());
                Log.d(TAG, "showData: phone: " + uInfo.getPhone());

                array.add(uInfo.getName());
                array.add(uInfo.getPhone());
                phoneFirebase = uInfo.getPhone();
            }

        }

        SharedPreferences.Editor editor = getSharedPreferences("FirebaseDB", MODE_PRIVATE).edit();
        editor.putString("firebasephone", phoneFirebase);
        editor.apply();
        Log.d(TAG, "Caretaker phone: " + phoneFirebase);

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //add a toast to show when successfully signed in
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}