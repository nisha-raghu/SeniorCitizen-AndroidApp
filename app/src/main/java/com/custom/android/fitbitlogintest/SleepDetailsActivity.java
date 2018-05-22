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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SleepDetailsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);
        String SleepUrl = "https://p17a212g7d.execute-api.us-east-2.amazonaws.com/sleepmob/sleepmob";
        String result;
        SleepDetailsActivity.HttpGetData h = new SleepDetailsActivity.HttpGetData();
        try {
            result= h.execute(SleepUrl).get();
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
            String steps;

            try {
                JSONObject res = new JSONObject(result);
                JSONArray arr = res.getJSONArray("sleep");
                // Log.d("Step Count data", String.valueOf(arr));
                BarChart chart = (BarChart) findViewById(R.id.chart);
                ArrayList<BarEntry> BarEntry = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                int count=0;
                for (int i = arr.length()-1; i >= 0 ; i--)
                {
                    value = (arr.getJSONObject(i).getString("dateOfSleep"));
                    steps = (arr.getJSONObject(i).getString("minutesAsleep"));
                     float sleepinfo=Float.valueOf(steps)/60;
                    Log.d("dates",value) ;
                    labels.add(String.valueOf((value)));
                    BarEntry.add(new BarEntry(Float.valueOf(sleepinfo), count));
                    count++;

                }
                BarDataSet dataSet = new BarDataSet(BarEntry, "Sleep Info");
                dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                BarData data = new BarData(labels, dataSet);
                chart.setData(data);
                //chart.getDescription().setEnabled(false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
