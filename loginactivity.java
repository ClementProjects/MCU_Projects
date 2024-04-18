package com.example.bigproject3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class loginactivity extends Activity {

    private EditText editText1;
    private EditText editText2;
    private Button button1;
    private Button buttonRegister;
    private CheckBox checkBoxRememberAccount;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private DatabaseHelper3 databaseHelper;
    private TextView recordTextView1;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login Activity");

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);
        button1 = (Button) findViewById(R.id.button1);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);
        checkBoxRememberAccount = (CheckBox) findViewById(R.id.checkBoxRememberAccount);
        recordTextView1 = (TextView) findViewById(R.id.recordTextView1);
        databaseHelper = new DatabaseHelper3(this);

        // Retrieve saved username and password from SharedPreferences
        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = preferences.getString(USERNAME_KEY, "");
        String savedPassword = preferences.getString(PASSWORD_KEY, "");
        editText1.setText(savedUsername);
        editText2.setText(savedPassword);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editText1.getText().toString();
                String password = editText2.getText().toString();

                if (isValidLogin(username, password)) {
                    // Save the entered username and password to SharedPreferences
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(USERNAME_KEY, username);
                    editor.putString(PASSWORD_KEY, password);
                    editor.apply();

                    // Check if the username and password match
                    if (databaseHelper.checkCredentials(username, password)) {
                        // Proceed to BluetoothActivity
                        Intent intent = new Intent(loginactivity.this, BluetoothActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(loginactivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editText1.getText().toString();
                String password = editText2.getText().toString();

                // Check if username is root or administrator
                if (username.equals("root") || username.startsWith("Administrator")) {
                    // Proceed to RegisterActivity for root or administrator user
                    Intent intent = new Intent(loginactivity.this, RegisterActivity.class);
                   
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    // Show permission denied message for regular user
                    Toast.makeText(loginactivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        
          
    
        // Check if username is remembered
        if (preferences.contains(USERNAME_KEY)) {
            String rememberedUsername = preferences.getString(USERNAME_KEY, "");
            editText1.setText(rememberedUsername);
            checkBoxRememberAccount.setChecked(true);
        }

        // Display initial username from the database
        FirstData();
    }

    public boolean isValidLogin(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }

    private void FirstData() {
        String firstUsername = databaseHelper.getFirstUsername();
        String displayText = (firstUsername != null) ? firstUsername : "No username found";
        recordTextView1.setText("Initial username: " + displayText);
    }
}
