package com.poping520.open.xpreference;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

//completed
class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceGroupAdapter.PreferenceViewHolder>
        implements Preference.XPreferenceChangeInternalListener, PreferenceGroup.PreferencePositionCallback {

    private PreferenceGroup mPreferenceGroup;

    private List<Preference> mPreferenceList;

    private List<Preference> mPreferenceListInternal;

    private List<PreferenceLayout> mPreferenceLayouts;

    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

    private Handler mHandler = new Handler();

    private Runnable mSyncRunnable = this::syncPreferences;

    PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<>();
        mPreferenceListInternal = new ArrayList<>();
        mPreferenceLayouts = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen)
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).isShouldUseGeneratedIds());
        else
            setHasStableIds(true);

        syncPreferences();
    }

    private void syncPreferences() {
        for (Preference p : mPreferenceListInternal) {
            p.setOnPreferenceChangeInternalListener(null);
        }

        List<Preference> fullList = new ArrayList<>(mPreferenceListInternal.size());
        flattenPreferenceGroup(fullList, mPreferenceGroup);

        List<Preference> newVisibleList = new ArrayList<>(fullList.size());

        for (Preference p : fullList) {
            if (p.isVisible()) {
                newVisibleList.add(p);
            }
        }

        List<Preference> oldVisibleList = mPreferenceList;
        mPreferenceList = newVisibleList;
        mPreferenceListInternal = fullList;

        PreferenceManager mgr = mPreferenceGroup.getXPreferenceManager();
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

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference p = group.getPreference(i);

            preferences.add(p);

            addPreferenceClassName(p);

            if (p instanceof PreferenceGroup) {
                final PreferenceGroup asGroup = (PreferenceGroup) p;
                if (asGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, asGroup);
                }
            }

            p.setOnPreferenceChangeInternalListener(this);
        }
    }

    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private void addPreferenceClassName(Preference preference) {
        PreferenceLayout pl = createPreferenceLayout(preference, null);
        if (!mPreferenceLayouts.contains(pl)) {
            mPreferenceLayouts.add(pl);
        }
    }

    @Override
    public int getItemCount() {
        return mPreferenceList.size();
    }

    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mPreferenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds())
            return RecyclerView.NO_ID;
        return this.getItem(position).getId();
    }

    @Override
    public void onPreferenceChange(Preference preference) {
        int index = mPreferenceList.indexOf(preference);
        if (index != -1) notifyItemChanged(index, preference);
    }

    @Override
    public void onPreferenceHierarchyChange(Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public void onPreferenceVisibilityChange(Preference preference) {
        if (!mPreferenceListInternal.contains(preference)) return;

        if (preference.isVisible()) {
            int previousVisibleIndex = -1;
            for (Preference pref : mPreferenceListInternal) {
                if (preference.equals(pref)) break;
                if (pref.isVisible()) previousVisibleIndex++;
            }
            mPreferenceList.add(previousVisibleIndex, preference);
            notifyItemInserted(previousVisibleIndex);
        } else {
            int removalIndex;
            int listSize = mPreferenceList.size();
            for (removalIndex = 0; removalIndex < listSize; removalIndex++) {
                if (preference.equals(mPreferenceList.get(removalIndex))) break;
            }
            mPreferenceList.remove(removalIndex);
            notifyItemRemoved(removalIndex);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Preference item = getItem(position);
        mTempPreferenceLayout = createPreferenceLayout(item, mTempPreferenceLayout);

        int viewType = mPreferenceLayouts.indexOf(mTempPreferenceLayout);

        if (viewType != -1) return viewType;
        else {
            viewType = mPreferenceList.size();
            mPreferenceLayouts.add(new PreferenceLayout(mTempPreferenceLayout));
            return viewType;
        }
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
            ViewCompat.setBackground(view, bg);
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
    public int getPreferenceAdapterPosition(String key) {
        int size = mPreferenceList.size();
        for (int i = 0; i < size; i++) {
            Preference candidate = mPreferenceList.get(i);
            if (TextUtils.equals(key, candidate.getKey())) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getPreferenceAdapterPosition(Preference preference) {
        int size = mPreferenceList.size();
        for (int i = 0; i < size; i++) {
            Preference candidate = mPreferenceList.get(i);
            if (candidate != null && candidate.equals(preference)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
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
        private boolean mDividerAllowedAbove;
        private boolean mDividerAllowedBelow;

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

        public boolean isDividerAllowedAbove() {
            return mDividerAllowedAbove;
        }

        public void setDividerAllowedAbove(boolean allowed) {
            mDividerAllowedAbove = allowed;
        }

        public boolean isDividerAllowedBelow() {
            return mDividerAllowedBelow;
        }

        public void setDividerAllowedBelow(boolean allowed) {
            mDividerAllowedBelow = allowed;
        }
    }
}
