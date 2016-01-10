package com.jonlatane.quizzy.model;

import java.util.List;

import com.jonlatane.quizzy.io.GsonUtils;

/**
 * Created by jonlatane on 11/21/15.
 */
public class Quiz implements Cloneable {
    private String title;
    private List<Question> questions;

    public String getTitle() {
        return title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public String toJSON() {
        return GsonUtils.getGsonWrapper().toJson(this);
    }

    public static Quiz fromJSON(String json) {
        return GsonUtils.getGsonWrapper().fromJson(json, Quiz.class);
    }

    public Quiz clone() {
        return fromJSON(toJSON());
    }

    public String toString() {
        return toJSON();
    }
}
