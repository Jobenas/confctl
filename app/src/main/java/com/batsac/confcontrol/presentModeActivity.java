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
import android.widget.TextView;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

import static java.lang.Integer.parseInt;

public class presentModeActivity extends AppCompatActivity {

    String user;
    String pwd;
    String ipAddress;

    String devIp;
    String room;

    public String settingsPwd;

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
        room = getIntent().getStringExtra("room");

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

                        stopRepeatingTask();

                        Intent intent = new Intent(getBaseContext(), MainActivity.class);

                        intent.putExtra("user", user);
                        intent.putExtra("pwd", pwd);
                        intent.putExtra("ipAddress", ipAddress);
                        intent.putExtra("sessionId", sessionId);
                        intent.putExtra("acCSRFToken", acCSRFToken);
                        intent.putExtra("devIp", devIp);
                        intent.putExtra("connected", "1");
                        intent.putExtra("room", room);

                        startActivity(intent);

                        return true;
                }

                return false;
            }
        });

        mHandler = new Handler();

        startRepeatingTask();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        System.out.println("Got in onResume");

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
            devIp = getIntent().getStringExtra("devIp");
            room = getIntent().getStringExtra("room");
        }
        else
        {
            grabSettings();

            try {
                connectToVC();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onStop()
    {
        super.onStop();

        System.out.println("Got in onStop");

//        try {
//            logoutAPI();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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

    private static String bodyToString(final Request request)
    {
        try
        {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                        TextView roomEdit = findViewById(R.id.textview1);
                        String fullRoomString = roomEdit.getText().toString();
                        fullRoomString = fullRoomString + " " + room;
                        roomEdit.setText(fullRoomString);
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
                }
                else if(lineCounter == 1)
                {
                    pwd = "";
                    ipAddress = "";
                    devIp = "";
                    settingsPwd = "0410";
                }
                else if(lineCounter == 2)
                {
                    ipAddress = "";
                    devIp = "";
                    settingsPwd = "0410";
                }
                else if(lineCounter == 3)
                {
                    devIp = "";
                    settingsPwd = "0410";
                }
                else if(lineCounter == 4)
                {
                    settingsPwd = "0410";
                }

                System.out.println(settingsPwd);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void connectToVC() throws IOException
    {

        System.out.println("Starting connection procedure");

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

                presentModeActivity.this.runOnUiThread(new Runnable() {
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

        System.out.println("credentials = {\n\t\"user\": \"" + user + "\",\n\t\"password\": \"" + pwd + "\"\n}");

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"user\": \"" + user + "\",\n\t\"password\": \"" + pwd + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_RequestCertificate")
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

        System.out.println(request.headers());
        System.out.println("Body:");
        System.out.println(bodyToString(request));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("got some response from request certificate");

                final String myResponse = response.body().string();

                System.out.print(myResponse);

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
                                System.out.println("got this: " + myResponse);
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

                presentModeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            {
                                String cookieLongText = responseCookies.get(0);
                                String[] stringSplit = cookieLongText.split(";");
                                String newSessionId = stringSplit[0].split("=")[1];

                                System.out.println(cookieLongText);

                                sessionId = newSessionId;

                                toastText = "Conexion al equipo completa";

                                connected = 1;

                                mHandler = new Handler();

                                startRepeatingTask();
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

//                                Toast toast = Toast.makeText(context, toastText, duration);
//                                toast.show();
                                System.out.println(toastText);
                            }
                            else
                            {
                                toastText = "Desconexión al equipo completa";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

//                                Toast toast = Toast.makeText(context, toastText, duration);
//                                toast.show();
                                System.out.println(toastText);

                                stopRepeatingTask();
                                connected = 0;
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