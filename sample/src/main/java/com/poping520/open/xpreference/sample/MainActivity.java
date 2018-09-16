package com.poping520.open.xpreference.sample;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.EditTextPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.poping520.open.xpreference.ListPreference;
import com.poping520.open.xpreference.OptionEditTextPreference;
import com.poping520.open.xpreference.PreferenceFragment;
import com.poping520.open.xpreference.storage.JSONStorage;
import com.poping520.open.xpreference.storage.PropertiesStorage;

import java.io.File;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SampleFragment sampleFragment = new SampleFragment();
        SampleFragmentV7 sampleFragmentV7 = new SampleFragmentV7();
        SampleFragmentApp sampleFragmentApp = new SampleFragmentApp();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main, sampleFragment)
                .commit();

//        getFragmentManager()
//                .beginTransaction()
//                .replace(R.id.main, sampleFragmentApp)
//                .commit();
    }

    public static class SampleFragment extends PreferenceFragment {

        @Override
        protected void onCreatePreferences(@Nullable Bundle savedInstanceState) {
            PropertiesStorage storage = new PropertiesStorage("/sdcard/000/text.prop");
            getPreferenceManager().setStorage(storage);
            addPreferencesFromResource(R.xml.xpref_sample);

            OptionEditTextPreference list = findPreference("optionEditText");


        }
    }

    public static class SampleFragmentV7 extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.xpref_sample);
        }
    }

    public static class SampleFragmentApp extends android.preference.PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.xpref_sample);
        }
    }
}
