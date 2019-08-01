package com.batsac.confcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Integer.parseInt;

public class presentModeActivity extends AppCompatActivity {

    String user;
    String pwd;
    String ipAddress;

    String devIp;

    int connected = 0;

    public String sessionId;
    public String acCSRFToken;

    private int mInterval = 3000;
    private Handler mHandler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present_mode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = getIntent().getStringExtra("user");
        pwd = getIntent().getStringExtra("pwd");
        ipAddress = getIntent().getStringExtra("ipAddress");
        sessionId = getIntent().getStringExtra("sessionId");
        acCSRFToken = getIntent().getStringExtra("acCSRFToken");
        connected = parseInt(getIntent().getStringExtra("connected"));
        devIp = getIntent().getStringExtra("devIp");

        final ImageButton presentationButton = findViewById(R.id.presentationButton);
        final ImageButton returnContactButton = findViewById(R.id.backButton);

        presentationButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        presentationButton.setImageResource(R.drawable.contentsend_pressed);
                        return true;
                    case MotionEvent.ACTION_UP:
                        presentationButton.setImageResource(R.drawable.contentsend_normal);

                        if(connected > 0)
                        {
                            try {
                                startPresentation();

                                Intent intent = new Intent(getBaseContext(), callControl.class);
                                intent.putExtra("user", user);
                                intent.putExtra("pwd", pwd);
                                intent.putExtra("ipAddress", ipAddress);
                                intent.putExtra("sessionId", sessionId);
                                intent.putExtra("acCSRFToken", acCSRFToken);
                                intent.putExtra("type", "conference");
                                intent.putExtra("presentation", "true");
                                intent.putExtra("connected", "1");
                                intent.putExtra("devIp", devIp);

                                startActivity(intent);

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

        returnContactButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        returnContactButton.setImageResource(R.drawable.b14p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        returnContactButton.setImageResource(R.drawable.b14);

                        try {
                            startTermSleep();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

//                        stopRepeatingTask();
//
//                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
//
//                        intent.putExtra("user", user);
//                        intent.putExtra("pwd", pwd);
//                        intent.putExtra("ipAddress", ipAddress);
//                        intent.putExtra("sessionId", sessionId);
//                        intent.putExtra("acCSRFToken", acCSRFToken);
//                        intent.putExtra("devIp", devIp);
//                        if(connected == 0)
//                        {
//                            intent.putExtra("connected", "0");
//                        }
//                        else if(connected == 1)
//                        {
//                            intent.putExtra("connected", "1");
//                        }
//
//                        startActivity(intent);

                        return true;
                }

                return false;
            }
        });

        mHandler = new Handler();

        startRepeatingTask();
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

                presentModeActivity.this.runOnUiThread(new Runnable() {
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

    void startPresentation() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\":\"" +
                acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_StartSendAuxStreamAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "6c18ff8a-96f0-40e5-a087-2ddb8d2d2e59,60325971-64fb-4b61-9fd5-a92d2641a927")
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

                System.out.println("Response for starting presentation mode: " + myResponse);

                presentModeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Necesita conectar una fuente de video";

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



                presentModeActivity.this.runOnUiThread(new Runnable() {
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

                presentModeActivity.this.runOnUiThread(new Runnable() {
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

                presentModeActivity.this.runOnUiThread(new Runnable() {
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

                                stopRepeatingTask();
                                connected = 0;

                                sendSamsungToggleCmd();

                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                intent.putExtra("user", user);
                                intent.putExtra("pwd", pwd);
                                intent.putExtra("ipAddress", ipAddress);
                                intent.putExtra("sessionId", sessionId);
                                intent.putExtra("acCSRFToken", acCSRFToken);
                                intent.putExtra("connected", "0");
                                intent.putExtra("devIp", devIp);

                                startActivity(intent);
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

    void sendSamsungToggleCmd() throws IOException
    {
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url("http://" + devIp + "/sendCmdSamsung")
                .get()
                .addHeader("User-Agent", "PostmanRuntime/7.15.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "01abcca8-681a-4aea-89ca-495078357eaa,949b85be-5518-4d83-856f-3084c2267655")
                .addHeader("Host", devIp)
                .addHeader("cookie", "SessionID=s81i01X9410q0eXjjWWi8CKmXyG1mDS")
                .addHeader("accept-encoding", "gzip, deflate")
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

                System.out.println(myResponse);

                presentModeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        System.out.println("sent tv command");

                    }
                });
            }
        });
    }
}