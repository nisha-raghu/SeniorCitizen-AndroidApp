package com.custom.android.fitbitlogintest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

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

public class HeartDataActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);
        String HeartrateUrl = "https://2d6u0kva1b.execute-api.us-east-2.amazonaws.com/ihealthall/ihealthall";
        String result;
        HeartDataActivity.HttpGetData h = new HeartDataActivity.HttpGetData();
        try {
            result = h.execute(HeartrateUrl).get();
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
                JSONArray arr = res.getJSONArray("BPDataList");
                // Log.d("Step Count data", String.valueOf(arr));
                for (int i = 4; i < 11; i++) {
                    value = (arr.getJSONObject(i).getString("MDate"));
                    steps = (arr.getJSONObject(i).getString("HR"));
                }
                BarChart chart = (BarChart) findViewById(R.id.chart);
                ArrayList<BarEntry> BarEntry = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                //BarData data = new BarData(labels, dataSet);

                //chart.setData(data);
                //chart.setDescription("Heart rate");
                int count=0;
                for (int i = arr.length()-1; i > arr.length() - 7 ; i--)
                {
                    value = (arr.getJSONObject(i).getString("MDate"));
                    Date date = new Date(Long.valueOf(value)*1000L);
                    // format of the date
                    SimpleDateFormat jdf = new SimpleDateFormat("MM-dd HH:mm");
                    String java_date = jdf.format(date);

                    steps = (arr.getJSONObject(i).getString("HR"));
                    labels.add(String.valueOf((java_date)));
                    BarEntry.add(new BarEntry(Float.valueOf(steps), count));
                    count++;

                }
                        /*    labels.add("1");
                            labels.add("2");
                            labels.add("3");
                            labels.add("4");
                            labels.add("5");
                            labels.add("6");
                            labels.add("7");

                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(5).getString("value"))), 1));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(6).getString("value"))), 2));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(7).getString("value"))), 3));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(8).getString("value"))), 4));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(9).getString("value"))), 5));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(10).getString("value"))), 6));
                  BarEntry.add(new BarEntry(Float.valueOf((arr.getJSONObject(11).getString("value"))), 7));     */
                BarDataSet dataSet = new BarDataSet(BarEntry, "Step Count");
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
