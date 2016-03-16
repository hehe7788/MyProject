package myproject.ycc.criminalintent;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChoosePickerFragment extends DialogFragment {
    public static final String EXTRA_DATE_TIME = "bry.criminalintent.date&time";
    private static final int REQUEST_DATE = 101;
    private static final String DIALOG_DATE = "date";
    private static final String DIALOG_TIME = "time";
    private static final String TAG = "ChoosePickerFragment";
    private static final int REQUEST_TIME = 110;
    private Date mDate;
    private Button mDateButton;
    private Button mTimeButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDate = (Date) getArguments().getSerializable(EXTRA_DATE_TIME);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_picker,  null);

        mDateButton = (Button) v.findViewById(R.id.dialog_date_button);
        mTimeButton = (Button) v.findViewById(R.id.dialog_time_button);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //启动DatePickerFragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dateDialog = DatePickerFragment.newInstance(mDate);
                dateDialog.setTargetFragment(ChoosePickerFragment.this, REQUEST_DATE);
                dateDialog.show(fm, DIALOG_DATE);
                Log.e(TAG, "启动DatePicKerFragment");
            }
        });
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start TimePickerFragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TimePickerFragment dateDialog = TimePickerFragment.newInstance(mDate);
                dateDialog.setTargetFragment(ChoosePickerFragment.this, REQUEST_TIME);
                dateDialog.show(fm, DIALOG_TIME);
                Log.e(TAG, "启动TimePickerFragment");
            }
        });


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ChoosePickerFragment", ": ");
        Log.e("onActivityResult", "request " + requestCode + "result " + resultCode);
        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "resultCode != Activity.RESULT_OK");
            return;
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (requestCode == REQUEST_DATE) {
            Log.e(TAG, "Dialog date back");
            // 2016/2/20 把子fragment得到的结果交给CrimeFragment
            mDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            sendResult(Activity.RESULT_OK);
            fm.beginTransaction()
                    .remove(this)
                    .commit();
        }
        if (requestCode == REQUEST_TIME) {
            Log.e(TAG, "Dialog time back");
            //  2016/2/20 把子fragment得到的结果交给CrimeFragment
            mDate = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            sendResult(Activity.RESULT_OK);
            fm.beginTransaction()
                    .remove(this)
                    .commit();
        }
    }

    public static ChoosePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_DATE_TIME, date);

        ChoosePickerFragment fragment = new ChoosePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE_TIME, mDate);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
        Log.e("ChoosePickerFragment", "sendResult ");
    }
}
