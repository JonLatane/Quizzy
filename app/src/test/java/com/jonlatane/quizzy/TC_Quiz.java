package com.jonlatane.quizzy;

import com.google.repacked.apache.commons.io.IOUtils;

import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

import com.jonlatane.quizzy.model.Quiz;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TC_Quiz {
    public static final String QUIZ_URL = "https://docs.google.com/document/u/0/export?format=txt&id=1MV7GHAvv4tgj98Hj6B_WZdeeEu7CRf1GwOfISjP4GT0";
    public String loadQuizData() throws Exception {
        URLConnection connection = new URL(QUIZ_URL).openConnection();
        InputStream in = connection.getInputStream();
        return IOUtils.toString(in, Charset.defaultCharset());
    }

    @Test
    public void readQuiz() throws Exception {
        String quizJson = loadQuizData();
        System.out.println(quizJson);

        Quiz q = Quiz.fromJSON(quizJson);
        System.out.println(q.toJSON());
        JSONObject fromData = new JSONObject(quizJson);
        JSONObject fromModel = new JSONObject(q.toJSON());


        JSONAssert.assertEquals(fromData, fromModel, true);

    }
}