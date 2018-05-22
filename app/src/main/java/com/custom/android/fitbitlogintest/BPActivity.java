package com.custom.android.fitbitlogintest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class BPActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bp_details);
        String BPUrl = "https://2d6u0kva1b.execute-api.us-east-2.amazonaws.com/ihealthall/ihealthall";
        String result;
        BPActivity.HttpGetData h = new BPActivity.HttpGetData();
        try {
            result= h.execute(BPUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class HttpGetData extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            String result = null;
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String value;
            String hp;
            String lp;

            try {
                JSONObject res = new JSONObject(result);
                JSONArray arr = res.getJSONArray("BPDataList");
                BarChart chart = (BarChart) findViewById(R.id.chart);
                ArrayList<BarEntry> BarEntry1 = new ArrayList<>();
                ArrayList<BarEntry> BarEntry2 = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                //BarData data = new BarData(labels, dataSet);

                //chart.setData(data);
                //chart.setDescription("Heart rate");
                int count=0;
                for (int i = arr.length()-1; i > arr.length() - 7 ; i--)
                {
                    value = (arr.getJSONObject(i).getString("MDate"));
                    Date date = new Date(Long.valueOf(value)*1000L);
                    SimpleDateFormat jdf = new SimpleDateFormat("MM-dd HH:mm");
                    String java_date = jdf.format(date);
                    hp = (arr.getJSONObject(i).getString("HP"));
                    lp = (arr.getJSONObject(i).getString("LP"));
                    labels.add(String.valueOf((java_date)));
                    BarEntry1.add(new BarEntry(Float.valueOf(hp), count));
                    BarEntry2.add(new BarEntry(Float.valueOf(lp), count));
                    count++;

                }
                BarDataSet dataSet1 = new BarDataSet(BarEntry1, "HP");
                BarDataSet dataSet2 = new BarDataSet(BarEntry2, "LP");
                dataSet1.setColors(ColorTemplate.VORDIPLOM_COLORS);
                dataSet2.setColors(ColorTemplate.VORDIPLOM_COLORS);
                BarData data1 = new BarData(labels, dataSet1);
                BarData data2 = new BarData(labels, dataSet2);
                ArrayList<BarDataSet> dataSets = null;
                dataSets = new ArrayList<>();
                dataSets.add(dataSet1);
                dataSets.add(dataSet2);
                BarData data = new BarData(labels, dataSets);
                chart.setData(data);
                //chart.getDescription().setEnabled(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}

