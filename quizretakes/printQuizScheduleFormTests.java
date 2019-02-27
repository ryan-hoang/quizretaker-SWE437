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

public class printQuizScheduleFormTests
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


    @Test // quiz from more than 14 days ago. Expected: Shouldnt show up in the quiz listings at all.  (use regex to check)
    public void testExpiredQuiz()  throws Exception
    {
      course.setStartSkip(today.minusDays(10)); //set skip start to 10 days ago, Controllability
      course.setEndSkip(today.minusDays(5)); // set end of the skip period to 5 days ago, Controllability
      LocalDate retakeDate = today;//Controllability
      LocalDate quizGiven = today.minusDays(15);  //quiz date says it happened 15 days ago so its no longer valid for a retake, Controllability
      quizList = new quizzes(1, quizGiven.getMonthValue(), quizGiven.getDayOfMonth(),15,30);//Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);//Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, quizList, retakeList, course); 
      
      testPattern = Pattern.compile("1\\)\\sQuiz\\s1\\sfrom\\s" + quizGiven.getDayOfWeek() + ",\\s" + quizGiven.getMonth());// Observability
      testMatch = testPattern.matcher(testingOut.toString());//  Observability
      if(debug) {System.err.println("\nTEST_EXPIRED_QUIZ Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertEquals(false,testMatch.find()); // Observability
    }

    @Test // quiz hasnt happened yet so it isnt a valid option. Expected: Shouldnt show up in the quiz listings at all. (use regex to check)
    public void testFutureQuiz() throws Exception
    {
      course.setStartSkip(today.minusDays(10)); //set skip start to 10 days ago, Controllability
      course.setEndSkip(today.minusDays(5)); // set end of the skip period to 5 days ago, Controllability
      LocalDate retakeDate = today;// Controllability
      LocalDate quizGiven = today.plusDays(1);  //quiz date says it took place tomorrow which is invalid. Controllability
      quizList = new quizzes(1, quizGiven.getMonthValue(), quizGiven.getDayOfMonth(),15,30);// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, quizList, retakeList, course); 
      
      testPattern = Pattern.compile("1\\)\\sQuiz\\s1\\sfrom\\s" + quizGiven.getDayOfWeek() + ",\\s" + quizGiven.getMonth()); // Observability
      testMatch = testPattern.matcher(testingOut.toString());// Observability
      if(debug) {System.err.println("\nTEST_FUTURE_QUIZ Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertEquals(false,testMatch.find());// Observability
    }

    @Test // this is a valid quiz. Expected: We should see it in the list   (use regex to check)
    public void testValidQuiz() throws Exception
    {
      course.setStartSkip(today.minusDays(10)); // set the beginning of the skip period to 10 days ago, Controllability
      course.setEndSkip(today.minusDays(5)); // set the end of the skip period to 5 days ago, Controllability
      LocalDate retakeDate = today;// Controllability
      LocalDate quizGiven = today.minusDays(1); // Controllability
      quizList = new quizzes(1, quizGiven.getMonthValue(), quizGiven.getDayOfMonth(),15,30);// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, quizList, retakeList, course); 
      
      testPattern = Pattern.compile("1\\)\\sQuiz\\s1\\sfrom\\s" + quizGiven.getDayOfWeek() + ",\\s" + quizGiven.getMonth());// Observability
      testMatch = testPattern.matcher(testingOut.toString());// Observability
      if(debug) {System.err.println("\nTEST_VALID_QUIZ Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertEquals(true,testMatch.find()); // Observability
    }
      
    @Test // retake session is on a date that already passed. Expected: should not show up  (use regex to check)
    public void testExpiredRetake() throws Exception
    {
      course.setStartSkip(today.minusDays(10)); // set the beginning of the skip period to 10 days ago, Controllability
      course.setEndSkip(today.minusDays(5)); // set the end of the skip period to 5 days ago, Controllability
      LocalDate retakeDate = today.minusDays(2);// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), retakeList, course); 
      
      testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTLOCATION");// Observability
      testMatch = testPattern.matcher(testingOut.toString());// Observability
      if(debug) {System.err.println("\nTEST_EXPIRED_RETAKE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertEquals(false,testMatch.find()); // Observability
    }

    @Test // retake session is more than 2 weeks in the future Expected: should not show up  (use regex to check)
    public void testFutureRetake() throws Exception
    {
      course.setStartSkip(today.minusDays(2)); // set the beginning of the skip period to 2 days ago, Controllability
      course.setEndSkip(today.minusDays(1)); // set the end of the skip period to yesterday, Controllability
      LocalDate retakeDate = today.plusDays(15); // set the retake more than 2 weeks away, Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), retakeList, course); 
      
      testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTLOCATION");// Observability
      testMatch = testPattern.matcher(testingOut.toString());// Observability
      if(debug) {System.err.println("\nTEST_FUTURE_RETAKE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertFalse(testMatch.find()); // Observability
    }
      
    @Test // todays date is correctly displayed. Expected: i.e. "Today is TUESDAY, FEBRUARY 26" (use regex to check)
    public void testTodaysDate() throws Exception
    {
      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);//Controllability
      meth.setAccessible(true);// Controllability
      meth.invoke(q, new quizzes(), new retakes(), course); //Controllability
      
      testPattern = Pattern.compile("Today\\sis\\s" + today.getDayOfWeek() + ",\\s" + today.getMonth() + "\\s" + today.getDayOfMonth());// Observability
      testMatch = testPattern.matcher(testingOut.toString());// Observability
      if(debug) {System.err.println("\nTEST_TODAYS_DATE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertTrue(testMatch.find()); // Observability
    }

    @Test // date 2 weeks from todays date should also be shown. Expected: i.e. "... until TUESDAY, MARCH 12" (use regex to check)
    public void testMaxRetakeDate() throws Exception
    {
      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), new retakes(), course); 
      
      testPattern = Pattern.compile("Currently\\sscheduling\\squizzes\\sfor\\sthe\\snext\\stwo\\sweeks,\\suntil\\s" + (endDay.getDayOfWeek()) + ",\\s" + endDay.getMonth() + "\\s" + endDay.getDayOfMonth()); // Observability
      testMatch = testPattern.matcher(testingOut.toString()); // Observability
      if(debug) {System.err.println("\nTEST_MAX_RETAKE_DATE Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertTrue(testMatch.find());  // Observability
    }
      
    @Test // a retake session will be privided that is within the skip window. It should not appear. Expected: test will fail if the regex matcher finds something.
    public void testRetakeDuringSkip() throws Exception
    {
      course.setStartSkip(today.minusDays(1)); // set the beginning of the skip period to yesterday, Controllability
      course.setEndSkip(today.plusDays(1)); // set the end of the skip period to tomorrow, Controllability
      LocalDate retakeDate = today;// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), retakeList, course); 
      
      testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTLOCATION");// Observability
      testMatch = testPattern.matcher(testingOut.toString());  // Observability
      if(debug) {System.err.println("\nTEST_RETAKE_DURING_SKIP Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertEquals(false,testMatch.find()); // Observability
    }
      
    @Test // retake session after the skip window. Expected: should find the retake session present in the output.
    public void testRetakeAfterSkip() throws Exception
    {
      course.setStartSkip(today.minusDays(2)); // set the beginning of the skip period to 2 days ago, Controllability
      course.setEndSkip(today.minusDays(1)); // set the end of the skip period to yesterday, Controllability
      LocalDate retakeDate = today;// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), retakeList, course); 
      
      testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTLOCATION"); // Observability
      testMatch = testPattern.matcher(testingOut.toString()); // Observability
      if(debug) {System.err.println("\nTEST_RETAKE_AFTER_SKIP Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertTrue(testMatch.find()); // Observability
    }

    @Test // retake session before skip window. Expected: should find the retake session present in the output.
    public void testRetakeBeforeSkip() throws Exception
    {
      course.setStartSkip(today.plusDays(1)); // set the beginning of the skip period to yesterday, Controllability
      course.setEndSkip(today.plusDays(3)); // set the end of the skip period to tomorrow, Controllability
      LocalDate retakeDate = today;// Controllability
      retakeList = new retakes(1,"TESTLOCATION",retakeDate.getMonthValue(),retakeDate.getDayOfMonth(),15,30);// Controllability

      meth = testSubject.getDeclaredMethod("printQuizScheduleForm", quizretakes.quizzes.class, quizretakes.retakes.class, quizretakes.courseBean.class);
      meth.setAccessible(true);
      meth.invoke(q, new quizzes(), retakeList, course);
       
      testPattern = Pattern.compile("RETAKE:\\s"+ retakeDate.getDayOfWeek() + ",\\s" + retakeDate.getMonth() + "\\s" + retakeDate.getDayOfMonth() +",\\sat\\s15:30\\sin\\sTESTLOCATION"); // Observability
      testMatch = testPattern.matcher(testingOut.toString()); // Observability
      if(debug) {System.err.println("\nTEST_RETAKE_BEFORE_SKIP Output:") ; System.err.println("\n" + testingOut.toString()) ; System.err.println(""); }
      assertTrue(testMatch.find()); // Observability
    }

}



