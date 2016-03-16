package myproject.ycc.criminalintent.javabean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by L.Y.C on 2016/3/10.
 */
public class Photo {

    private static final String JSON_FILENAME = "filename";
    private static final String JSON_DEGREE = "degree";
    private int mDegree;
    private String mFileName;

    public Photo(String fileName) {
        mFileName = fileName;
    }

    //拍摄时记录方向
    public Photo(String fileName, int degree) {
        mFileName = fileName;
        mDegree = degree;
    }

    public Photo(JSONObject json) throws JSONException {
        mFileName = json.getString(JSON_FILENAME);
        mDegree = json.getInt(JSON_DEGREE);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFileName);
        json.put(JSON_DEGREE, mDegree);
        return json;
    }

    public String getFileName() {
        return mFileName;
    }

    public int getDegree() {
        return mDegree;
    }

    public void setDegree(int degree) {
        mDegree = degree;
    }
}
