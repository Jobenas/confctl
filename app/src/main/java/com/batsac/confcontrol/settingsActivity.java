package com.batsac.confcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;

public class settingsActivity extends AppCompatActivity {

    String user;
    String pwd;
    String ipAddress;
    String devIp;
    String settingsPwd;
    String room;
    String tvOption;

    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final EditText userEdit = findViewById(R.id.userEdit);
        final EditText pwdEdit = findViewById(R.id.pwdEdit);
        final EditText ipEdit = findViewById(R.id.ipEdittext);
        final EditText devIpEdit = findViewById(R.id.devIpEdit);
        final EditText appPwd = findViewById(R.id.appPwd);
        final EditText roomEdit = findViewById(R.id.nombreSala);

        Button saveButton = findViewById(R.id.saveIPButton);
        Button backButton = findViewById(R.id.backButton);

        spinner = findViewById(R.id.tvSpinner);

        ArrayAdapter<CharSequence>  adapter = ArrayAdapter.createFromResource(this,
                R.array.tvOptions, android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

//        spinner.setOnItemClickListener(new CustomOnItemSelectedListener());

        Context context = this;
        File directory = context.getFilesDir();
        File file = new File(directory, "config.conf");

        if(file.exists())
        {
            System.out.println("File exists");
            try
            {
                FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                int lineCounter = 0;
                String line;
                while((line = reader.readLine()) != null)
                {
                    System.out.println("line number " + lineCounter + " value is: " + line);
                    if(lineCounter == 0)
                    {
                        user = line;
                    }
                    else if(lineCounter == 1)
                    {
                        pwd = line;
                    }
                    else if(lineCounter == 2)
                    {
                        ipAddress = line;
                    }
                    else if(lineCounter == 3)
                    {
                        devIp = line;
                    }
                    else if(lineCounter == 4)
                    {
                        settingsPwd = line;
                    }
                    else if(lineCounter == 5)
                    {
                        room = line;
                    }
                    else if(lineCounter == 6)
                    {
                        tvOption = line;
                    }
                    lineCounter += 1;
                }

                if(lineCounter == 0)
                {
                    user = "";
                    pwd = "";
                    ipAddress = "";
                    devIp = "";
                    settingsPwd = "0410";
                    room = "";
                    tvOption = "Samsung";

                }
                else if(lineCounter == 1)
                {
                    pwd = "";
                    ipAddress = "";
                    devIp = "";
                    settingsPwd = "0410";
                    room = "";
                    tvOption = "Samsung";
                }
                else if(lineCounter == 2)
                {
                    ipAddress = "";
                    devIp = "";
                    settingsPwd = "0410";
                    room = "";
                    tvOption = "Samsung";
                }
                else if(lineCounter == 3)
                {
                    devIp = "";
                    settingsPwd = "0410";
                    room = "";
                    tvOption = "Samsung";
                }
                else if(lineCounter == 4)
                {
                    settingsPwd = "0410";
                    room = "";
                    tvOption = "Samsung";
                }
                else if(lineCounter == 5)
                {
                    room = "";
                    tvOption = "Samsung";
                }
                else if(lineCounter == 6)
                {
                    tvOption = "Samsung";
                }

                userEdit.setText(user);
                pwdEdit.setText(pwd);
                ipEdit.setText(ipAddress);
                devIpEdit.setText(devIp);
                appPwd.setText(settingsPwd);
                roomEdit.setText(room);
                spinner.setSelection(adapter.getPosition(tvOption));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = userEdit.getText().toString();
                pwd = pwdEdit.getText().toString();
                ipAddress = ipEdit.getText().toString();
                devIp = devIpEdit.getText().toString();
                settingsPwd = appPwd.getText().toString();
                room = roomEdit.getText().toString();
                long spinnerValue= spinner.getSelectedItemId();

                if (spinnerValue == 0)
                {
                    tvOption = "Samsung";
                }
                else if(spinnerValue == 1)
                {
                    tvOption = "LG Grupo 1";
                }
                else if(spinnerValue == 2)
                {
                    tvOption = "LG Grupo 2";
                }

                String fileString = user + "\n" + pwd + "\n" + ipAddress + "\n" + devIp + "\n" +
                        settingsPwd + "\n" + room + "\n" + tvOption + "\n";

                System.out.println(fileString);

                try
                {
                    FileOutputStream outputStream = openFileOutput("config.conf", Context.MODE_PRIVATE);
                    outputStream.write(fileString.getBytes());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Context context = getApplicationContext();
                CharSequence text = "Configuración Guardada";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);

                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_main)
        {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, settingsActivity.class);
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}