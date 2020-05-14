package com.example.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity {

    // This logs the user in and pushes them to the dashboard activity.
    public void loggedIn(){
        Intent intent = new Intent(getApplicationContext(), LoggedIn.class);
        startActivity(intent);
    }

    // This gets rid of the keyboard if you click anywhere off of it.
    public void onClick(View view) {
        RelativeLayout backgroundRelativeLayout = (RelativeLayout) findViewById(R.id.backgroundRelativeLayout);
        ImageView logoImageView = (ImageView) findViewById(R.id.logoImageView);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if ((view.getId() == backgroundRelativeLayout.getId() || view.getId() == logoImageView.getId()) && inputMethodManager.isActive()){
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    // This signs a user up
    // It takes the input from the username and password input fields and tries to push that data onto the ParseServer.
    public void signUp(View view) {

        EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        if (usernameEditText.getText().toString().matches("") || passwordEditText.getText().toString().matches("")) {
            Toast.makeText(this, "A username and password are required", Toast.LENGTH_SHORT).show();
        } else {
            ParseUser user = new ParseUser();

            user.setUsername(usernameEditText.getText().toString());
            user.setPassword(getHash(createDigest(passwordEditText.getText().toString())));


            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.i("Signup", "Successful");
                        Toast.makeText(MainActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            user.saveInBackground();
        }
    }

    // This method runs when the login button is pressed
    // It takes the input from the username and password input fields and tries logging into the Parse Server
    public void login(View view) {
        EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        final String username = usernameEditText.getText().toString();
        final String password = getHash(createDigest(passwordEditText.getText().toString()));

        if (username.matches("") || password.matches("")) {
            Toast.makeText(this, "A username and password are required", Toast.LENGTH_SHORT).show();
        } else {
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        EditText usernameEditText = (EditText) findViewById(R.id.usernameEditText);
                        EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
                        usernameEditText.getText().clear();
                        passwordEditText.getText().clear();
                        loggedIn();
                    }
                }
            });
        }
    }

    // This method takes in a string of plaintext and converts it into ciphertext using SHA-256
    public byte[] createDigest(String plaintext){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(plaintext.getBytes());
            byte[] digest = md.digest();
            return digest;
        } catch (NoSuchAlgorithmException e) {
            Toast.makeText(MainActivity.this, "Error with Hashing. Try Again!", Toast.LENGTH_SHORT).show();
        }
        return new byte[-1];
    }

    // This takes in the byte array containing the ciphertext and returns it in it's proper form
    public String getHash(byte[] digest){

        if (digest.length == -1) {
            Toast.makeText(MainActivity.this, "Error with Hashing, Try Again!", Toast.LENGTH_SHORT).show();
        }
        StringBuffer hashString = new StringBuffer();

        for (int i = 0; i < digest.length; i++) {
            hashString.append(Integer.toHexString(0xFF & digest[i]));
        }

        return hashString.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        // This bit connects us to the database
        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("8b84a08f75e0b47cf96fc7adf14489088dd3b1cb")
                .clientKey("4b3c301e3594424a0ab7c4c89fe66e34e9815060")
                .server("http://3.17.14.88:80/parse")
                .build()
        );

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        // This checks to make sure the user doesn't need to log in again.
        if (ParseUser.getCurrentUser() != null) {
            loggedIn();
        }

        setContentView(R.layout.activity_main);
    }
}
