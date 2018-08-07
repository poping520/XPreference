package com.poping520.open.xpreference;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceGroupAdapter.PreferenceViewHolder>
        implements Preference.OnPreferenceChangeInternalListener {

    private PreferenceGroup mPreferenceGroup;

    private List<Preference> mPreferences;

    private List<Preference> mPreferencesInternal;

    private List<PreferenceLayout> mPreferenceLayouts;

    private PreferenceLayout mTempLayout = new PreferenceLayout();

    private static final String TAG = "PreferenceGroupAdapter";

    PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferences = new ArrayList<>();
        mPreferencesInternal = new ArrayList<>();
        mPreferenceLayouts = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen)
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).isShouldUseGeneratedIds());
        else
            setHasStableIds(true);

        syncPreferences();

        Log.e(TAG, "PreferenceGroupAdapter: ===" );
    }

    private void syncPreferences() {
        for (Preference p : mPreferencesInternal) {
            p.setOnPreferenceChangeInternalListener(null);
        }

        List<Preference> fullList = new ArrayList<>(mPreferencesInternal.size());
        flattenPreferenceGroup(fullList, mPreferenceGroup);

        List<Preference> newVisibleList = new ArrayList<>(fullList.size());

        for (Preference p : fullList) {
            if (p.isVisible()) {
                newVisibleList.add(p);
            }
        }

        List<Preference> oldVisibleList = mPreferences;
        mPreferences = newVisibleList;
        mPreferencesInternal = fullList;

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
        if (!mPreferenceLayouts.contains(pl)) {
            mPreferenceLayouts.add(pl);
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
        return mPreferences.get(position);
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
        Context context = parent.getContext();
        PreferenceLayout pl = mPreferenceLayouts.get(viewType);
        LayoutInflater inflater = LayoutInflater.from(context);

        TypedArray a = context.obtainStyledAttributes(null, R.styleable.BackgroundStyle);
        Drawable bg = a.getDrawable(R.styleable.BackgroundStyle_android_selectableItemBackground);
        if (bg == null) {
            bg = ContextCompat.getDrawable(context, android.R.drawable.list_selector_background);
        }
        a.recycle();

        View view = inflater.inflate(pl.resId, parent, false);
        if (view.getBackground() == null) {
            view.setBackgroundDrawable(bg);
        }

        ViewGroup widgetFrame = view.findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            if (pl.widgetResId != 0)
                inflater.inflate(pl.widgetResId, widgetFrame);
            else
                widgetFrame.setVisibility(View.GONE);
        }
        return new PreferenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        getItem(position).onBindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return mPreferences.size();
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

        int index = mPreferenceLayouts.indexOf(mTempLayout);

        if (index != -1) return index;
        else {
            index = mPreferences.size();
            mPreferenceLayouts.add(new PreferenceLayout(mTempLayout));
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

        private final SparseArray<View> mCachedViews = new SparseArray<>(4);

        PreferenceViewHolder(View itemView) {
            super(itemView);

            mCachedViews.put(R.id.title, itemView.findViewById(R.id.title));
            mCachedViews.put(R.id.summary, itemView.findViewById(R.id.summary));
            mCachedViews.put(R.id.icon, itemView.findViewById(R.id.icon));
            mCachedViews.put(R.id.icon_frame, itemView.findViewById(R.id.icon_frame));
        }

        @SuppressWarnings("unchecked")
        public <T extends View> T findViewById(@IdRes int id) {
            View cachedView = mCachedViews.get(id);
            if (cachedView != null) return (T) cachedView;
            else {
                T view = itemView.findViewById(id);
                if (view != null) mCachedViews.put(id, view);
                return view;
            }
        }
    }
}
