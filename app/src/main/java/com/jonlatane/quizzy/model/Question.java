package com.jonlatane.quizzy.model;

import java.util.List;

/**
 * Created by jonlatane on 11/21/15.
 */
public class Question {

    private String question;
    private List<Answer> multiple_choice;
    String answer;

    public Question(Question question) {
        this.question = question.question;
        multiple_choice = question.multiple_choice;
        answer = question.answer;
    }

    public String getQuestionText() {
        return question;
    }

    public List<Answer> getAnswers() {
        return multiple_choice;
    }

    public String getCorrectAnswer() {
        return answer;
    }
}
