package com.poping520.open.xpreference.sample;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.poping520.open.xpreference.PreferenceFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SampleFragment sampleFragment = new SampleFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, sampleFragment)
                .commit();

    }

    public static class SampleFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.xpref_sample);
        }
    }
}
