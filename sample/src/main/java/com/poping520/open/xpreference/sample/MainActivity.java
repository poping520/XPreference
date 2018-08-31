package com.poping520.open.xpreference.sample;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.poping520.open.xpreference.PreferenceFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SampleFragment sampleFragment = new SampleFragment();
        SampleFragmentV7 sampleFragmentV7 = new SampleFragmentV7();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, sampleFragment)
                .commit();

    }

    public static class SampleFragment extends PreferenceFragment {

        @Override
        protected void onCreatePreferences(@Nullable Bundle savedInstanceState) {
            addPreferencesFromResource(R.xml.xpref_sample);

        }
    }

    public static class SampleFragmentV7 extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.xpref_sample);
        }
    }
}
