package myproject.ycc.criminalintent.javabean;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by L.Y.C on 2016/2/4.
 */
public class Crime {
    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_SOLVED = "solved";
    private static final String JSON_DATE = "date";
    private static final String JSON_PHOTO = "photo";
    private Photo mPhoto;
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm", Locale.CHINA);

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public UUID getId() {

        return mId;
    }

    @Override
    public String toString() {
        return mTitle;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Photo photo) {
        mPhoto = photo;
    }

    public Crime() {
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public Crime(JSONObject json) throws JSONException {
        mId = UUID.fromString(json.getString(JSON_ID));
        if (json.has(JSON_TITLE)) {
            mTitle = json.getString(JSON_TITLE);
        }
        mSolved = json.getBoolean(JSON_SOLVED);

        try {
            //取出来的mDate.toString()格式在各系统中可能不同
            mDate = mSimpleDateFormat.parse(json.getString(JSON_DATE));
        } catch (ParseException e) {
            e.printStackTrace();
            mDate = new Date(json.getString(JSON_DATE));
        }

        if (json.has(JSON_PHOTO)) {
            mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
        }
    }

    /**
     * 转化
     * @return 可写入JSON文件的JSONObject对象
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mId.toString());
        json.put(JSON_TITLE, mTitle);
        json.put(JSON_SOLVED, mSolved);
        //文件里存的都是yyyy年MM月dd日 E HH:mm格式
        json.put(JSON_DATE, mSimpleDateFormat.format(mDate));
        if (mPhoto != null) {
            json.put(JSON_PHOTO, mPhoto.toJSON());
        }
        return json;
    }
}
