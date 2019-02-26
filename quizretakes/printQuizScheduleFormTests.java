package quizretakes;
import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class printQuizScheduleFormTests
{
private Class testSubject;
private quizschedule q;
private Method meth; //its a hell of a drug
private courseBean course;
private quizzes quizList;
private retakes retakeList;
private final ByteArrayOutputStream testingOut = new ByteArrayOutputStream();
private final PrintStream normalOut = System.out;


@Before
public void initialize() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException
{
  q = new quizschedule();
  testSubject = q.getClass();
  Field field;
  System.setOut(new PrintStream(testingOut));
  
  
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

@After
public void cleanup()
{
   System.setOut(new PrintStream(normalOut));
}

@Test
public void test1() throws Exception
{
  //meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
  //meth.setAccessible(true);
  //meth.invoke(q, new quizzes(), new retakes(), course); 
  //assertEquals("\n\n******************************************************************************\nGMU quiz retake scheduler for class Software testing\n******************************************************************************\n\n\nYou can sign up for quiz retakes within the next two weeks. \nEnter your name (as it appears on the class roster), \nthen select which date, time, and quiz you wish to retake from the following list.\n\nToday is TUESDAY, FEBRUARY 26\nCurrently scheduling quizzes for the next two weeks, until TUESDAY, MARCH 12\n\n", testingOut.toString());
}

@Test // quiz from more than 14 days ago. Expected: Shouldnt show up in the quiz listings at all.
public void testExpiredQuiz()
{}

@Test // quiz hasnt happened yet so it isnt a valid option. Expected: Shouldnt show up in the quiz listings at all.
public void testFutureQuiz()
{}

@Test // this is a valid quiz. Expected: We should see it in the list 
public void testValidQuiz()
{}
  
@Test // 
public void testExpiredQuiz()
{}

}



