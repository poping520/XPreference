package com.poping520.open.xpreference;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class PreferenceFragment extends Fragment {

    private Context mContext;
    private PreferenceManager mPreferenceManager;
    private boolean mHavePrefs;
    private boolean mInitDone;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        mPreferenceManager = new PreferenceManager(mContext);


        onCreatePreferences(savedInstanceState);
    }

    protected abstract void onCreatePreferences(@Nullable Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceManager.getPreferenceScreen();
    }

    public void addPreferencesFromResource(@XmlRes int preferenceResId) {
        PreferenceScreen preferenceScreen = inflateFromResource(preferenceResId, getPreferenceScreen());
        setPreferenceScreen(preferenceScreen);
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (mPreferenceManager.setPreferenceScreen(preferenceScreen) && preferenceScreen != null) {
            mHavePrefs = true;
            if (mInitDone) {
                postBindPreferences();
            }
        }
    }

    protected void postBindPreferences() {

    }

    public PreferenceScreen inflateFromResource(@XmlRes int resId, PreferenceScreen rootPreferences) {
        final PreferenceInflater inflater = new PreferenceInflater(mContext, mPreferenceManager);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(mPreferenceManager);
        return rootPreferences;
    }


}
