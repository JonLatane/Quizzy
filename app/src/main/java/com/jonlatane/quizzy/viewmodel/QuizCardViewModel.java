package com.jonlatane.quizzy.viewmodel;

import android.content.Context;
import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.alexvasilkov.android.commons.texts.SimpleTextWatcher;
import com.jonlatane.quizzy.BR;
import com.jonlatane.quizzy.QuizInfoActivity;
import com.jonlatane.quizzy.QuizSelectionActivity;
import com.jonlatane.quizzy.R;
import com.jonlatane.quizzy.TakeQuizActivity;
import com.jonlatane.quizzy.io.EnrolledQuiz;
import com.jonlatane.quizzy.io.RemoteQuiz;
import com.jonlatane.quizzy.model.Quiz;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by jonlatane on 11/24/15.
 */
public class QuizCardViewModel extends BaseObservable {
    private static final String TAG = "QuizCardViewModel";
    private static final long URL_FETCH_DELAY = 1000;
    private final Context context;
    private final EnrolledQuiz enrolledQuiz;
    private Integer error = null;
    private Future<Quiz> currentFuture;
    private boolean loading;

    public QuizCardViewModel(EnrolledQuiz quiz, Context context) {
        this.enrolledQuiz = quiz;
        this.context = context;
        this.loading = false;
    }

    @Bindable
    public String getTitle() {
        Quiz latest = enrolledQuiz.getLatest();
        if(latest != null && latest.getTitle() != null) {
            return latest.getTitle();
        }
        return context.getString(R.string.new_quiz);
    }

    @Bindable
    public String getUrl() {
        return enrolledQuiz.getUrl();
    }

    @Bindable
    public TextWatcher getUrlWatcher() {
        return new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence url, int i1, int i2, int i3) {
                error = null;
                try {
                    new URL(url.toString());
                } catch(MalformedURLException e) {
                    if(StringUtils.isBlank(url)) {
                        error = R.string.err_quiz_url_empty;
                    } else {
                        error = R.string.err_quiz_url_invalid;
                    }
                    notifyPropertyChanged(BR.urlError);
                }
                notifyPropertyChanged(BR.urlError);
            }
        };
    }

    public void onUrlFocusChange(final View view, boolean b) {
        notifyPropertyChanged(BR.url);
    }


    public boolean onUrlEditorAction(final TextView urlInput, final int actionId, final KeyEvent keyEvent) {
        urlInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });
        if (actionId == EditorInfo.IME_ACTION_DONE && error == null) {
            InputMethodManager imm = (InputMethodManager)urlInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
            setUrlAsync(urlInput);
            return true;
        }
        return false;
    }

    private void setUrlAsync(final TextView urlInput) {
        final String url = urlInput.getText().toString();
        if(currentFuture != null) {
            currentFuture.cancel(true);
            currentFuture = null;
        }
        error = null;
        if(!loading) {
            loading = true;
            notifyPropertyChanged(BR.loading);
        }

        final Future<Quiz> myFuture = currentFuture = RemoteQuiz.getRemoteTemplate(url);

        new AsyncTask<Void, Void, Quiz>() {
            @Override
            public Quiz doInBackground(Void... args) {
                try {
                    return myFuture.get(RemoteQuiz.CONNECT_TIMEOUT, TimeUnit.SECONDS);
                } catch(InterruptedException e) {
                    return null;
                } catch(TimeoutException e) {
                    if(myFuture == currentFuture) {
                        error = R.string.err_connection_timeout;
                    }
                } catch(Throwable t) {
                    if(myFuture == currentFuture) {
                        Log.e(TAG, "failure fetcing quiz", t);
                        error = R.string.err_loading_quiz;
                    }
                }
                return null;
            }

            @Override
            public void onPostExecute(Quiz result) {
                if(result != null && myFuture == currentFuture) {
                    enrolledQuiz.setUrl(url);
                    enrolledQuiz.setLatest(result);
                    try {
                        enrolledQuiz.save(context);
                    } catch (IOException e) {
                        Log.wtf(TAG, "failed to save quiz");
                        throw new RuntimeException(e);
                    }
                    notifyPropertyChanged(BR.title);
                    notifyPropertyChanged(BR.takeable);
                }
                if(myFuture == currentFuture) {
                    loading = false;
                    notifyPropertyChanged(BR.loading);
                    if(error != null) {
                        notifyPropertyChanged(BR.urlError);
                    }
                }
                urlInput.requestLayout();
            }
        }.execute();
    }

    @Bindable
    public String getUrlError() {
        if(error != null) {
            return context.getString(error.intValue());
        }
        return null;
    }

    @Bindable
    public boolean isLoading() {
        return loading;
    }

    @Bindable
    public boolean isTakeable() {
        return enrolledQuiz.getLatest() != null && enrolledQuiz.getLatest().getQuestions().size() > 0;
    }

    @Bindable
    public boolean isReviewable() {
        return !enrolledQuiz.getQuizInstances().isEmpty();
    }

    public void takeQuiz(View view) {
        Intent takeQuizIntent = new Intent(context, TakeQuizActivity.class);
        takeQuizIntent.putExtra(QuizSelectionActivity.EXTRA_QUIZ_ID, enrolledQuiz.getQuizId());
        enrolledQuiz.createQuizInstance();
        try {
            enrolledQuiz.save(context);
        } catch(IOException e) {
            Log.wtf(TAG, e);
            throw new RuntimeException(e);
        }
        context.startActivity(takeQuizIntent);
    }
    public void reviewQuiz(View view) {
        Intent intent = new Intent(context, QuizInfoActivity.class);
        intent.putExtra(QuizSelectionActivity.EXTRA_QUIZ_ID, enrolledQuiz.getQuizId());
        context.startActivity(intent);
    }

    public void deleteQuiz(View view) {
        Snackbar.make(view, R.string.confirm_delete, Snackbar.LENGTH_LONG)
                .setAction(R.string.confirm_delete_yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (context instanceof QuizSelectionActivity) {
                            ((QuizSelectionActivity) context).deleteQuiz(enrolledQuiz.getQuizId());
                        } else {
                            enrolledQuiz.delete(context);
                        }
                    }
                })
                .show();
    }
}
