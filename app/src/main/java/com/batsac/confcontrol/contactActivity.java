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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class contactActivity extends AppCompatActivity {

    String user;
    String pwd;
    String ipAddress;

    String sessionId;
    String acCSRFToken;

    String callIp;

    int connected = 0;

    private int mInterval = 3000;
    private Handler mHandler;

    List<String> nameList = new ArrayList<>();
    List<String> ipList = new ArrayList<>();
    List<String> typeList = new ArrayList<>();

    Set<Integer> indexes = new HashSet<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = getIntent().getStringExtra("user");
        pwd = getIntent().getStringExtra("pwd");
        ipAddress = getIntent().getStringExtra("ipAddress");
        sessionId = getIntent().getStringExtra("sessionId");
        acCSRFToken = getIntent().getStringExtra("acCSRFToken");

        try {
            getSiteList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();
        startRepeatingTask();

        Button startCallButton = findViewById(R.id.startCallButton);
        Button returnContactButton = findViewById(R.id.returnContactButton);

        startCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Number of checked devices: " + indexes.size());
                if(indexes.size() == 1)
                {
                    Integer[] indexArray = new Integer[indexes.size()];

                    indexes.toArray(indexArray);

                    callIp = ipList.get(indexArray[0]);
                    String callType = typeList.get(indexArray[0]);

                    System.out.println("current IP selected: " + callIp);

                    try {
                        callP2P(callIp, callType);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        returnContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                stopRepeatingTask();

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }


    Runnable mGetMailboxData  = new Runnable() {
        @Override
        public void run() {
            try
            {
                try
                {
                    getMailBoxData();
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

                contactActivity.this.runOnUiThread(new Runnable() {
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

    void getSiteList() throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        System.out.println("this is the sesion id: " + sessionId);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"acCSRFToken\": \"" + acCSRFToken + "\", \"ParamIntArray\": []}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_GetSiteListAPI")
                .post(body)
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("userType", "web")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "82846592-9f76-47f4-b2cd-eb0569256d86,9efa35f2-7ce5-4483-b7bd-d9efd44ac555")
                .addHeader("Host", "192.168.0.50")
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "71")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        System.out.println("about to make a call");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                System.out.println("Something failed in making call, " + e.toString());

                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("got some response");

                final String myResponse = response.body().string();

                contactActivity.this.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al obtener la lista de direcciones";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else {

                                String dataString = json.getString("data");

                                JSONObject dataJson = new JSONObject(dataString);

                                JSONArray astSites = dataJson.getJSONArray("astSites");

                                for (int i = 0; i < astSites.length(); i++) {

                                    JSONObject currentObject = (JSONObject) astSites.get(i);

                                    System.out.println("iteration: " + i);
                                    System.out.println(currentObject);

                                    List<String> singleElement = new ArrayList<>();;

                                    nameList.add(currentObject.getString("szName"));
                                    JSONObject stIPObject =currentObject.getJSONObject("stIP");
                                    if(stIPObject.length() > 0)
                                    {
                                        ipList.add(stIPObject.getString("szIP"));
                                        typeList.add("H323");
                                    }
                                    else
                                    {
                                        ipList.add(currentObject.getJSONObject("stSIP").getString("szIP"));
                                        typeList.add("SIP");
                                    }
                                    System.out.println(singleElement);

                                }

                                String addressBookText = "";

                                TableLayout siteTable = (TableLayout)findViewById(R.id.siteTable);
                                siteTable.setStretchAllColumns(true);
                                siteTable.bringToFront();

                                for (int i = 0; i < astSites.length(); i++)
                                {

                                    TableRow tr = new TableRow(contactActivity.this);
                                    TextView c1 = new TextView(contactActivity.this);
                                    c1.setText(nameList.get(i));
                                    TextView c2 = new TextView(contactActivity.this);
                                    c2.setText(ipList.get(i));
                                    TextView c3 = new TextView(contactActivity.this);
                                    c3.setText(typeList.get(i));
                                    CheckBox c4 = new CheckBox(contactActivity.this);
                                    c4.setId(i);

                                    final int currentId = i;

                                    c4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                            if(isChecked)
                                            {
                                                indexes.add(currentId);
                                            }
                                            else
                                            {
                                                indexes.remove(currentId);
                                            }
                                        }
                                    });

                                    tr.addView(c1);
                                    tr.addView(c2);
                                    tr.addView(c3);
                                    tr.addView(c4);
                                    siteTable.addView(tr);
                                }


                                System.out.println(astSites);

                                System.out.println(myResponse);
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, addressBookText, duration);
                                toast.show();
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

    void callP2P(String dest, String type) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        System.out.println("destination IP Address: " + dest);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body;

        if(type.equals("H323"))
        {
            body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\": \"" +  acCSRFToken + "\",\n" +
                    "\t\"bIsLdapCall\":0,\n" +
                    "\t\"bIsVideoCall\":1,\n" +
                    "\t\"ucEnableH239\":0,\n" +
                    "\t\"stSiteInfo\":\n" +
                    "\t{\n" +
                    "\t\t\"uwID\":0,\n" +
                    "\t\t\"ucType\":3,\n" +
                    "\t\t\"ucDevice\":0,\n" +
                    "\t\t\"bIsLdap\":0,\n" +
                    "\t\t\"ucOnline\":0,\n" +
                    "\t\t\"uwSortPos\":0,\n" +
                    "\t\t\"stTPS\":{},\n" +
                    "\t\t\"stCTS\":{},\n" +
                    "\t\t\"stISDN\":{},\n" +
                    "\t\t\"stIP\":\n" +
                    "\t\t{\n" +
                    "\t\t\t\"ucBaudRate\":152,\n" +
                    "\t\t\t\"szAlias\":\"\",\n" +
                    "\t\t\t\"szIP\":\"\",\n" +
                    "\t\t\t\"szUri\":\"\"\n" +
                    "\t\t},\n" +
                    "\t\t\"stSIP\":{},\n" +
                    "\t\t\"stV35\":{},\n" +
                    "\t\t\"stE1\":{},\n" +
                    "\t\t\"stIPOverE1\":{},\n" +
                    "\t\t\"stT1\":{},\n" +
                    "\t\t\"stPhone\":{},\n" +
                    "\t\t\"stPSTN\":{},\n" +
                    "\t\t\"szName\":\"" + dest + "\",\n" +
                    "\t\t\"szPName\":\"\"\n" +
                    "\t},\n" +
                    "\t\"ucH235Policy\":0\n" +
                    "}");
        }
        else
        {
            body = RequestBody.create(mediaType, "{\n\t\"acCSRFToken\": \"" +  acCSRFToken + "\",\n" +
                    "\t\"bIsLdapCall\":0,\n" +
                    "\t\"bIsVideoCall\":1,\n" +
                    "\t\"ucEnableH239\":0,\n" +
                    "\t\"stSiteInfo\":\n" +
                    "\t{\n" +
                    "\t\t\"uwID\":0,\n" +
                    "\t\t\"ucType\":8,\n" +
                    "\t\t\"ucDevice\":0,\n" +
                    "\t\t\"bIsLdap\":0,\n" +
                    "\t\t\"ucOnline\":0,\n" +
                    "\t\t\"uwSortPos\":0,\n" +
                    "\t\t\"stTPS\":{},\n" +
                    "\t\t\"stCTS\":{},\n" +
                    "\t\t\"stISDN\":{},\n" +
                    "\t\t\"stSIP\":\n" +
                    "\t\t{\n" +
                    "\t\t\t\"ucBaudRate\":152,\n" +
                    "\t\t\t\"szAlias\":\"\",\n" +
                    "\t\t\t\"szIP\":\"\",\n" +
                    "\t\t\t\"szUri\":\"\"\n" +
                    "\t\t},\n" +
                    "\t\t\"stIP\":{},\n" +
                    "\t\t\"stV35\":{},\n" +
                    "\t\t\"stE1\":{},\n" +
                    "\t\t\"stIPOverE1\":{},\n" +
                    "\t\t\"stT1\":{},\n" +
                    "\t\t\"stPhone\":{},\n" +
                    "\t\t\"stPSTN\":{},\n" +
                    "\t\t\"szName\":\"" + dest + "\",\n" +
                    "\t\t\"szPName\":\"\"\n" +
                    "\t},\n" +
                    "\t\"ucH235Policy\":0\n" +
                    "}");
        }

        Request request = new Request.Builder()
                .url("http://" + ipAddress +"/action.cgi?ActionID=WEB_CallSiteAPI")
                .post(body)
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("userType", "web")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "4aeb4524-fea4-4958-9ffe-c1efa9b50d18,ef9d820c-47c0-4b56-b4b6-c7644c9601d5")
                .addHeader("Host", ipAddress)
                .addHeader("accept-encoding", "gzip, deflate")
//                .addHeader("content-length", "514")
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
                System.out.println("got some response from trying to make a call");

                final String myResponse = response.body().string();

                contactActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try
                        {
                            JSONObject json = new JSONObject(myResponse);
                            System.out.println(myResponse);

                            int status = json.getInt("success");

                            String toastText = "";

                            if (status == 0)
                            {
                                toastText = "Error al tratar de realizar la llamada";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();
                            }
                            else {

                                toastText = "LLamada exitosa, mirar en el TV";

                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, toastText, duration);
                                toast.show();

                                Intent intent = new Intent(getBaseContext(), callControl.class);
                                intent.putExtra("user", user);
                                intent.putExtra("pwd", pwd);
                                intent.putExtra("ipAddress", ipAddress);
                                intent.putExtra("sessionId", sessionId);
                                intent.putExtra("acCSRFToken", acCSRFToken);

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
