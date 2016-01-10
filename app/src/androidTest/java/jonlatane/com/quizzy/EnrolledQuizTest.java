package jonlatane.com.quizzy;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;

import com.jonlatane.quizzy.io.EnrolledQuiz;

import java.util.List;

/**
 * Created by jonlatane on 11/27/15.
 */
public class EnrolledQuizTest extends InstrumentationTestCase {
    public static final String QUIZ_URL = "https://docs.google.com/document/u/0/export?format=txt&id=1MV7GHAvv4tgj98Hj6B_WZdeeEu7CRf1GwOfISjP4GT0";

    Context context;

    public void setUp() throws Exception {
        super.setUp();

        context = new MockContext();

        assertNotNull(context);

    }

    public void testEnrolledQuiz() {
        EnrolledQuiz q = EnrolledQuiz.addQuiz(QUIZ_URL, context);
        List<EnrolledQuiz> quizzes = EnrolledQuiz.getEnrolledQuizzes(context);

        assertEquals(false, true);
    }
}
