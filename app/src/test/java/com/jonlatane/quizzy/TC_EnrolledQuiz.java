package com.jonlatane.quizzy;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.jonlatane.quizzy.io.EnrolledQuiz;

import org.junit.Test;

import java.util.List;

/**
 * Created by jonlatane on 11/27/15.
 */
public class TC_EnrolledQuiz  extends AndroidTestCase {

    @Test
    public void testCreateQuiz() throws Exception {
        Context context = new MockContext();
        EnrolledQuiz q = EnrolledQuiz.addQuiz(TC_Quiz.QUIZ_URL, context);
        List<EnrolledQuiz> quizzes = EnrolledQuiz.getEnrolledQuizzes(context);

    }
}
