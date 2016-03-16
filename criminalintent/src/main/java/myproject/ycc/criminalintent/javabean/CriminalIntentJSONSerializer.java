package myproject.ycc.criminalintent.javabean;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * 创建和解析JSON
 * Created by L.Y.C on 2016/2/25.
 */
public class CriminalIntentJSONSerializer {

    private static final String TAG = "JSONSerializer";
    private Context mContext;
    private String mFilename;
    private String mExternalStoragePath = "/criminal_intent_files";

    public CriminalIntentJSONSerializer(Context context, String filename) {
        mContext = context;
        mFilename = filename;
    }

    public ArrayList<Crime> loadCrimesFromExternalStorage() throws IOException, JSONException {
        ArrayList<Crime> crimes = new ArrayList<>();
        BufferedReader reader = null;
        try {
            Log.e(TAG, String.valueOf(Environment.isExternalStorageEmulated()));
            File sdCardDictionary = Environment.getExternalStorageDirectory();
            File sdCardFile = new File(sdCardDictionary + mExternalStoragePath + "/" + mFilename);
            Log.e(TAG, sdCardFile.toString());
            FileInputStream in = new FileInputStream(sdCardFile);

            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //省略换行
                jsonString.append(line);
            }
            //Parses a JSON  encoded string into the corresponding object
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            //即时出错，也能完成底层文件句柄的释放
            if (reader != null) {
                reader.close();
            }
        }
        return crimes;
    }

    public ArrayList<Crime> loadCrimes() throws IOException, JSONException {
        ArrayList<Crime> crimes = new ArrayList<>();
        BufferedReader reader = null;
        try {
            InputStream in = mContext.openFileInput(mFilename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                //省略换行
                jsonString.append(line);
            }
            //Parses a JSON  encoded string into the corresponding object
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            //即时出错，也能完成底层文件句柄的释放
            if (reader != null) {
                reader.close();
            }
        }
        return crimes;
    }

    public void saveCrimesInExternalStorage(ArrayList<Crime> crimes) throws JSONException, IOException {
        //Build an array in JSON
        JSONArray array = new JSONArray();
        //每次都是写全部的数据，不管单个的Crime有没有更新 TODO 只更新修改了的Crime
        for (Crime c : crimes) {
            array.put(c.toJSON());
        }

        //外部存储 获取外部存储设备（SD卡）的路径
        File sdCardDictionary = Environment.getExternalStorageDirectory();
        File sdCardFile = new File(sdCardDictionary + mExternalStoragePath + "/" + mFilename);
        //如果文件不存在，则创建目录
        if (!sdCardFile.exists()) {
            sdCardFile.getParentFile().mkdirs();
        }

        Writer sdWriter = null;
        try {
            FileOutputStream sdOut = new FileOutputStream(sdCardFile);
            sdWriter = new OutputStreamWriter(sdOut);
            sdWriter.write(array.toString());
        } finally {
            if (sdWriter != null) {
                sdWriter.close();
            }
        }
    }

    public void saveCrimes(ArrayList<Crime> crimes) throws JSONException, IOException {
        //Build an array in JSON
        JSONArray array = new JSONArray();
        //每次都是写全部的数据，不管单个的Crime有没有更新 TODO 只更新修改了的Crime
        for (Crime c : crimes) {
            array.put(c.toJSON());
        }
        //内部存储Write the file to disk, mContext.openFileOutput打开文件并写入数据
        Writer writer = null;
        try {
            //mContext.openFileOutput获取OutputStream对象
            OutputStream out = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
            //用OutputStream对象创建一个新的OutputStreamWriter
            writer = new OutputStreamWriter(out);
            //调用OutputStreamWriter的写方法
            writer.write(array.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
