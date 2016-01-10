package com.jonlatane.quizzy.model;

import com.jonlatane.quizzy.io.GsonUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jonlatane on 11/23/15.
 */
public class QuizInstance {
    private DateTime startTime;
    private Quiz quiz;
    private List<String> answerChoices;
    private List<String> correctAnswers;

    public QuizInstance(Quiz quiz) {
        this.quiz = quiz;
        this.startTime = DateTime.now();
        this.answerChoices = new ArrayList<>(
                Collections.nCopies(quiz.getQuestions().size(), (String)null)
        );
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Answer getSelectedAnswer(int questionIndex) {
        Question question = quiz.getQuestions().get(questionIndex);
        String answerChoice = answerChoices.get(questionIndex);
        if(answerChoice != null) {
            for (Answer answer : question.getAnswers()) {
                if (answer.id.toLowerCase().equals(answerChoice.toLowerCase())) {
                    return answer;
                }
            }
        }
        return null;
    }

    public void selectAnswer(Answer answer, int questionIndex) {
        this.answerChoices.set(questionIndex, answer.id);
    }

    public boolean isCorrect(int questionIndex) {
        assert(quiz.getQuestions().size() == answerChoices.size());
        if(answerChoices.get(questionIndex) != null) {
            String choice = answerChoices.get(questionIndex).toLowerCase();
            String correct = quiz.getQuestions().get(questionIndex).getCorrectAnswer().toLowerCase();
            return choice.equals(correct);
        }
        return false;
    }

    public float getScore() {
        float numberCorrect = 0;
        for(int i = 0; i < answerChoices.size(); i++) {
            if(isCorrect(i)) {
                numberCorrect += 1;
            }
        }
        return numberCorrect / answerChoices.size();
    }

    public String getPercentCorrect() {
        return Math.round(100 * getScore()) + "%";
    }

    /**
     * Used to make JSON more readable
     */
    private static class AnsweredQuestion extends Question {
        private final String selectedAnswer;
        AnsweredQuestion(Question question, String selectedAnswer) {
            super(question);
            this.selectedAnswer = selectedAnswer;
        }
    }

    public String getPrettyJson() {
        correctAnswers = new ArrayList<>();
        for(int i = 0; i < quiz.getQuestions().size(); i++) {
            correctAnswers.add(quiz.getQuestions().get(i).getCorrectAnswer());
            quiz.getQuestions().set(i,
                    new AnsweredQuestion(quiz.getQuestions().get(i), answerChoices.get(i))
            );
        }
        return GsonUtils.getPrettyPrintWrapper().toJson(this);
    }
}
