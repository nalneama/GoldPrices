package com.nasser.android.stocksratios;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;


public class Stocks_new extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocks_new);

        android.app.FragmentManager fm = getFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment==null){

            fragment = new GoldFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }



    }

}
