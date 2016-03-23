package myproject.ycc.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import myproject.ycc.criminalintent.javabean.Crime;
import myproject.ycc.criminalintent.javabean.CrimeLab;
import myproject.ycc.criminalintent.javabean.Photo;
import myproject.ycc.criminalintent.javabean.PictureUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class CrimeFragment extends Fragment {
    private static final String TAG = "CrimeFragment.Log";
    public static final String EXTRA_CRIME_ID = "byr.criminalintent.crime_id";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_DATE_TIME = 11;
    private static final String DIALOG_DATE_TIME = "date&time";
    private static final int REQUEST_PHOTO = 12;
    private static final String DIALOG_IMAGE = "image";
    private static final int REQUEST_CONTACT = 13;
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    //"yyyy年MM月dd日 E HH:mm"
    private SimpleDateFormat sd = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm", Locale.CHINA);
    private Button mDateTimeButton;

    private UUID mCrimeId;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private String mExternalStoragePath = "/criminal_intent_camera";
    private int screenOrientation;
    private Button mSuspectButton;
    private Button mDialButton;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks= (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCrimeId = (UUID)getArguments().getSerializable(EXTRA_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(mCrimeId);
        setHasOptionsMenu(true);
        //getPixelDisplayMetricsII();
    }

    private void getPixelDisplayMetricsII() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        float density = dm.density;
        int densityDpi = dm.densityDpi;

        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;

        Log.e(TAG ,"宽:" + screenWidth + ", 高:"+screenHeight);
        Log.e(TAG, "密度 density:" + density + ",densityDpi:" + densityDpi);
        Log.e(TAG, "精确密度 xdpi:" + xdpi + ", ydpi:" + ydpi);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment_crime_list for this fragment
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                mCallbacks.onCrimeUpdated(mCrime);
                getActivity().setTitle(mCrime.getTitle());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateTimeButton = (Button) v.findViewById(R.id.crime_date_time);
        updateDate();

        mDateTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                ChoosePickerFragment dialog = ChoosePickerFragment.newInstance(mCrime.getDate());
                //设置本fragment为要启动的ChoosePickerFragment的目标fragment，requestCode为REQUEST_DATE_TIME
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE_TIME);
                dialog.show(fm, DIALOG_DATE_TIME);
            }
        });
        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                mCallbacks.onCrimeUpdated(mCrime);
            }
        });
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_imageButton);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
                startActivityForResult(i, REQUEST_PHOTO);
            }
        });
        //查询是否有相机
        PackageManager pm = getActivity().getPackageManager();
        boolean hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD ||
                android.hardware.Camera.getNumberOfCameras() > 0;
        if (!hasCamera) {
            mPhotoButton.setEnabled(false);
        }

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(TAG, "landscape");
            screenOrientation = 2;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.e(TAG, "portrait");
            screenOrientation = 1;
        }

        mPhotoView = (ImageView) v.findViewById(R.id.crime_imageView);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo p = mCrime.getPhoto();
                if (p == null) {
                    return;
                }

                FragmentManager fm = getActivity().getSupportFragmentManager();
                String path = Environment.getExternalStorageDirectory() + mExternalStoragePath + "/" + p.getFileName();
                int degree = p.getDegree();
                ImageFragment.newInstance(path, degree, screenOrientation).show(fm, DIALOG_IMAGE);
            }
        });

        Button reportButton = (Button) v.findViewById(R.id.crime_reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("test/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i= Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspectButton);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, REQUEST_CONTACT);

            }
        });
        mDialButton = (Button) v.findViewById(R.id.crime_dialButton);
        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:10086");
                Intent i = new Intent(Intent.ACTION_DIAL, number);
                startActivity(i);
            }
        });
        return v;
    }

    private void showPhoto() {
        //显示缩略图
        Photo photo = mCrime.getPhoto();
        int degree = 0;
        BitmapDrawable bitmapDrawable = null;
        if (photo != null) {
            String path = Environment.getExternalStorageDirectory() +  mExternalStoragePath + "/" + photo.getFileName();
            degree = photo.getDegree();

            Log.e(TAG, "showPhoto " + path + " rotate " + degree);
            bitmapDrawable = PictureUtils.getScaledDrawable(getActivity(), path);
        }
        mPhotoView.setImageDrawable(bitmapDrawable);
        mPhotoView.setRotation(degree);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.crime_list_item_context, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity())!=null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;
            case R.id.menu_item_delete:
                Log.e(TAG, "delete");
                // startActivity
                Intent i = new Intent(getActivity(), CrimeListActivity.class);
                //在回退栈中寻找指定的activity实例，如果找到则弹出其他所有，让启动的activity在栈顶
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("delete.id", mCrimeId);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "request " + requestCode + "result " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            Log.e(TAG, date.toString());
            Log.e(TAG, mCrime.getDate().toString());
            mDateButton.setText(sd.format(mCrime.getDate()));
        } else if (requestCode == REQUEST_DATE_TIME) {
            //收到了ChoosePickerFragment返回的消息
            Date date = (Date) data.getSerializableExtra(ChoosePickerFragment.EXTRA_DATE_TIME);
            mCrime.setDate(date);
            updateDate();
            mCallbacks.onCrimeUpdated(mCrime);
        } else if (requestCode == REQUEST_PHOTO) {
            //收到了CrimeCameraFragment返回的消息
            String fileName = data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
            int degree = data.getIntExtra(CrimeCameraFragment.EXTRA_PHOTO_DEGREE, 0);
            if (fileName != null) {
                Photo photo = new Photo(fileName, degree);
                Log.e(TAG, "NEW photo: " + degree + "  "+ fileName);
                mCrime.setPhoto(photo);
                Log.e(TAG, "get degree" + mCrime.getPhoto().getDegree());
                showPhoto();
                mCallbacks.onCrimeUpdated(mCrime);
            }
        } else if (requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();

            String[] queryFields = new String[] {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};

            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);

            if (c.getCount() == 0) {
                c.close();
                return;
            }
            c.moveToFirst();
            Log.e(TAG, c.getString(0) + "  " + c.getString(1));
            String suspect = c.getString(0);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);
            mCallbacks.onCrimeUpdated(mCrime);
            c.close();
        }
    }
    public void updateDate() {
        mDateTimeButton.setText(sd.format(mCrime.getDate()));
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        //需要在onPause这里保存！
        CrimeLab.get(getActivity()).saveCrimes();
        Log.e(TAG, "onPause save" + mCrime.getTitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume " + mCrime.getTitle());
    }

    @Override
    public void onStart() {
        super.onStart();
        //加载缩略图
        showPhoto();
    }

    @Override
    public void onStop() {
        super.onStop();
        //卸载缩略图
        if (mCrime.getPhoto() != null) {
            PictureUtils.cleanImageView(mPhotoView);
        }
    }
}
