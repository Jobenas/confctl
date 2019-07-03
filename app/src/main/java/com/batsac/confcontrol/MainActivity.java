package com.batsac.confcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    String user;
    String pwd;
    String ipAddress;

    String callIp = "192.168.0.6";

    int connected = 0;

    public String sessionId;
    public String acCSRFToken;

    private int mInterval = 3000;
    private Handler mHandler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        grabSettings();

        Intent intent = getIntent();

        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            user = getIntent().getStringExtra("user");
            pwd = getIntent().getStringExtra("pwd");
            ipAddress = getIntent().getStringExtra("ipAddress");
            sessionId = getIntent().getStringExtra("sessionId");
            acCSRFToken = getIntent().getStringExtra("acCSRFToken");
            connected = parseInt(getIntent().getStringExtra("connected"));

//            connected = 1;
        }
        else
        {
            try {
                connectToVC();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        final ImageButton onButton = findViewById(R.id.onButton);
        final ImageButton presentationButton = findViewById(R.id.presentationButton);
        final ImageButton offButton = findViewById(R.id.offButton);
        final ImageButton confControlButton = findViewById(R.id.confControlButton);
        final ImageButton acceptCallButton = findViewById(R.id.acceptCallButton);
//        final ImageButton declineCallButton = findViewById(R.id.declineCallButton);

        mHandler = new Handler();

        startRepeatingTask();

        presentationButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        presentationButton.setBackgroundResource(R.drawable.b5p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        presentationButton.setBackgroundResource(R.drawable.b5);

                        if(connected > 0)
                        {
                            try {
                                emulateRemote(0, 8);
                                emulateRemote(2, 8);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Context context = getApplicationContext();
                            CharSequence text = "Requiere conectarse al dispositivo primero";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                        return true;
                }

                return false;
            }
        });

        offButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        offButton.setBackgroundResource(R.drawable.b2p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        offButton.setBackgroundResource(R.drawable.b2);
                        stopRepeatingTask();
                        if(connected > 0)
                        {
                            connected = 0;
                            try
                            {
                                startTermSleep();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Context context = getApplicationContext();
                            CharSequence text = "Requiere conectarse al dispositivo primero";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                        return true;
                }

                return false;
            }
        });

        confControlButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        confControlButton.setBackgroundResource(R.drawable.b6p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        confControlButton.setBackgroundResource(R.drawable.b6);

                        if(connected > 0)
                        {
                            Intent intent = new Intent(getBaseContext(), contactActivity.class);
                            intent.putExtra("user", user);
                            intent.putExtra("pwd", pwd);
                            intent.putExtra("ipAddress", ipAddress);
                            intent.putExtra("sessionId", sessionId);
                            intent.putExtra("acCSRFToken", acCSRFToken);
                            if(connected == 0)
                            {
                                intent.putExtra("connected", "0");
                            }
                            else if(connected == 1)
                            {
                                intent.putExtra("connected", "1");
                            }

                            startActivity(intent);
                        }
                        else
                        {
                            Context context = getApplicationContext();
                            CharSequence text = "Requiere conectarse al dispositivo primero";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }

                        return true;
                }
                return false;
            }
        });

        acceptCallButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        acceptCallButton.setBackgroundResource(R.drawable.b18p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        acceptCallButton.setBackgroundResource(R.drawable.b18);

                        Intent intent = new Intent(getBaseContext(), incomingCallCtrl.class);
                        intent.putExtra("user", user);
                        intent.putExtra("pwd", pwd);
                        intent.putExtra("ipAddress", ipAddress);
                        intent.putExtra("sessionId", sessionId);
                        intent.putExtra("acCSRFToken", acCSRFToken);
                        if(connected == 0)
                        {
                            intent.putExtra("connected", "0");
                        }
                        else if(connected == 1)
                        {
                            intent.putExtra("connected", "1");
                        }

                        startActivity(intent);

//                        if(connected > 0)
//                        {
//                            try {
//                                incomingCallProc(1);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        else
//                        {
//                            Intent intent = new Intent(getBaseContext(), incomingCallCtrl.class);
//                            intent.putExtra("user", user);
//                            intent.putExtra("pwd", pwd);
//                            intent.putExtra("ipAddress", ipAddress);
//                            intent.putExtra("sessionId", sessionId);
//                            intent.putExtra("acCSRFToken", acCSRFToken);
//                            if(connected == 0)
//                            {
//                                intent.putExtra("connected", "0");
//                            }
//                            else if(connected == 1)
//                            {
//                                intent.putExtra("connected", "1");
//                            }
//
//                            startActivity(intent);
//                        }

                        return true;
                }

                return false;
            }
        });

//        declineCallButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                switch (motionEvent.getAction())
//                {
//                    case MotionEvent.ACTION_DOWN:
//                        declineCallButton.setBackgroundResource(R.drawable.b4p);
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        declineCallButton.setBackgroundResource(R.drawable.b4);
//
//                        try {
//                            incomingCallProc(0);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        return true;
//                }
//
//                return false;
//            }
//        });

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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopRepeatingTask();
    }


    Runnable mGetMailboxData  = new Runnable() {
        @Override
        public void run() {
            try
            {
                try
                {
                    if (connected > 0)
                    {
                        getMailBoxData();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finally
            {
                mHandler.postDelayed(mGetMailboxData, mInterval);
            }
        }
    };

    void startRepeatingTask()
    {
        mGetMailboxData.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mGetMailboxData);
    }

    void grabSettings()
    {
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
                    lineCounter += 1;
                }

                if(lineCounter == 0)
                {
                    user = "";
                    pwd = "";
                    ipAddress = "";
                }
                else if(lineCounter == 1)
                {
                    pwd = "";
                    ipAddress = "";
                }
                else if(lineCounter == 2)
                {
                    ipAddress = "";
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToVC() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(null, new byte[0]);


        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_RequestSessionIDAPI")
                .post(requestBody)
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "7f91e7a0-5dd6-4be6-b436-78ac2b271800,948272ef-2dc8-4610-83af-cb249f8de2d5")
                .addHeader("Host", ipAddress)
                .addHeader("cookie", "SessionID=sa4bjbP94j0af8X0zruzTOzOa9m0uaj")
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("got some response");

                final String myResponse = response.body().string();
                final List<String> responseCookies = response.headers("Set-Cookie");

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            System.out.println(myResponse);

                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "No se pudo conectar al dispositivo";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else {
                                String dataString = json.getString("data");

                                JSONObject dataJson = new JSONObject(dataString);

                                sessionId = dataJson.getString("acSessionId");
                                if (sessionId.equals(""))
                                {
                                    String cookieLongText = responseCookies.get(0);
                                    String[] stringSplit = cookieLongText.split(";");
                                    sessionId = stringSplit[0].split("=")[1];
                                }

                                System.out.println("Session ID: " + sessionId);
                            }

                            try{
                                requestCertificate();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void requestCertificate() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        System.out.println("this is the sesion id: " + sessionId);

//        String loginString = "{\n\t\"user\": \"" + user + "\",\n\t\"password\": \"" + pwd + "\"\n}";
//        Context context = getApplicationContext();
//        int duration = Toast.LENGTH_SHORT;
//
//        Toast toast = Toast.makeText(context, loginString, duration);
//        toast.show();


        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\n\t\"user\": \"admin\",\n\t\"password\": \"Huawei123\"\n}");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"user\": \"" + user + "\",\n\t\"password\": \"" + pwd + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_RequestCertificate")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Content-Type", "application/json")
//                .addHeader("Cookie", "SessionID=sn8zO48HODny481fbyyWGG54e9DqyXi")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "44762c3b-db49-4d5d-ab14-8be92a0b42b1,ac05f67d-ee2f-45ab-a1ac-6b807d4000e7")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "46")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("got some response");

                final String myResponse = response.body().string();

                System.out.print(myResponse);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al autenticar el dispositivo";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else
                            {
                                String dataString = json.getString("data");

                                JSONObject dataJson = new JSONObject(dataString);

                                acCSRFToken = dataJson.getString("acCSRFToken");

                                System.out.println("CSRF Token: " + acCSRFToken);

                                changeSessionId();
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void changeSessionId() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        System.out.println("this is the sesion id: " + sessionId);

        RequestBody requestBody = RequestBody.create(null, new byte[0]);

        Request request = new Request.Builder()
                .url("http://" + ipAddress +"/action.cgi?ActionID=WEB_ChangeSessionID")
                .post(requestBody)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "7df13e86-0391-4b0e-ae8b-69a6dbdcd6a4,23545ae4-92f4-4b0c-bcbd-857917cdafde")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("got some response");

                final String myResponse = response.body().string();
                final List<String> responseCookies = response.headers("Set-Cookie");

                System.out.println(responseCookies);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al cambiar ID de sesión";
                            }
                            else
                            {
                                String cookieLongText = responseCookies.get(0);
                                String[] stringSplit = cookieLongText.split(";");
                                String newSessionId = stringSplit[0].split("=")[1];

                                System.out.println(cookieLongText);

                                sessionId = newSessionId;

                                toastText = "Conexion al equipo completa";

                                connected = 1;
                            }

                            Context context = getApplicationContext();
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, toastText, duration);
                            toast.show();
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void getMailBoxData() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\": \"" + acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http:/" + ipAddress + "/action.cgi?ActionID=WEB_GetMailBoxDataAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "44762c3b-db49-4d5d-ab14-8be92a0b42b1,ac05f67d-ee2f-45ab-a1ac-6b807d4000e7")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "46")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("got some response");

                final String myResponse = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al obtener datos del equipo";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

//                                Toast toast = Toast.makeText(context, toastText, duration);
//                                toast.show();
                            }
                            else
                            {
//                                System.out.println(myResponse);
                                String a = "1";
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void emulateRemote(int keyState, int keyCode) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"keyState\":" + keyState +
                ",\n\t\"keyCode\":" + keyCode + ",\n\t" +
                "\"acCSRFToken\":\"" + acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_EmuRemoteKeyAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "c7613bae-2920-4285-a537-5c3efc1e3ac8,73cce5d7-dea2-44da-93a7-20603ac5826b")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "81")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("got some response");

                final String myResponse = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al intentar iniciar el modo presentación";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else
                            {
//                                System.out.println(myResponse);
                                String a = "1";
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void incomingCallProc(final int action) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"ucValue\":" + action + ",\n\t" +
                "\"ucMediaType\":0,\n\t\"ucSiteHandle\":1,\n\t" +
                "\"acCSRFToken\":\"" +  acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_IncomingCallProcAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "a197c472-eaf4-40c2-8b8e-885a048bda34,01a6ad88-5cf5-4aa9-addb-655d1a587399")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "103")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("got some response");

                final String myResponse = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al intentar desconectar el equipo";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else
                            {

                                if(action == 1)
                                {
                                    Intent intent = new Intent(getBaseContext(), incomingCallCtrl.class);
                                    intent.putExtra("user", user);
                                    intent.putExtra("pwd", pwd);
                                    intent.putExtra("ipAddress", ipAddress);
                                    intent.putExtra("sessionId", sessionId);
                                    intent.putExtra("acCSRFToken", acCSRFToken);
                                    if(connected == 0)
                                    {
                                        intent.putExtra("connected", "0");
                                    }
                                    else if(connected == 1)
                                    {
                                        intent.putExtra("connected", "1");
                                    }

                                    startActivity(intent);
                                }

                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void startTermSleep() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\": \"" + acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress +"/action.cgi?ActionID=WEB_StartTermSleepAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "e449a3a9-2470-4311-a425-981e723d5975,cf569ba5-8698-43b9-96df-2b8ef9485106")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "53")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("got some response");

                final String myResponse = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al intentar desconectar el equipo";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else
                            {
                                logoutAPI();
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void logoutAPI() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\": \"" + acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_LogOutAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "e449a3a9-2470-4311-a425-981e723d5975,cf569ba5-8698-43b9-96df-2b8ef9485106")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "53")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                System.out.println("got some response");

                final String myResponse = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al intentar salir de sesiónn";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else
                            {
//                                System.out.println(myResponse);
                                toastText = "Desconexión al equipo completa";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();

                                Intent intent = new Intent(getBaseContext(), welcomeMsg.class);
                                startActivity(intent);
                            }


                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
