package com.jonlatane.quizzy.io;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.jonlatane.quizzy.model.Quiz;
import com.jonlatane.quizzy.model.QuizInstance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Encapsulation of RemoteQuizzes with an ID; responsible for storing/generating Quiz IDs for
 * the storage layer and choosing where to save quizzes to.
 *
 * Created by jonlatane on 11/23/15.
 */
public class EnrolledQuiz {
    private static final String TAG = "EnrolledQuiz";
    private static final String SCHEMA = TAG;
    private static final String REMOTEQUIZZES = "RemoteQuizzes";

    private final String quizId;
    private final RemoteQuiz remoteQuiz;

    private EnrolledQuiz(String quizId, RemoteQuiz quiz) {
        this.quizId = quizId;
        this.remoteQuiz = quiz;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getUrl() {
        return remoteQuiz.getUrl();
    }

    public Future<Quiz> fetchLatest() {
        return remoteQuiz.fetchLatest();
    }

    public void setLatest(Quiz latest) {
        remoteQuiz.setLatest(latest);
    }

    public void loadLatest(Context context) throws IOException {
        remoteQuiz.loadLatest();
    }

    public void setUrl(String url) {
        remoteQuiz.setUrl(url);
    }

    public Quiz getLatest() {
        return remoteQuiz.getLatest();
    }

    public List<QuizInstance> getQuizInstances() {
        return remoteQuiz.getQuizInstances();
    }

    public QuizInstance createQuizInstance() {
        return remoteQuiz.createQuizInstance();
    }

    public QuizInstance getLatestInstance() {
        return remoteQuiz.getLatestInstance();
    }

    public String getInstanceCount() {
        return String.valueOf(getQuizInstances().size());
    }

    public String getAverage() {
        float total = 0;
        for(QuizInstance instance : getQuizInstances()) {
            total += instance.getScore();
        }
        return Math.round(100 * total / getQuizInstances().size()) + "%";
    }

    public void save(Context context) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    context.openFileOutput(getDataFileName(), Context.MODE_PRIVATE)
            ));
            remoteQuiz.write(out);
        } catch(IOException e) {
            Log.wtf(TAG, "The docs say the file will be created, and a UUID should always be a safe name");
            throw e;
        } finally {
            if(out != null) {
                out.close();
            }
        }
    }

    public static EnrolledQuiz addQuiz(String url, Context context) throws IOException {
        SharedPreferences.Editor editor = getSchema(Context.MODE_PRIVATE, context).edit();
        RemoteQuiz remoteQuiz = new RemoteQuiz(url);
        String quizId;
        LinkedHashSet<String> quizIds = new LinkedHashSet<>(getEnrolledQuizIds(context));
        while(quizIds.contains(quizId = UUID.randomUUID().toString())) {
            //generated remoteQuiz id is a dupe, get a different one
        }
        quizIds.add(quizId);
        EnrolledQuiz enrolledQuiz = new EnrolledQuiz(quizId, remoteQuiz);
        enrolledQuiz.save(context);
        editor.putString(REMOTEQUIZZES, GsonUtils.getGsonWrapper().toJson(quizIds));
        editor.commit();

        return enrolledQuiz;
    }

    public void delete(Context context) {
        List<String> quizIds = getEnrolledQuizIds(context);
        quizIds.remove(quizId);
        SharedPreferences.Editor editor = getSchema(Context.MODE_PRIVATE, context).edit();
        editor.putString(REMOTEQUIZZES, GsonUtils.getGsonWrapper().toJson(quizIds));
        editor.commit();
        context.deleteFile(getDataFileName());
    }

    private String getDataFileName() {
        return nameDataFile(quizId);
    }

    /**
     * Gets all quizzes and cleans things up as appropriate.
     * @param context
     * @return
     * @throws FileNotFoundException
     */
    public static List<EnrolledQuiz> getEnrolledQuizzes(Context context) {
        List<String> quizIds = getEnrolledQuizIds(context);
        List<EnrolledQuiz> result = new ArrayList<>(quizIds.size());
        for(String quizId : quizIds) {
            try {
                result.add(openQuiz(quizId, context));
            } catch(FileNotFoundException e) {
                Log.wtf(TAG, "Orphan quizId found");
            }
        }
        return result;
    }

    private static List<String> getEnrolledQuizIds(Context context) {
        SharedPreferences enrolledQuizzes = getSchema(Context.MODE_PRIVATE, context);
        String quizIdsJson = enrolledQuizzes.getString(REMOTEQUIZZES, "[]");

        return new LinkedList<>(Arrays.asList(
                GsonUtils.getGsonWrapper().fromJson(quizIdsJson, String[].class)
        ));
    }

    public static EnrolledQuiz openQuiz(String quizId, Context context) throws FileNotFoundException {
        Reader reader = new BufferedReader(new InputStreamReader(
                context.openFileInput(nameDataFile(quizId))
        ));
        RemoteQuiz remoteQuiz = RemoteQuiz.read(reader);
        return new EnrolledQuiz(quizId, remoteQuiz);
    }

    private static final String nameDataFile(String quizId) {
        return SCHEMA + "-" + quizId;
    }

    private static final SharedPreferences getSchema(int mode, Context context) {
        return context.getSharedPreferences(SCHEMA, mode);
    }
}
