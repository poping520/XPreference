package com.poping520.open.xpreference;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.Fragment;


public class PreferenceFragment extends Fragment {

    private Context mContext;
    private PreferenceManager mPreferenceManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferenceManager = new PreferenceManager();
        mContext = getActivity();
    }


    public void addPreferencesFromResource(@XmlRes int preferenceResId) {
        inflateFromResource(preferenceResId, null);
    }

    public PreferenceScreen inflateFromResource(@XmlRes int resId, PreferenceScreen rootPreferences) {
        final PreferenceInflater inflater = new PreferenceInflater(mContext, mPreferenceManager);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(mPreferenceManager);
        return rootPreferences;
    }
}
