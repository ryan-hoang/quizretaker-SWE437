package quizretakes;
import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.*;

//Ryan Hoang, Justin Plassmeyer, Ryan Robinson
//Dependencies JUNIT-4.12 | Hamcrest-core-2.1

public class assignment9tests
{

private Boolean debug = false;      // print out output of each test

private Class<?> testSubject;
private quizschedule q;
private Method meth;     //its a hell of a drug
private Pattern testPattern;
private Matcher testMatch;
private static int daysAvailable = 14;
private LocalDate today = LocalDate.now();
private LocalDate endDay = today.plusDays(new Long(daysAvailable));

private courseBean course;
private quizzes quizList;
private retakes retakeList;
private quizBean quiz;
private retakeBean retake;

private final ByteArrayOutputStream testingOut = new ByteArrayOutputStream();
private final PrintStream normalOut = System.out;


@Before
public void initialize() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException
{
        q = new quizschedule();
        testSubject = q.getClass();
        Field field;
        System.setOut(new PrintStream(testingOut));
        retakeList = new retakes();
        course = new courseBean("swe437", "Software testing", Integer.toString(daysAvailable), today, today, "quizretakes/");
        quizList = new quizzes();

        /* Uncomment if you want to use actual datafiles in your tests
           //set courseFileName
           meth = testSubject.getDeclaredMethod("readCourseFile", String.class);
           meth.setAccessible(true);
           field = testSubject.getDeclaredField("courseFileName");
           field.setAccessible(true);
           field.set(q, "quizretakes/course-swe437.xml");
           course = (courseBean) meth.invoke(q,"swe437");

           //set quizzesFileName
           meth = testSubject.getDeclaredMethod("readQuizzes", String.class);
           meth.setAccessible(true);
           field = testSubject.getDeclaredField("quizzesFileName");
           field.setAccessible(true);
           field.set(q, "quizretakes/quiz-orig-swe437.xml");
           quizList = (quizzes) meth.invoke(q,"swe437");

           //set retakesFileName
           meth = testSubject.getDeclaredMethod("readRetakes", String.class);
           meth.setAccessible(true);
           field = testSubject.getDeclaredField("retakesFileName");
           field.setAccessible(true);
           field.set(q, "quizretakes/quiz-retakes-swe437.xml");
           retakeList = (retakes) meth.invoke(q,"swe437");
         */
}

@After
public void cleanup()
{
        System.setOut(new PrintStream(normalOut));
}


@Test     // (-, empty retake list, CourseBean defines today inside the skip window) the end date should be 21 days in the future instead of 14 because 14 days from now is within the skip window.
public void test1() throws Exception
{
        course.setStartSkip(today.plusDays(13));
        course.setEndSkip(today.plusDays(15));
        LocalDate retakeDate = today;
        retakeList = new retakes();

        meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
        meth.setAccessible(true);
        meth.invoke(q, quizList, retakeList, course);

        LocalDate expected = today.plusDays(21);
        testPattern = Pattern.compile("Currently scheduling quizzes for the next two weeks, until " + (expected.getDayOfWeek()) + ", " + expected.getMonth() + " " + expected.getDayOfMonth());
        testMatch = testPattern.matcher(testingOut.toString());
        if(debug) {System.err.println("\nTest1 Output:"); System.err.println("\n" + testingOut.toString()); System.err.println(""); }
        assertTrue(testMatch.find());
}

//two quizzes one expired, one valid
//one retake session, valid
//retake session is outside of skip window.
@Test
public void test2() throws Exception
{
        course.setStartSkip(today.minusDays(2));
        course.setEndSkip(today.minusDays(1));
        LocalDate retakeDate = today;
        retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);
        quizList = new quizzes();
        LocalDate q1Date = today.minusDays(15);
        LocalDate q2Date = today.minusDays(1);
        quizBean q1 = new quizBean(1, q1Date.getMonthValue(), q1Date.getDayOfMonth(), 15, 30); // expired quiz
        quizBean q2 = new quizBean(2, q2Date.getMonthValue(), q2Date.getDayOfMonth(), 15, 30); // valid quiz

        quizList.addQuiz(q1);
        quizList.addQuiz(q2);

        meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
        meth.setAccessible(true);
        meth.invoke(q, quizList, retakeList, course);

        Pattern patternQ1 = Pattern.compile("Quiz " + 1 + " from " + q1Date.getDayOfWeek() + ", " + q1Date.getMonth() + " " + q1Date.getDayOfMonth());
        Matcher testMatchQ1 = patternQ1.matcher(testingOut.toString());
        Pattern patternQ2 = Pattern.compile("Quiz " + 2 + " from " + q2Date.getDayOfWeek() + ", " + q2Date.getMonth() + " " + q2Date.getDayOfMonth());
        Matcher testMatchQ2 = patternQ2.matcher(testingOut.toString());
        if(debug) {System.err.println("\nTest2 Output:"); System.err.println("\n" + testingOut.toString()); System.err.println(""); }
        assertTrue(!testMatchQ1.find() && testMatchQ2.find());
}

//we are outside skip window
//one invalid retake session (yesterday)
//one valid retake session (tomorrow)
//empty quiz list
@Test
public void test3() throws Exception
{
        course.setStartSkip(today.minusDays(2));
        course.setEndSkip(today.minusDays(1));
        LocalDate retakeDateR1 = today.minusDays(1);
        LocalDate retakeDateR2 = today.plusDays(1);
        retakeBean r1 = new retakeBean(1,"TESTLOCATION",retakeDateR1.getMonthValue(),retakeDateR1.getDayOfMonth(),15,30);
        retakeBean r2 = new retakeBean(2,"TESTLOCATION",retakeDateR2.getMonthValue(),retakeDateR2.getDayOfMonth(),15,30);

        retakeList.addRetake(r1);
        retakeList.addRetake(r2);

        quizList = new quizzes();


        meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
        meth.setAccessible(true);
        meth.invoke(q, quizList, retakeList, course);

        Pattern patternR1 = Pattern.compile("RETAKE: " + retakeDateR1.getDayOfWeek() + ", " +
                                            retakeDateR1.getMonth() + " " +
                                            retakeDateR1.getDayOfMonth() + ", at " +
                                            "15:30" + " in " + "TESTLOCATION");
        Matcher testMatchR1 = patternR1.matcher(testingOut.toString());

        Pattern patternR2 = Pattern.compile("RETAKE: " + retakeDateR2.getDayOfWeek() + ", " +
                                            retakeDateR2.getMonth() + " " +
                                            retakeDateR2.getDayOfMonth() + ", at " +
                                            "15:30" + " in " + "TESTLOCATION");
        Matcher testMatchR2 = patternR2.matcher(testingOut.toString());
        if(debug) {System.err.println("\nTest3 Output:"); System.err.println("\n" + testingOut.toString()); System.err.println(""); }
        assertTrue(!testMatchR1.find() && testMatchR2.find());
}

//outside skip window
//retake list has 1 session outside of skip
//no quizzes
//retake session is invalid, it is already past
@Test
public void test5() throws Exception
{
        course.setStartSkip(today.minusDays(2));
        course.setEndSkip(today.minusDays(1));
        LocalDate retakeDateR1 = today.minusDays(1);
        retakeBean r1 = new retakeBean(1,"TESTLOCATION",retakeDateR1.getMonthValue(),retakeDateR1.getDayOfMonth(),15,30);
        retakeList.addRetake(r1);
        quizList = new quizzes();

        meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
        meth.setAccessible(true);
        meth.invoke(q, quizList, retakeList, course);

        Pattern patternR1 = Pattern.compile("RETAKE: " + retakeDateR1.getDayOfWeek() + ", " +
                                            retakeDateR1.getMonth() + " " +
                                            retakeDateR1.getDayOfMonth() + ", at " +
                                            "15:30" + " in " + "TESTLOCATION");
        Matcher testMatchR1 = patternR1.matcher(testingOut.toString());

        if(debug) {System.err.println("\nTest5 Output:"); System.err.println("\n" + testingOut.toString()); System.err.println(""); }
        assertTrue(!testMatchR1.find());
}

}



