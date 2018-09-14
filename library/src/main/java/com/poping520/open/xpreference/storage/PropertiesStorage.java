package com.poping520.open.xpreference.storage;

import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Set;

/**
 * Created by WangKZ on 18/09/14.
 *
 * @author poping520
 * @version 1.0.0
 */
public class PropertiesStorage implements Storage {

    private File mFile;
    private Properties mProp;

    public PropertiesStorage(File propFile) {
        mFile = propFile;
        mProp = new Properties();
        if (!propFile.getParentFile().exists()) {
            propFile.getParentFile().mkdirs();
            try {
                propFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            InputStream is = new FileInputStream(propFile);
            mProp.load(new InputStreamReader(is, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PropertiesStorage(String propFilePath) {
        this(new File(propFilePath));
    }

    @Override
    public boolean saveInt(String key, int value) {
        return false;
    }

    @Override
    public boolean saveLong(String key, long value) {
        return false;
    }

    @Override
    public boolean saveFloat(String key, float value) {
        return false;
    }

    @Override
    public boolean saveBoolean(String key, boolean value) {
        return false;
    }

    @Override
    public boolean saveString(String key, String value) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(mFile), "UTF-8");
            mProp.setProperty(key, value);
            mProp.store(osw, null);
            osw.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean saveStringSet(String key, Set<String> value) {
        return false;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return 0;
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return 0;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return 0;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return false;
    }

    @Override
    public String getString(String key, @Nullable String defaultValue) {
        return mProp.getProperty(key, defaultValue);
    }

    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defaultValue) {
        return null;
    }

    @Override
    public boolean contains(String key) {
        return mProp.containsKey(key);
    }
}
