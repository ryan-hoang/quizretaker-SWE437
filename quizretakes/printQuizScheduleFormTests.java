package quizretakes;
import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.*;
//import static org.hamcrest.Matchers.*;

public class printQuizScheduleFormTests
{
  
private Boolean debug = false;  

private Class<?> testSubject;
private quizschedule q;
private Method meth; //its a hell of a drug
private Pattern testPattern;
private Matcher testMatch;
private static int daysAvailable = 14;
private static int startSkipMonth = 2;//2
private static int startSkipDay = 9;//9
private static int endSkipMonth = 2;//2
private static int endSkipDay = 17;//17
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
  course = new courseBean("swe437", "Software testing", Integer.toString(daysAvailable), LocalDate.of(today.getYear(),startSkipMonth,startSkipDay), LocalDate.of(today.getYear(),endSkipMonth,endSkipDay), "quizretakes/");
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

@Test
public void test1() throws Exception
{
  //meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
  //meth.setAccessible(true);
  //meth.invoke(q, new quizzes(), new retakes(), course); 
  //assertEquals("\n\n******************************************************************************\nGMU quiz retake scheduler for class Software testing\n******************************************************************************\n\n\nYou can sign up for quiz retakes within the next two weeks. \nEnter your name (as it appears on the class roster), \nthen select which date, time, and quiz you wish to retake from the following list.\n\nToday is TUESDAY, FEBRUARY 26\nCurrently scheduling quizzes for the next two weeks, until TUESDAY, MARCH 12\n\n", testingOut.toString());
}

@Test // quiz from more than 14 days ago. Expected: Shouldnt show up in the quiz listings at all.  (use regex to check)
public void testExpiredQuiz()
{
  
  testPattern = Pattern.compile("");
  testMatch = testPattern.matcher(testingOut.toString());
  //assertFalse(testMatch.find());
}

@Test // quiz hasnt happened yet so it isnt a valid option. Expected: Shouldnt show up in the quiz listings at all. (use regex to check)
public void testFutureQuiz()
{}

@Test // this is a valid quiz. Expected: We should see it in the list   (use regex to check)
public void testValidQuiz()
{}
  
@Test // retake session is on a date that already passed. Expected: should not show up  (use regex to check)
public void testExpiredRetake()
{}

@Test // retake session is more than 2 weeks in the future Expected: should not show up  (use regex to check)
public void testFutureRetake()
{}

@Test // retake session is valid and within 2 weeks from today. Expected: should show up. (use regex to check)
public void testValidRetake()
{}
  
@Test // todays date is correctly displayed. Expected: i.e. "Today is TUESDAY, FEBRUARY 26" (use regex to check)
public void testTodaysDate() throws Exception
{
  meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
  meth.setAccessible(true);
  meth.invoke(q, new quizzes(), new retakes(), course); 
  testPattern = Pattern.compile("Today\\sis\\s" + today.getDayOfWeek() + ",\\s" + today.getMonth() + "\\s" + today.getDayOfMonth());
  testMatch = testPattern.matcher(testingOut.toString());
  if(debug) {System.err.println("\n\nTEST_TODAYS_DATE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
  assertTrue(testMatch.find());
}

@Test // date 2 weeks from todays date should also be shown. Expected: i.e. "... until TUESDAY, MARCH 12" (use regex to check)
public void testMaxRetakeDate() throws Exception
{
  meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
  meth.setAccessible(true);
  meth.invoke(q, new quizzes(), new retakes(), course); 
  testPattern = Pattern.compile("Currently\\sscheduling\\squizzes\\sfor\\sthe\\snext\\stwo\\sweeks,\\suntil\\s" + (endDay.getDayOfWeek()) + ",\\s" + endDay.getMonth() + "\\s" + endDay.getDayOfMonth());
  testMatch = testPattern.matcher(testingOut.toString());
  if(debug) {System.err.println("\n\nTEST_MAX_RETAKE_DATE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
  assertTrue(testMatch.find());  
}
  
@Test // a retake session will be privided that is within the skip window. It should not appear. Expected: test will fail if the regex matcher finds something.
public void testRetakeDuringSkip() throws Exception
{
  retakeList = new retakes(1,"TESTRETAKEDURINGSKIPLOCATION",startSkipMonth,startSkipDay,15,30);
  LocalDate retakeDate = LocalDate.of(today.getYear(),startSkipMonth,startSkipDay);
  
  meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
  meth.setAccessible(true);
  meth.invoke(q, new quizzes(), retakeList, course); 
  testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTRETAKEDURINGSKIPLOCATION");
  testMatch = testPattern.matcher(testingOut.toString());
  if(debug) {System.err.println("\n\nTEST_RETAKE_DURING_SKIP Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
  assertFalse(testMatch.find());  
}
  
@Test // retake session after the skip window. Expected: should find the retake session present in the output.
public void testRetakeAfterSkip()
{}

@Test // retake session before skip window. Expected: should find the retake session present in the output.
public void testRetakeBeforeSkip()
{}

}



