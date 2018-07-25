package com.poping520.open.xpreference;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.InflateException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;


class PreferenceInflater {

    private static final String TAG = "PreferenceInflater";

    private static final HashMap<String, Constructor> CONSTRUCTOR_MAP = new HashMap<>();

    private final Context mContext;

    private final Object[] mConstructorArgs = new Object[2];

    private PreferenceManager mPreferenceManager;

    private static final String X_PREFERENCE_PACKAGE = "com.poping520.open.xpreference.";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String EXTRA_TAG_NAME = "extra";

    PreferenceInflater(Context context, PreferenceManager preferenceManager) {
        mContext = context;
        mPreferenceManager = preferenceManager;
    }

    Preference inflate(int resource, @Nullable PreferenceGroup root) {
        synchronized (mConstructorArgs) {
            XmlResourceParser parser = mContext.getResources().getXml(resource);
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            mConstructorArgs[0] = mContext;
            final Preference result;
            try {
                int type;
                do type = parser.next();
                while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);

                if (type != XmlPullParser.START_TAG)
                    throw new XmlPullParserException(parser.getPositionDescription() + ": No start tag found!");

                Preference xmlRoot = createItemFromTag(parser.getName(), attrs);
                result = onMergeRoots(root, (PreferenceGroup) xmlRoot);
                inflateAll(parser, result, attrs);

            } catch (XmlPullParserException e) {
                throw new InflateException(e.getMessage(), e);
            } catch (IOException e) {
                throw new InflateException(parser.getPositionDescription() + ": " + e.getMessage(), e);
            } finally {
                parser.close();
            }
            return result;
        }
    }

    private Preference createItemFromTag(String name, AttributeSet attrs) {
        Constructor constructor = CONSTRUCTOR_MAP.get(name);
        try {
            if (constructor == null) {
                Class<?> clazz = mContext.getClassLoader().loadClass(
                        name.indexOf('.') == -1 ? X_PREFERENCE_PACKAGE + name : name);
                constructor = clazz.getConstructor(Context.class, AttributeSet.class);
                constructor.setAccessible(true);
                CONSTRUCTOR_MAP.put(name, constructor);
            }
            Object[] args = mConstructorArgs;
            args[1] = attrs;
            return (Preference) constructor.newInstance(args);
        } catch (ClassNotFoundException e) {
            throw new InflateException(attrs.getPositionDescription() + ": Error inflating class (not found)" + name, e);
        } catch (Exception e) {
            throw new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e);
        }
    }

    @NonNull
    private PreferenceGroup onMergeRoots(PreferenceGroup givenRoot, @NonNull PreferenceGroup xmlRoot) {
        if (givenRoot == null) {
            xmlRoot.onAttachedToHierarchy(mPreferenceManager);
            return xmlRoot;
        } else {
            return givenRoot;
        }
    }

    private void inflateAll(XmlPullParser parser, Preference parent, final AttributeSet attrs) throws XmlPullParserException, IOException {
        final int depth = parser.getDepth();
        int type;
        while (((type = parser.next()) != XmlPullParser.END_TAG
                || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) continue;

            final String name = parser.getName();
            if (INTENT_TAG_NAME.equals(name)) {
                final Intent intent;

                try {
                    intent = Intent.parseIntent(mContext.getResources(), parser, attrs);
                } catch (IOException e) {
                    throw new XmlPullParserException("Error parsing preference");
                }

                parent.setIntent(intent);
            } else if (EXTRA_TAG_NAME.equals(name)) {
                mContext.getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs, parent.getExtras());
                try {
                    skipCurrentTag(parser);
                } catch (IOException e) {
                    throw new XmlPullParserException("Error parsing preference");
                }
            } else {
                final Preference item = createItemFromTag(name, attrs);
                ((PreferenceGroup) parent).addItemFromInflater(item);
                inflateAll(parser, item, attrs);
            }
        }
    }

    private void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        do type = parser.next();
        while (type != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth));
    }
}
