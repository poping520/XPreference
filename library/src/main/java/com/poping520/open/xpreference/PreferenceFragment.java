package com.poping520.open.xpreference;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.poping520.open.xpreference.PreferenceGroupAdapter.PreferenceViewHolder;


public abstract class PreferenceFragment extends Fragment implements
        PreferenceManager.OnPreferenceTreeClickListener,
        PreferenceManager.OnDisplayPreferenceDialogListener {

    private static final String PREFERENCES_TAG = "android:preferences";

    private Context mStyledContext;
    private PreferenceManager mPreferenceManager;
    private RecyclerView mRecyclerView;
    private boolean mHavePrefs;
    private boolean mInitDone;

    private static final int MSG_BIND_PREFERENCES = 1;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_BIND_PREFERENCES) bindPreferences();
        }
    };

    private Runnable mRequestFocus = () -> {
        mRecyclerView.focusableViewAvailable(mRecyclerView);
    };

    private Runnable mSelectPreferenceRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();

        if (activity == null) {
            throw new IllegalStateException("the activity object was null");
        }

        TypedValue tv = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        int themeResId = tv.resourceId;
        mStyledContext = new ContextThemeWrapper(getActivity(),
                themeResId == 0 ? R.style.XPreferenceTheme : themeResId);

        mPreferenceManager = new PreferenceManager(mStyledContext);

        onCreatePreferences(savedInstanceState);
    }

    protected abstract void onCreatePreferences(@Nullable Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.xpreference_list_fragment, container, false);

        ViewGroup viewGroup = view.findViewById(R.id.list_container);

        mRecyclerView = (RecyclerView) inflater.inflate(R.layout.xpreference_recyclerview, viewGroup, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mStyledContext));
        mRecyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(mRecyclerView));

        viewGroup.addView(mRecyclerView);
        mHandler.post(mRequestFocus);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mHavePrefs) {
            bindPreferences();
            if (mSelectPreferenceRunnable != null) {
                mSelectPreferenceRunnable.run();
                mSelectPreferenceRunnable = null;
            }
        }

        mInitDone = true;
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

    public Preference findPreference(CharSequence key) {
        return getPreferenceScreen().findPreference(key);
    }

    protected void postBindPreferences() {
        if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) return;
        mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
    }

    public PreferenceScreen inflateFromResource(@XmlRes int resId, PreferenceScreen rootPreferences) {
        final PreferenceInflater inflater = new PreferenceInflater(mStyledContext, mPreferenceManager);
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

    public void scrollToPreference(String key) {
        scrollToPreferenceInternal(null, key);
    }

    public void scrollToPreference(final Preference preference) {
        scrollToPreferenceInternal(preference, null);
    }

    private void scrollToPreferenceInternal(final Preference preference, final String key) {
        Runnable runnable = () -> {
            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (!(adapter instanceof PreferenceGroup.PreferencePositionCallback)) {
                if (adapter != null) {
                    throw new IllegalStateException("Adapter must implement PreferencePositionCallback");
                } else {
                    return;
                }
            }
            int position;
            if (preference != null)
                position = ((PreferenceGroup.PreferencePositionCallback) adapter)
                        .getPreferenceAdapterPosition(preference);
            else
                position = ((PreferenceGroup.PreferencePositionCallback) adapter)
                        .getPreferenceAdapterPosition(key);

            if (position != RecyclerView.NO_POSITION)
                mRecyclerView.scrollToPosition(position);
            else
                adapter.registerAdapterDataObserver(
                        new ScrollToPreferenceObserver(adapter, mRecyclerView, preference, key));

        };
        if (mRecyclerView == null) {
            mSelectPreferenceRunnable = runnable;
        } else {
            runnable.run();
        }
    }

    private static class ScrollToPreferenceObserver extends RecyclerView.AdapterDataObserver {
        private final RecyclerView.Adapter mAdapter;
        private final RecyclerView mRecyclerView;
        private final Preference mPreference;
        private final String mKey;

        public ScrollToPreferenceObserver(RecyclerView.Adapter adapter, RecyclerView recyclerView,
                                          Preference preference, String key) {
            mAdapter = adapter;
            mRecyclerView = recyclerView;
            mPreference = preference;
            mKey = key;
        }

        private void scrollToPreference() {
            mAdapter.unregisterAdapterDataObserver(this);
            int position;
            if (mPreference != null)
                position = ((PreferenceGroup.PreferencePositionCallback) mAdapter)
                        .getPreferenceAdapterPosition(mPreference);
            else
                position = ((PreferenceGroup.PreferencePositionCallback) mAdapter)
                        .getPreferenceAdapterPosition(mKey);

            if (position != RecyclerView.NO_POSITION)
                mRecyclerView.scrollToPosition(position);
        }

        @Override
        public void onChanged() {
            scrollToPreference();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            scrollToPreference();
        }
    }

    private class DividerDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mDividerHeight;
        private boolean mAllowDividerAfterLastItem = true;

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            if (mDivider == null) return;

            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
                final View view = parent.getChildAt(childViewIndex);
                if (shouldDrawDividerBelow(view, parent)) {
                    int top = (int) view.getY() + view.getHeight();
                    mDivider.setBounds(0, top, width, top + mDividerHeight);
                    mDivider.draw(c);
                }
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (shouldDrawDividerBelow(view, parent)) {
                outRect.bottom = mDividerHeight;
            }
        }

        private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
            final RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
            final boolean dividerAllowedBelow = holder instanceof PreferenceViewHolder
                    && ((PreferenceViewHolder) holder).isDividerAllowedBelow();

            if (!dividerAllowedBelow) return false;

            boolean nextAllowed = mAllowDividerAfterLastItem;
            int index = parent.indexOfChild(view);
            if (index < parent.getChildCount() - 1) {
                final View nextView = parent.getChildAt(index + 1);
                final RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(nextView);
                nextAllowed = nextHolder instanceof PreferenceViewHolder
                        && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove();
            }
            return nextAllowed;
        }

        public void setDivider(Drawable divider) {
            if (divider != null) {
                mDividerHeight = divider.getIntrinsicHeight();
            } else {
                mDividerHeight = 0;
            }
            mDivider = divider;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setDividerHeight(int dividerHeight) {
            mDividerHeight = dividerHeight;
            mRecyclerView.invalidateItemDecorations();
        }

        public void setAllowDividerAfterLastItem(boolean allowDividerAfterLastItem) {
            mAllowDividerAfterLastItem = allowDividerAfterLastItem;
        }
    }
}
