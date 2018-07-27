package com.poping520.open.xpreference;


import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
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

    private PreferenceLayout mTempLayout = new PreferenceLayout();


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

        List<Preference> fullList = new ArrayList<>(mListInternal.size());
        flattenPreferenceGroup(fullList, mPreferenceGroup);

        List<Preference> newVisibleList = new ArrayList<>(fullList.size());

        for (Preference p : fullList) {
            if (p.isVisible()) {
                newVisibleList.add(p);
            }
        }

        List<Preference> oldVisibleList = mList;
        mList = newVisibleList;
        mListInternal = fullList;

        PreferenceManager mgr = mPreferenceGroup.getPreferenceManager();
        if (mgr != null && mgr.getPreferenceComparisonCallback() != null) {
            PreferenceManager.PreferenceComparisonCallback callback = mgr.getPreferenceComparisonCallback();

            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldVisibleList.size();
                }

                @Override
                public int getNewListSize() {
                    return newVisibleList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return callback.arePreferenceItemsTheSame(oldVisibleList.get(oldItemPosition),
                            newVisibleList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return callback.arePreferenceContentsTheSame(oldVisibleList.get(oldItemPosition),
                            newVisibleList.get(newItemPosition));
                }
            });

            diffResult.dispatchUpdatesTo(this);
        } else
            notifyDataSetChanged();

        for (Preference p : fullList) {
            p.clearWasDetached();
        }


    }

    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup group) {
        group.sortPreferences();

        List<Preference> groupPreferenceList = group.getPreferenceList();
        //TODO
        preferences.addAll(groupPreferenceList);

        for (Preference p : groupPreferenceList) {
            addPreferenceClassName(p);

            if (p instanceof PreferenceGroup) {
                PreferenceGroup asGroup = (PreferenceGroup) p;
                if (asGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, asGroup);
                }
            }
            p.setOnPreferenceChangeInternalListener(this);
        }
    }

    private void addPreferenceClassName(Preference preference) {
        PreferenceLayout pl = createPreferenceLayout(preference, null);
        if (!mLayoutList.contains(pl)) {
            mLayoutList.add(pl);
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

    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mList.get(position);
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
        getItem(position).onBindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds())
            return RecyclerView.NO_ID;
        return this.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        Preference item = getItem(position);
        mTempLayout = createPreferenceLayout(item, mTempLayout);

        int index = mLayoutList.indexOf(mTempLayout);

        if (index != -1) return index;
        else {
            index = mList.size();
            mLayoutList.add(new PreferenceLayout(mTempLayout));
            return index;
        }
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
