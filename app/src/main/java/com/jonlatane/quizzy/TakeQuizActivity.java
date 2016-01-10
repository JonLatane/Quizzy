package com.jonlatane.quizzy;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jonlatane.quizzy.io.EnrolledQuiz;
import com.jonlatane.quizzy.model.Answer;
import com.jonlatane.quizzy.model.Question;
import com.jonlatane.quizzy.model.QuizInstance;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TakeQuizActivity extends AppCompatActivity {
    public static final String TAG = "TakeQuizActivity";
    private static final String CURRENT_TAB = "CurrentTab";
    public static final int TIME_LIMIT_SECONDS = 60;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private QuestionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private EnrolledQuiz enrolledQuiz;
    private QuizInstance quizInstance;
    private int currentQuestion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_take_quiz);
        String quizId = getIntent().getStringExtra(QuizSelectionActivity.EXTRA_QUIZ_ID);
        enrolledQuiz = null;
        try {
            enrolledQuiz = EnrolledQuiz.openQuiz(quizId, this);
        } catch(FileNotFoundException e) {
            Log.wtf(TAG, "Should never reach this state");
            throw new RuntimeException(e);
        }
        quizInstance = enrolledQuiz.getLatestInstance();
        binding.setVariable(com.jonlatane.quizzy.BR.quizInstance, quizInstance);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(quizInstance.getQuiz().getTitle());
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new QuestionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.confirm_done, Snackbar.LENGTH_LONG)
                        .setAction(R.string.confirm_done_yes, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finishAndOpenReview();
                            }
                        }).show();
            }
        });

        //Start the countdown timer
        final TextView countdown = (TextView)findViewById(R.id.countdown);
        long timeRemaining = quizInstance.getStartTime().getMillis()
                                + (TIME_LIMIT_SECONDS * 1000L)
                                - System.currentTimeMillis();
        new CountDownTimer(timeRemaining, 1000) {

            public void onTick(long millisUntilFinished) {
                countdown.setText(millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                finishAndOpenReview();
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        //do nothing
        Snackbar.make(mViewPager, R.string.confirm_done, Snackbar.LENGTH_LONG)
                .setAction(R.string.confirm_done_yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finishAndOpenReview();
                    }
                }).show();
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
        saveQuiz();
    }

    protected void saveQuiz() {
        try {
            enrolledQuiz.save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void finishAndOpenReview() {
        saveQuiz();
        Intent intent = new Intent(this, QuizInfoActivity.class);
        intent.putExtra(QuizSelectionActivity.EXTRA_QUIZ_ID, enrolledQuiz.getQuizId());
        startActivity(intent);
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class TakeQuizQuestionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private final QuizInstance quizInstance;
        private final int questionIndex;
        //private final Question question;

        public TakeQuizQuestionFragment(QuizInstance quizInstance, int questionIndex) {
            this.quizInstance = quizInstance;
            this.questionIndex = questionIndex;
        }

        public Question getQuestion() {
            return this.quizInstance.getQuiz().getQuestions().get(questionIndex);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_question, container, false);
            ((TextView)rootView.findViewById(R.id.question_text)).setText(
                    getQuestion().getQuestionText()
            );
            RadioGroup radioGroup = ((RadioGroup)rootView.findViewById(R.id.question_selections));
            for(final Answer answer : getQuestion().getAnswers()) {
                RadioButton rb = new RadioButton(getContext());
                rb.setText(answer.answer);
                if(quizInstance.getSelectedAnswer(questionIndex) == answer) {
                    rb.setChecked(true);
                } else {
                    rb.setChecked(false);
                }
                if(quizInstance.getStartTime().plusSeconds(TIME_LIMIT_SECONDS).isBeforeNow()) {
                    rb.setClickable(false);
                } else {
                    rb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            quizInstance.selectAnswer(answer, questionIndex);
                        }
                    });
                }
                radioGroup.addView(rb);
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class QuestionsPagerAdapter extends FragmentPagerAdapter {

        public QuestionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return new TakeQuizQuestionFragment(quizInstance, position);
        }

        @Override
        public int getCount() {
            return quizInstance.getQuiz().getQuestions().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getString(R.string.question_tab_title, position + 1);
        }
    }
}
