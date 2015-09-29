package com.nasser.android.stocksratios;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GoldFragment extends Fragment {

    private TextView mInfo;
    private TextView mProfit;
    private final static double ten_tolas=116.63;
    private final static double GRAMTOTROYOUNCE =0.032151;
    private double myprice=17325;
    private int mRefreshRate=10;
    private final static String mGoldUrl="http://rate-exchange.appspot.com/currency?from=XAU&to=QAR";
    private TimeUnit time=TimeUnit.SECONDS;
    public static final String PREFS_NAME = "MySavedData";
    private FrameLayout mFrame;
    private ImageButton mSettingsButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.gold_fragment,container,false);

        mFrame = (FrameLayout)v.findViewById(R.id.card_frame);
        mInfo = (TextView)v.findViewById(R.id.info_text);
        mProfit = (TextView)v.findViewById(R.id.profit);
        mSettingsButton = (ImageButton)v.findViewById(R.id.settings_button);

        // Retrieve saved price and info
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        mInfo.setText(settings.getString("goldPrice", ""));
        mProfit.setText(settings.getString("profitLoss",""));

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFrame.setVisibility(View.GONE);
            }
        });

        // Before attempting to fetch the URL, makes sure that there is a network connection.
        //ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        //if (networkInfo != null && networkInfo.isConnected()) {
        // fetch data
        new DownloadWebPageTask().execute(mGoldUrl);

        recurring_download();

        return v;


    }

    private void recurring_download(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // call service
                new DownloadWebPageTask().execute(mGoldUrl);
            }
        }, 1, mRefreshRate, time);

    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            ConnectivityManager connMgr = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (!(networkInfo != null && networkInfo.isConnected())) {
                this.cancel(true);
                // to test the cancel
                Toast.makeText(getActivity().getApplication(), "Cancelled", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onCancelled() {

            super.onCancelled();
            recurring_download();
        }

        @Override
        protected String doInBackground(String... urls) {
            //try to download the data;

            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }

        }

        @Override
        protected void onPostExecute(String result) {

            //to do after getting the results
            String asd[] = result.split(" ");
            if (asd[0].equals("Error"))
            {
                Toast.makeText(getActivity().getApplication(),"Error",Toast.LENGTH_SHORT).show();
            }

            else {
                double dbl = Double.parseDouble(asd[3].substring(0, asd[3].length() - 1));

                long dblinqar = (long) (dbl * GRAMTOTROYOUNCE * ten_tolas);

                mInfo.setText(String.format("%,d", dblinqar) + " QR");
                mProfit.setText(String.format("%,d", dblinqar - (long) myprice) + " QR");

                // to test refresh rate
                Toast.makeText(getActivity().getApplication(), "Refresh", Toast.LENGTH_SHORT).show();

                if (dblinqar <= myprice) {
                    mProfit.setTextColor(getResources().getColor(R.color.red));
                } else {
                    mProfit.setTextColor(getResources().getColor(R.color.green));
                }
            }
        }



        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 200 characters of the retrieved web page content.
            int len = 200;



            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("DEBUG_TAG", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }




        }

        // Reads an InputStream and converts it to a String.
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }
    }
}

