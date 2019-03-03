package quizretakes;
import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.*;

//Ryan Hoang, Justin Plassmeyer, Ryan Robinson
//Dependencies JUNIT-4.12 | Hamcrest-core-2.1

public class retakeTest
{

    private Boolean debug = false;  // print out output of each test

    private Class<?> testSubject;
    private quizschedule q;
    private Method meth; //its a hell of a drug
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
    private final InputStream systemIn = System.in;
    private ByteArrayInputStream testIn;
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

    }
    private void provideInput(String data)
    {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    @After
    public void cleanup()
    {
        System.setOut(new PrintStream(normalOut));
        System.setIn(System.in);
    }




    /*
    * both tests invalid as there was a change into how the value was set
    // first test it failed since no field
    // fixed it by adding static field of always true
    @Test // test to see if user is a teacher
    public void isTeacherTest() throws IllegalAccessException, NoSuchFieldException
    {
        Field field;
        field = testSubject.getDeclaredField("isTeacher");
        field.setAccessible(true);
        boolean is_teacher = (boolean) field.get(q);
        assertEquals(true,is_teacher);
    }
    //second test
    //fails since the field isTeacher is always true
    // had to refactor test since it now needs to input to a method
    @Test // test to see if user is a student
    public void isStudentTest() throws IllegalAccessException, NoSuchFieldException
    {

        Field field;
        field = testSubject.getDeclaredField("isTeacher");
        field.setAccessible(true);
        boolean is_teacher = (boolean) field.get(q);
        assertEquals(false,is_teacher);
    }
    */
    //second test V 2.0
    //fails since the field isTeacher is always true
    // had to refactor test since it now needs to input to a method instead of just the field
    @Test // test to see if user is a student
    public void isStudentTest() throws Exception
    {
        boolean is_teacher = true;
        meth = testSubject.getDeclaredMethod("user_type",String.class);
        meth.setAccessible(true);
        is_teacher = (boolean) meth.invoke(q,"No");
        assertEquals(false,is_teacher);
    }
    // third test
    //should fail since entered value is no a valid option
    @Test(expected = Exception.class)
    public void teacher_or_studendtTest() throws Exception
    {
        boolean is_teacher = true;
        String input_str = "hello";
        provideInput(input_str);
        meth = testSubject.getDeclaredMethod("teacher_or_studendt");
        meth.setAccessible(true);
        is_teacher = (boolean) meth.invoke(q);

    }

}



