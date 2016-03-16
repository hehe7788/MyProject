package myproject.ycc.criminalintent.javabean;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by L.Y.C on 2016/2/15.
 */
public class CrimeLab {
    private static final String TAG = "CrimeLab";
    private static final String FILENAME = "crimes.json";
    private ArrayList<Crime> mCrimes;

    private static CrimeLab sCrimeLab;
    private Context mAppContext;
    private CriminalIntentJSONSerializer mSerializer;

    /**
     * CrimeLab构造方法接受Context实例，完成数据的加载，可复用
     * @param appContext Context类
     */
    private CrimeLab(Context appContext) {
        mAppContext = appContext;
        mSerializer = new CriminalIntentJSONSerializer(mAppContext, FILENAME);

        try {
//            mCrimes = mSerializer.loadCrimes();
            mCrimes = mSerializer.loadCrimesFromExternalStorage();
        } catch (Exception e) {
            mCrimes = new ArrayList<>();
            Log.e(TAG, "Error loading crimes: ", e);
        }
    }

    public static CrimeLab get(Context c) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(c.getApplicationContext());
        }
        return sCrimeLab;
    }

    public ArrayList<Crime> getCrimes() {
        return mCrimes;
    }
    public Crime getCrime(UUID id) {
        for (Crime c : mCrimes) {
            if(c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    public void deleteCrime(Crime c) {
        mCrimes.remove(c);
        //// TODO: 2016/3/14 删除存储的照片
    }
    /**
     * 数据的保存
     * @return 是否成功
     */
    public boolean saveCrimes() {
        try {
//            mSerializer.saveCrimes(mCrimes);
            mSerializer.saveCrimesInExternalStorage(mCrimes);
            Log.e(TAG, "crimes saved to file");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving crimes: " + e);
            return false;
        }
    }
}
