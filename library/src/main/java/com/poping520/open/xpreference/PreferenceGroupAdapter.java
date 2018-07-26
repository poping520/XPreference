package com.poping520.open.xpreference;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangKZ on 18/07/26.
 *
 * @author poping520
 * @version 1.0.0
 */
class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceGroupAdapter.PreferenceViewHolder> implements Preference.OnPreferenceChangeInternalListener {

    private PreferenceGroup mPreferenceGroup;

    private List<Preference> mList;

    private List<Preference> mListInternal;

    private List<PreferenceLayout> mLayoutList;


    PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mList = new ArrayList<>();
        mListInternal = new ArrayList<>();
        mLayoutList = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen)
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).isShouldUseGeneratedIds());
        else
            setHasStableIds(true);

        syncPreferences();

    }

    private void syncPreferences() {
        for (Preference p : mListInternal) {
            p.setOnPreferenceChangeInternalListener(null);
        }

        List<Preference> fullPreferenceList = new ArrayList<>(mListInternal.size());
        flattenPreferenceGroup(fullPreferenceList, mPreferenceGroup);
    }

    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup preferenceGroup) {
        preferenceGroup.sortPreferences();
        List<Preference> groupPreferenceList = preferenceGroup.getPreferenceList();
        for (Preference p : groupPreferenceList) {
            preferences.add(p);


        }
    }

    private void getPreferenceCount(Preference preference) {

    }

    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    @Override
    public void onPreferenceChange(Preference preference) {

    }

    @Override
    public void onPreferenceHierarchyChange(Preference preference) {

    }

    @Override
    public void onPreferenceVisibilityChange(Preference preference) {

    }

    @NonNull
    @Override
    public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    private static class PreferenceLayout {
        private int resId;
        private int widgetResId;
        private String name;

        private PreferenceLayout() {
        }

        private PreferenceLayout(PreferenceLayout other) {
            resId = other.resId;
            widgetResId = other.widgetResId;
            name = other.name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PreferenceLayout)) {
                return false;
            }
            final PreferenceLayout other = (PreferenceLayout) o;
            return resId == other.resId && widgetResId == other.widgetResId && TextUtils.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + resId;
            result = 31 * result + widgetResId;
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    public static class PreferenceViewHolder extends RecyclerView.ViewHolder {

        public PreferenceViewHolder(View itemView) {
            super(itemView);
        }
    }
}
