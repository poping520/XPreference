package com.poping520.open.xpreference;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class PreferenceFragment extends Fragment implements
        PreferenceManager.OnPreferenceTreeClickListener,
        PreferenceManager.OnDisplayPreferenceDialogListener {

    private static final String PREFERENCES_TAG = "android:preferences";

    private Context mContext;
    private PreferenceManager mPreferenceManager;
    private RecyclerView mRecyclerView;
    private boolean mHavePrefs;
    private boolean mInitDone;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) bindPreferences();
        }
    };

    private Runnable mRequestFocus = () -> {
        mRecyclerView.focusableViewAvailable(mRecyclerView);
    };

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

        View view = inflater.inflate(R.layout.xpreference_list_fragment, container, false);

        ViewGroup viewGroup = view.findViewById(R.id.list_container);

        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.xpreference_recyclerview, viewGroup, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(mRecyclerView));

        viewGroup.addView(mRecyclerView);
        mHandler.post(mRequestFocus);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (bundle != null && getPreferenceScreen() != null) {
                getPreferenceScreen().restoreHierarchyState(bundle);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreferenceManager.setOnPreferenceTreeClickListener(this);
        mPreferenceManager.setOnDisplayPreferenceDialogListener(this);
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


    private void bindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            mRecyclerView.setAdapter(new PreferenceGroupAdapter(preferenceScreen));
            preferenceScreen.onAttached();
        }
        onBindPreferences();
    }

    protected void onBindPreferences() {

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return false;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

    }
}
