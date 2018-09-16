package com.poping520.open.xpreference.storage;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by WangKZ on 18/09/14.
 *
 * @author poping520
 * @version 1.0.0
 */
public class PropertiesStorage implements Storage {

    private static final String CHARSET_NAME = "UTF-8";
    private static final String SEPARATE = "$#$";

    private File mFile;
    private Properties mProp;

    public PropertiesStorage(File propFile) {
        mFile = propFile;
        mProp = new Properties();

        if (!mFile.exists()) {
            createNewPropFile(mFile);
        }

        try {
            InputStreamReader isr = new InputStreamReader(
                    new FileInputStream(propFile), CHARSET_NAME);
            mProp.load(isr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PropertiesStorage(String propFilePath) {
        this(new File(propFilePath));
    }

    @Override
    public boolean saveInt(String key, int value) {
        return save(key, value);
    }

    @Override
    public boolean saveLong(String key, long value) {
        return save(key, value);
    }

    @Override
    public boolean saveFloat(String key, float value) {
        return save(key, value);
    }

    @Override
    public boolean saveBoolean(String key, boolean value) {
        return save(key, value);
    }

    @Override
    public boolean saveString(String key, String value) {
        return save(key, value);
    }

    @Override
    public boolean saveStringSet(String key, Set<String> value) {
        return save(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, defaultValue));
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return Long.parseLong(get(key, defaultValue));
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return Float.parseFloat(get(key, defaultValue));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, defaultValue));
    }

    @Override
    public String getString(String key, @Nullable String defaultValue) {
        return mProp.getProperty(key, defaultValue);
    }

    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defaultValue) {
        String str = mProp.getProperty(key);
        if (TextUtils.isEmpty(str)) return defaultValue;
        return new HashSet<>(Arrays.asList(str.split(SEPARATE)));
    }

    @Override
    public boolean contains(String key) {
        return mProp.containsKey(key);
    }

    private String get(String key, Object defaultValue) {
        return mProp.getProperty(key, String.valueOf(defaultValue));
    }

    private boolean save(String key, Object value) {
        if (value == null) return false;
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(mFile), CHARSET_NAME);
            if (value instanceof Set) {
                //String join
                StringBuilder sb = new StringBuilder();
                Set set = (Set) value;
                for (Object obj : set) {
                    sb.append(obj).append(SEPARATE);
                }
                sb.delete(sb.length() - SEPARATE.length(), sb.length());
                mProp.setProperty(key, sb.toString());
            } else {
                mProp.setProperty(key, String.valueOf(value));
            }
            mProp.store(osw, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * if you want to create the prop file with your own way
     * please override this method
     */
    protected void createNewPropFile(File file) {
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs())
                throw new StorageException("create properties file's parent dir fail");
        }
        try {
            if (!file.createNewFile()) throw new StorageException("create properties file fail");
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }
}
