package com.jonlatane.quizzy;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jonlatane.quizzy.io.EnrolledQuiz;
import com.jonlatane.quizzy.model.QuizInstance;

import org.joda.time.format.ISODateTimeFormat;

import java.io.FileNotFoundException;

public class QuizInfoActivity extends AppCompatActivity {
    public static final String TAG = "QuizInfoActivity";
    private static final String CURRENT_TAB = "CurrentTab";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private QuizInstancePagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private EnrolledQuiz enrolledQuiz;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_quiz_info);
        String quizId = getIntent().getStringExtra(QuizSelectionActivity.EXTRA_QUIZ_ID);
        enrolledQuiz = null;
        try {
            enrolledQuiz = EnrolledQuiz.openQuiz(quizId, this);
        } catch(FileNotFoundException e) {
            Log.wtf(TAG, "Should never reach this state");
            throw new RuntimeException(e);
        }
        binding.setVariable(com.jonlatane.quizzy.BR.enrolledQuiz, enrolledQuiz);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(enrolledQuiz.getLatest().getTitle());

        mSectionsPagerAdapter = new QuizInstancePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        mViewPager.setCurrentItem(inState.getInt(CURRENT_TAB));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_TAB, mViewPager.getCurrentItem());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static class EnrolledQuizReviewFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private final EnrolledQuiz enrolledQuiz;

        public EnrolledQuizReviewFragment(EnrolledQuiz enrolledQuiz) {
            this.enrolledQuiz = enrolledQuiz;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            LayoutInflater layoutInflater = LayoutInflater.from(container.getContext());
            ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_quiz_review, container, false);
            binding.setVariable(com.jonlatane.quizzy.BR.enrolledQuiz, enrolledQuiz);
            View rootView = binding.getRoot();
            return rootView;
        }
    }
    public static class QuizInstanceReviewFragment extends Fragment {
        private final EnrolledQuiz enrolledQuiz;
        private final int instanceIndex;

        public QuizInstanceReviewFragment(EnrolledQuiz enrolledQuiz, int instanceIndex) {
            this.enrolledQuiz = enrolledQuiz;
            this.instanceIndex = instanceIndex;
        }

        public QuizInstance getQuizInstance() {
            return enrolledQuiz.getQuizInstances().get(instanceIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            LayoutInflater layoutInflater = LayoutInflater.from(container.getContext());
            ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_instance_review, container, false);
            binding.setVariable(com.jonlatane.quizzy.BR.quizInstance, getQuizInstance());
            View rootView = binding.getRoot();
            return rootView;
        }
    }

    public class QuizInstancePagerAdapter extends FragmentPagerAdapter {
        public QuizInstancePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return new EnrolledQuizReviewFragment(enrolledQuiz);
            }
            return new QuizInstanceReviewFragment(enrolledQuiz, enrolledQuiz.getQuizInstances().size() - position);
        }

        @Override
        public int getCount() {
            return enrolledQuiz.getQuizInstances().size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0) {
                return getString(R.string.info_summary);
            }
            return enrolledQuiz.getQuizInstances().get(enrolledQuiz.getQuizInstances().size() - position)
                    .getStartTime().toString(ISODateTimeFormat.dateHourMinute());
        }
    }
}
