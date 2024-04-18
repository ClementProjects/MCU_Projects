
package com.example.bigproject3;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private CheckBox checkBoxAdministrator;
    private Button buttonAdd;
    private Button buttonQuery;
    private Button buttonDelete;
    private Button buttonReturn;
    private DatabaseHelper3 databaseHelper;
    private String loggedInUserLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register Activity");

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        checkBoxAdministrator = (CheckBox) findViewById(R.id.checkBoxAdministrator);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        buttonQuery = (Button) findViewById(R.id.buttonQuery);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonReturn = (Button) findViewById(R.id.buttonReturn);
        databaseHelper = new DatabaseHelper3(this);
        loggedInUserLevel = getIntent().getStringExtra("username");

      

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                boolean isAdmin = checkBoxAdministrator.isChecked();

                if (isAdmin && loggedInUserLevel.equals("root")) {
                    if (databaseHelper.checkUsernameExist(username)) {
                        Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.insertUser(username, password, 1);
                        Toast.makeText(RegisterActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                } else if (isAdmin && loggedInUserLevel.equals("administrator")) {
                    Toast.makeText(RegisterActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                } else {
                    if (databaseHelper.checkUsernameExist(username)) {
                        Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.insertUser(username, password, 2);
                        Toast.makeText(RegisterActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                }
            }
        });

        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                if (databaseHelper.checkUsernameExist(username)) {
                    Toast.makeText(RegisterActivity.this, "User exists", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                boolean isAdmin = checkBoxAdministrator.isChecked();

                if (isAdmin && loggedInUserLevel.equals("root")) {
                    databaseHelper.deleteUser(username);
                    Toast.makeText(RegisterActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                } else if (isAdmin && loggedInUserLevel.equals("administrator")) {
                    if (loggedInUserLevel.equals("root") || loggedInUserLevel.equals("administrator")) {
                        Toast.makeText(RegisterActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.deleteUser(username);
                        Toast.makeText(RegisterActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to the LoginActivity
                Intent intent = new Intent(RegisterActivity.this, loginactivity.class);
                startActivity(intent);
            }
        });
    }
    private void clearFields() {
        editTextUsername.setText("");
        editTextPassword.setText("");
        checkBoxAdministrator.setChecked(false);
    }
}