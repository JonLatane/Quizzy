package com.jonlatane.quizzy;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.jonlatane.quizzy.adapter.QuizListAdapter;
import com.jonlatane.quizzy.io.EnrolledQuiz;

import java.io.IOException;

public class QuizSelectionActivity extends AppCompatActivity {
    public static final String TAG = "QuizSelectionActivity";
    public static final String EXTRA_QUIZ_ID = "QuizId";

    RecyclerView quizList;
    QuizListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_selection);
        quizList = (RecyclerView)findViewById(R.id.remoteQuizList);
        quizList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizListAdapter(EnrolledQuiz.getEnrolledQuizzes(this), R.layout.list_item_remotequiz);
        quizList.setAdapter(adapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    adapter.addQuiz(QuizSelectionActivity.this);
                } catch(IOException e) {
                    Log.wtf(TAG, "Something went very wrong adding a quiz");
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void deleteQuiz(String quizId) {
        adapter.deleteQuiz(quizId, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.updateAndNotify(this);
        if(adapter.getItemCount() == 0) {
            Snackbar.make(quizList, getText(R.string.hint_add_quiz), Snackbar.LENGTH_LONG).show();
        }
    }
}
