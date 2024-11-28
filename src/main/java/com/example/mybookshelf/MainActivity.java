package com.example.mybookshelf;  // Replace with your actual package name

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Authenticator auth;

    public MainActivity() {
        this.auth = new Authenticator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the login_activity.xml
        setContentView(R.layout.login_activity);  // Referencing the XML file for the layout

        // Initialize the UI components by finding their IDs
        usernameEditText = findViewById(R.id.txfUser);
        passwordEditText = findViewById(R.id.txfPassword);
        loginButton = findViewById(R.id.btnLogin);

        // Set a listener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login attempt = new Login(usernameEditText.getText().toString(),  passwordEditText.getText().toString());

                if (auth.checkLogin(attempt)) {
                    if (attempt.getUser().isEmpty() || attempt.getPassword().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    } else {

                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        setContentView(R.layout.starting_page);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "User or Password false", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
