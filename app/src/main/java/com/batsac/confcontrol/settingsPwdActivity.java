package com.batsac.confcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class settingsPwdActivity extends AppCompatActivity {

    String settingsPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_pwd);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        settingsPwd = getIntent().getStringExtra("settingsPwd");

        if (settingsPwd == null)
        {
            settingsPwd = "0410";
        }

        final EditText settingsPwdEdit = findViewById(R.id.settingsPwdEdit);
        Button inputPwdButton = findViewById(R.id.inputPwdButton);
        Button pwdBackButton = findViewById(R.id.pwdBackButton);

        inputPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputPwd = settingsPwdEdit.getText().toString();

                System.out.println("current password is " + settingsPwd);
                System.out.println("Input password is: " + inputPwd);

                if(inputPwd.equals(settingsPwd))
                {
                    Intent intent = new Intent(getBaseContext(), settingsActivity.class);
                    startActivity(intent);
                }
                else
                {
                    String toastText = "Usuario o Contraseña Equivocados";

                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, toastText, duration);
                    toast.show();
                }
            }
        });

        pwdBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
