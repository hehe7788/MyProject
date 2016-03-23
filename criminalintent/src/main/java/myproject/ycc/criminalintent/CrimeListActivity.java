package myproject.ycc.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import myproject.ycc.criminalintent.javabean.Crime;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.acitivity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        //如果使用的单栏布局，启动CrimePagerActivity
        if (findViewById(R.id.detailFragmentContainer) == null) {
            Intent i = new Intent(CrimeListActivity.this, CrimePagerActivity.class);
            i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
            startActivity(i);
        } else {
            //若果使用的两栏布局，更新右侧的fragment，将CrimeFragment放入detailFragmentContainer
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction =  fm.beginTransaction();

            Fragment oldDetail = fm.findFragmentById(R.id.detailFragmentContainer);
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());

            if (oldDetail != null) {
                transaction.remove(oldDetail);
            }
            transaction.add(R.id.detailFragmentContainer, newDetail);
            transaction.commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        FragmentManager fm = getSupportFragmentManager();
        CrimeListFragment listFragment = (CrimeListFragment)fm.findFragmentById(R.id.fragmentContainer);
        listFragment.updateUI();
    }
}
