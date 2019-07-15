package com.batsac.confcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
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
import java.util.Calendar;
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

import static java.lang.Integer.parseInt;

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
    List<Integer> uwIdList = new ArrayList<>();

    Set<Integer> indexes = new HashSet<Integer>();

    @SuppressLint("ClickableViewAccessibility")
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
        connected = parseInt(getIntent().getStringExtra("connected"));

        try {
            getSiteList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mHandler = new Handler();
        startRepeatingTask();

        final ImageButton startCallButton = findViewById(R.id.startCallButton);
        final ImageButton returnContactButton = findViewById(R.id.returnContactButton);

        startCallButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:

                        startCallButton.setBackgroundResource(R.drawable.create_pressed);
                        return true;
                    case MotionEvent.ACTION_UP:
                        startCallButton.setBackgroundResource(R.drawable.create_normal);

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
                        else if(indexes.size() > 1)
                        {
                            Integer[] indexArray = new Integer[indexes.size()];

                            indexes.toArray(indexArray);

                            StringBuilder idList = new StringBuilder("[");

                            for(int i=0; i < indexes.size(); i++)
                            {
                                if(i == indexes.size() - 1)
                                {
                                    idList.append(uwIdList.get(indexArray[i]).toString());
                                }
                                else
                                {
                                    idList.append(uwIdList.get(indexArray[i]).toString()).append(",");
                                }
                            }
                            idList.append("]");

                            try {
                                startConference(idList.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                        returnContactButton.setBackgroundResource(R.drawable.b14p);
                        return true;
                    case MotionEvent.ACTION_UP:
                        returnContactButton.setBackgroundResource(R.drawable.b14);

                        stopRepeatingTask();

                        Intent intent = new Intent(getBaseContext(), MainActivity.class);

                        intent.putExtra("user", user);
                        intent.putExtra("pwd", pwd);
                        intent.putExtra("ipAddress", ipAddress);
                        intent.putExtra("sessionId", sessionId);
                        intent.putExtra("acCSRFToken", acCSRFToken);
                        intent.putExtra("presentation", "false");
                        if(connected == 0)
                        {
                            intent.putExtra("connected", "0");
                        }
                        else if(connected == 1)
                        {
                            intent.putExtra("connected", "1");
                        }

                        startActivity(intent);

                        return true;
                }

                return false;
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

                                    uwIdList.add(currentObject.getInt("uwID"));

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
//                                siteTable.setStretchAllColumns(true);
                                siteTable.bringToFront();

                                for (int i = 0; i < astSites.length(); i++)
                                {

                                    TableRow tr = new TableRow(contactActivity.this);
                                    TextView c1 = new TextView(contactActivity.this);
                                    c1.setText(nameList.get(i));
                                    c1.setTextColor(Color.WHITE);
//                                    TextView c2 = new TextView(contactActivity.this);
//                                    c2.setText(ipList.get(i));
//                                    c2.setTextColor(Color.WHITE);
//                                    TextView c3 = new TextView(contactActivity.this);
//                                    c3.setText(typeList.get(i));
//                                    c3.setTextColor(Color.WHITE);
                                    CheckBox c4 = new CheckBox(contactActivity.this);
                                    c4.setId(i);
                                    c4.setBackgroundColor(Color.WHITE);
                                    c4.setHighlightColor(Color.WHITE);

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
//                                    tr.addView(c2);
//                                    tr.addView(c3);
                                    tr.addView(c4);
                                    siteTable.addView(tr);
                                }


                                System.out.println(astSites);

                                System.out.println(myResponse);
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;

//                                Toast toast = Toast.makeText(context, addressBookText, duration);
//                                toast.show();
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
                                intent.putExtra("type", "call");
                                intent.putExtra("presentation", "false");
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
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void startConference(String ids) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

//        Calendar rightNow = Calendar.getInstance();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n\t\"uwID\":65536,\n\t\"szName\"" +
                ":\"PreConf record 001\",\n\t\"ucConfMode\":0,\n\t\"stStartTime\":{},\n\t" +
                "\"ucCallType\":254,\n\t\"uwBaudRate\":159,\n\t\"bSupportMultiStream\":254," +
                "\n\t\"ucAuxStrmRoleLabel\":254,\n\t\"ucMutiPicGroupNum\":0,\n\t\"ucSubPicNum\":0," +
                "\n\t\"ucMutiPicMode\":0,\n\t\"ucSrtpEncrypt\":2,\n\t\"bMiniMcuCallset\":0,\n\t" +
                "\"stMcuCallsetInfo\":\n\t{\n\t\t\"ucPreType\":0,\n\t\t\"stPreTime\":\n\t\t{\n\t\t" +
                "\t\"year\":2019,\n\t\t\t\"month\":6,\n\t\t\t\"day\":21,\n\t\t\t\"hour\":11,\n\t\t\t" +
                "\"minute\":33,\n\t\t\t\"second\":0\n\t\t},\n\t\t\"uwDuration\":0,\n\t\t" +
                "\"ucMultiPic\":0,\n\t\t\"ucH235Policy\":0,\n\t\t\"bDataConf\":0,\n\t\t" +
                "\"ucMLPRate\":0,\n\t\t\"uwIPAnonymousSiteNum\":0,\n\t\t" +
                "\"uwISDNAnonymousSiteNum\":0,\n\t\t\"uwPSTNAnonymousSiteNum\":0,\n\t\t" +
                "\"uwSIPAnonymousSiteNum\":0,\n\t\t\"szConfCtrlPassword\":\"\",\n\t\t\"szCardNo\"" +
                ":\"\",\n\t\t\"szPassword\":\"\",\n\t\t\"ucPaySide\":1,\n\t\t\"ucVideoEncode\":254," +
                "\n\t\t\"ucVideoFormat\":254,\n\t\t\"ucVideoFrame\":254,\n\t\t\"ucAudioEncode\":254," +
                "\n\t\t\"ucDuleAudioChn\":254,\n\t\t\"ucLSDRate\":0,\n\t\t" +
                "\"ucAuxStreamProtocol\":254,\n\t\t\"uwAuxStreamBandWidth\":0,\n\t\t" +
                "\"ucAuxStreamFormat\":254,\n\t\t\"ucAuxStreamFrame\":254,\n\t\t\"ucIsUseVoiceSwitch" +
                "\":0,\n\t\t\"ucVoiceSwitchType\":0,\n\t\t\"ucVoiceSwitchLimit\":0},\n\t" +
                "\"stMiniMcuCallsetInfo\":\n\t{\n\t\t\"bJoinLocalConf\":0,\n\t\t\"ucVideoEncode" +
                "\":0,\n\t\t\"ucVideoFormat\":0,\n\t\t\"ucAudioEncode\":0,\n\t\t\"ucLSDRate\":0," +
                "\n\t\t\"ucAuxStreamProtocol\":0,\n\t\t\"ucAuxStreamBandWidth\":0,\n\t\t" +
                "\"ucAuxStreamFormat\":0\n\t},\n\t\"uwTotalSiteCount\":0,\n\t" +
                "\"auwSiteID\":" + ids + ",\n\t\"auwGroupID\":[]," +
                "\n\t\"astTempSiteInfo\":[],\n\t\"ulUseFre\":0,\n\t\"ucIsDirectBroadcast\":0,\n\t" +
                "\"ucIsRecordPlay\":0,\n\t\"ulSetReportMode\":0,\n\t" +
                "\"acCSRFToken\":\"" + acCSRFToken + "\"\n}");
        Request request = new Request.Builder()
                .url("http://" + ipAddress + "/action.cgi?ActionID=WEB_ScheduleConfAPI")
                .post(body)
                .addHeader("userType", "web")
                .addHeader("Cookie", "SessionID=" + sessionId)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("Postman-Token", "883bda72-443b-4fc9-a0c9-1bf94e0984c5")
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

                System.out.println("response from conf start: " + myResponse);

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
                                intent.putExtra("type", "conference");
                                intent.putExtra("presentation", "false");

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
