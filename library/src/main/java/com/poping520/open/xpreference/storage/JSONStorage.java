package com.poping520.open.xpreference.storage;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/9/13 11:48
 */
public class JSONStorage implements Storage {

    private static final String TAG = "JSONStorage";

    private JSONObject mJSONObject;

    private File mJSONFile;

    public JSONStorage(File jsonFile) {
        mJSONFile = jsonFile;
        if (mJSONFile.exists()) {
            BufferedReader br = null;
            try {
                StringBuilder sb = new StringBuilder();
                br = new BufferedReader(new FileReader(mJSONFile));

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                mJSONObject = new JSONObject(sb.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            mJSONObject = new JSONObject();
        }
    }

    public JSONStorage(String jsonFilePath) {
        this(new File(jsonFilePath));
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
        try {
            mJSONObject.put(key, value);
            save();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean saveString(String key, String value) {
        try {
            mJSONObject.put(key, value);
            save();
            return true;
        } catch (JSONException e) {
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
        return mJSONObject.optBoolean(key, defaultValue);
    }

    @Override
    public String getString(String key, @Nullable String defaultValue) {
        return mJSONObject.optString(key, defaultValue);
    }

    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defaultValue) {
        return null;
    }

    @Override
    public boolean contains(String key) {
        return mJSONObject.has(key);
    }

    private void save() {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(mJSONFile));
            bw.write(mJSONObject.toString());
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
