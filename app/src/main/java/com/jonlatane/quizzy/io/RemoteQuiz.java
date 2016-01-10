package com.jonlatane.quizzy.io;

import com.google.gson.Gson;
import com.jonlatane.quizzy.model.Quiz;
import com.jonlatane.quizzy.model.QuizInstance;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Responsible for IO of quiz data to file and from network.
 *
 * Created by jonlatane on 11/23/15.
 */
public class RemoteQuiz {
    private static final String TAG = "RemoteQuiz";
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    public static final long CONNECT_TIMEOUT = 5000;
    public static final long READ_TIMEOUT = 30000;

    private String url;
    private Quiz latest;
    private List<QuizInstance> instances;

    RemoteQuiz(String url)  {
        this.url = url;
    }

    public void loadLatest() throws IOException {
        URLConnection connection = new URL(url).openConnection();
        Reader reader = new InputStreamReader(connection.getInputStream(), Charset.defaultCharset());
        latest = new Gson().fromJson(reader, Quiz.class);
    }

    public Future<Quiz> fetchLatest() {
        return getRemoteTemplate(url);
    }

    public void setLatest(Quiz latest) {
        this.latest = latest;
    }

    public QuizInstance createQuizInstance() {
        QuizInstance result = new QuizInstance(latest);
        getQuizInstances().add(result);
        return result;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Quiz getLatest() {
        return latest;
    }

    public List<QuizInstance> getQuizInstances() {
        if(instances == null) {
            instances = new LinkedList<>();
        }
        return instances;
    }

    public QuizInstance getLatestInstance() {
        QuizInstance result = null;
        for(QuizInstance instance : getQuizInstances()) {
            if(result == null || result.getStartTime().isBefore(instance.getStartTime())) {
                result = instance;
            }
        }
        return result;
    }

    public void write(Appendable writer) {
        GsonUtils.getGsonWrapper().toJson(this, writer);
    }

    public static RemoteQuiz read(Reader reader) {
        RemoteQuiz result = GsonUtils.getGsonWrapper().fromJson(reader, RemoteQuiz.class);
        return result;
    }

    public static Future<Quiz> getRemoteTemplate(final String url) {
        Future<Quiz> future;
        future = EXECUTOR_SERVICE.submit(new Callable<Quiz>() {
            @Override
            public Quiz call() throws IOException {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout((int)CONNECT_TIMEOUT);
                connection.setReadTimeout((int)READ_TIMEOUT);
                try {
                    Reader reader = new InputStreamReader(connection.getInputStream(), Charset.defaultCharset());
                    return GsonUtils.getGsonWrapper().fromJson(reader, Quiz.class);
                } catch(Throwable e) {
                    throw new IOException(e);
                } finally {
                    connection.disconnect();
                }
            }
        });

        return future;
    }
}
