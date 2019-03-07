package quizretakes;
import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.*;
import java.util.Scanner;
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
    //value no longer needed a field check as it was set via a method call
    */
    //second test V 2.0
    //fails since the field isTeacher is always true
    // had to refactor test since it now needs to input to a method instead of just the field
    // had to refactor code to accept different veriations of no and yes
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
    // code refactored to give a more usefull error message
    // needed since it was not clear as to why it was failing
    @Test(expected = Exception.class)
    public void teacher_or_studendtFailTest() throws Exception
    {
        boolean is_teacher = true;
        String input_str = "hello";
        provideInput(input_str);
        meth = testSubject.getDeclaredMethod("teacher_or_studendt");
        meth.setAccessible(true);
        is_teacher = (boolean) meth.invoke(q);

    }
    // fourth test
    // should fail at first since this is the first test for a method
    // should pass since it is being send a valid input string
    // happy path testing
    @Test
    public void teacher_or_studendtPassTest() throws Exception
    {
        boolean is_teacher = true;
        String input_str = "No";
        provideInput(input_str);
        meth = testSubject.getDeclaredMethod("teacher_or_studendt");
        meth.setAccessible(true);
        is_teacher = (boolean) meth.invoke(q);
        assertEquals(false,is_teacher);

    }
    // fifth test
    // should fail since it there is no method yet
    // wrote the code to bring it first to compile then into the green
    // refactored it to give a more helpfull error message
    // testing to see if teacher can choose to add retake session
    @Test
    public void quiz_or_retakeTest() throws Exception
    {
        meth = testSubject.getDeclaredMethod("quiz_or_retake",Scanner.class);
        String input_str = "1";
        provideInput(input_str);
        meth.setAccessible(true);
        Scanner scan = new Scanner(System.in);
        String returnValue = (String) meth.invoke(q,scan);
        assertEquals("Retake",returnValue);
    }
    // sixth test
    // this is to hammer out the hardcoding
    @Test
    public void quiz_or_retakeTest2() throws Exception
    {
        meth = testSubject.getDeclaredMethod("quiz_or_retake",Scanner.class);
        String input_str = "2";
        provideInput(input_str);
        meth.setAccessible(true);
        Scanner scan = new Scanner(System.in);
        String returnValue = (String) meth.invoke(q,scan);
        assertEquals("Quiz",returnValue);
    }
    //seventh test
    //not a happy path the user enters a random number other that 1 or 2 should throw and exception
    @Test(expected = Exception.class)
    public void quiz_or_retakeTest3() throws Exception
    {
        meth = testSubject.getDeclaredMethod("quiz_or_retake",Scanner.class);
        String input_str = "5";
        provideInput(input_str);
        meth.setAccessible(true);
        Scanner scan = new Scanner(System.in);
        String returnValue = (String) meth.invoke(q,scan);

    }
    // eigth test
    // a helper method to get the id of the latest test
    //should first fail
    @Test
    public void getLastIDTest() throws Exception
    {
        Field field;
        meth = testSubject.getDeclaredMethod("readRetakes", String.class);
        meth.setAccessible(true);
        field = testSubject.getDeclaredField("retakesFileName");
        field.setAccessible(true);
        field.set(q, "quizretakes/quiz-retakes-swe437_test2.xml");
        retakeList = (retakes) meth.invoke(q,"swe437");

        meth = testSubject.getDeclaredMethod("getLastRetakeID", quizretakes.retakes.class);
        meth.setAccessible(true);
        int lastID = (int) meth.invoke(q, retakeList);
        assertEquals(10,lastID);
    }
    //ninth test
    // tests to see if null list is given, nonhappy path
    // this test was the one that lead to the error message refactoring
    @Test(expected = Exception.class)
    public void getLastID_NULLTest() throws Exception
    {
        retakeList = null;
        meth = testSubject.getDeclaredMethod("getLastID", quizretakes.retakes.class);
        meth.setAccessible(true);
        int lastID = (int) meth.invoke(q,retakeList);
    }
    // tenth test
    // tests getting the retake location
    // no compile
    // hardcoding
    @Test
    public void getRetakeLocactionTest1() throws Exception
    {
        String input_str = "EB 5321";
        provideInput(input_str);
        Scanner scan = new Scanner(System.in);
        meth = testSubject.getDeclaredMethod("getRetakeLocation",Scanner.class);
        meth.setAccessible(true);
        String returnStr = (String) meth.invoke(q,scan);
        assertEquals("EB 5321", returnStr);
    }
    // eleventh test
    // hammer out the hard coding in the getRetake Location method
    // now a useless test as it test nothng new
    @Test
    public void getRetakeLocationTest2() throws Exception
    {
        String input_str = "EB 4430";
        provideInput(input_str);
        Scanner scan = new Scanner(System.in);
        meth = testSubject.getDeclaredMethod("getRetakeLocation",Scanner.class);
        meth.setAccessible(true);
        String returnStr = (String) meth.invoke(q,scan);
        assertEquals("EB 4430", returnStr);
    }
    // 12th test
    // new line testing
    // tests to see what happens when user hits a blank enter
    // refactored code to allow it to handle this situation
    @Test
    public void getRetakeLocationTest3() throws Exception
    {
        String input_str = "\nEB 4430";
        provideInput(input_str);
        Scanner scan = new Scanner(System.in);
        meth = testSubject.getDeclaredMethod("getRetakeLocation",Scanner.class);
        meth.setAccessible(true);
        String returnStr = (String) meth.invoke(q,scan);
        assertEquals("EB 4430", returnStr);
    }
    // 13th test
    // adding a new feature of getNewDate
    // doesn't compile
    // leads to hard coading of a new method
    @Test
    public void getDateRetakeTest1() throws Exception
    {
        meth = testSubject.getDeclaredMethod("getNewDate",Scanner.class);
        meth.setAccessible(true);
        // added as the method changed
        provideInput("4 3 15 30");
        Scanner scan = new Scanner(System.in);
        int[] date_test = (int[]) meth.invoke(q,scan);
        int[] expected = new int[] {4,3,15,30};
        assertArrayEquals(expected, date_test);
    }
    // 14th test
    // hammering out the hard coding in getNewDate;
    // after refactoring the code this test is now useless as it is the same as the prior
    // test was later refactored as the time and date methods were combined into one for ease of access

    @Test
    public void getDateRetakeTest2() throws Exception
    {
        // this was added in since the code now has Command line input
        String date = "7 3 11 20";
        provideInput(date);
        meth = testSubject.getDeclaredMethod("getNewDate",Scanner.class);
        meth.setAccessible(true);
        Scanner scan = new Scanner(System.in);
        int[] date_test = (int[]) meth.invoke(q,scan);
        int[] expected = new int[] {7,3,11,20};
        assertArrayEquals(expected, date_test);
    }
    // 15th test
    //not a very happy path test person enters numbers that are not valid
    // as this should be valid lead to a rework of the code to allow for the user to retry for invalid numbers entered
    // test was later refactored as the time and date methods were combined into one for ease of access
    @Test
    public void getDateRetakeTest3() throws Exception
    {
        String input_str = "123 0 123 -123 4 123 -543 543 3 9 10";
        provideInput(input_str);
        meth = testSubject.getDeclaredMethod("getNewDate",Scanner.class);
        meth.setAccessible(true);
        Scanner scan = new Scanner(System.in);
        int[] date_test = (int[]) meth.invoke(q,scan);
        int[] expected = new int[] {4,3,9,10};
        assertArrayEquals(expected,date_test);
    }
    /*

            Being integrated into the tests above as the method that pulls all of them together would not be able to be automated as it stood

    // 16th test
    // adding new method getNewTime
    @Test
    public void getTimeRetakeTest1() throws Exception
    {
        meth = testSubject.getDeclaredMethod("getNewTime");
        meth.setAccessible(true);
        // added as the method changed
        provideInput("15 30");
        int[] date_test = (int[]) meth.invoke(q);
        int[] expected = new int[] {15,30};
        assertArrayEquals(expected, date_test);
    }



    // 17th test
    // adding new method getNewTime
    @Test
    public void getTimeRetakeTest2() throws Exception
    {
        meth = testSubject.getDeclaredMethod("getNewTime");
        meth.setAccessible(true);
        // added as the method changed
        provideInput("16 30");
        int[] date_test = (int[]) meth.invoke(q);
        int[] expected = new int[] {16,30};
        assertArrayEquals(expected, date_test);
    }
    // 18th test
    //not a very happy path test person enters a time that is not valid
    @Test
    public void getTimeRetakeTest3() throws Exception
    {
        String input_str = "123 -1 123 -123 10 123 -543 543 30";
        provideInput(input_str);
        meth = testSubject.getDeclaredMethod("getNewTime");
        meth.setAccessible(true);
        int[] date_test = (int[]) meth.invoke(q);
        int[] expected = new int[] {10,30};
        assertArrayEquals(expected,date_test);
    }
    */
    // 19th test
    // testing getting the last quiz id
    // same code as other test just different input
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! THIS TEST WILL FAIL IF YOU ADD A TEST BY RUNNING THE ACTUAL CODE TO COMPLETION
    //          AS THE EXPECTED WOULD HAVE TO CHANGE AS WELL POSSIBLE !!!!!!!!!!!!!!!!!!!
    // nvm false alarm test was refactored to look at a different instance of the quizzes
    @Test
    public void getLastQuizIDTest() throws Exception
    {
        Field field;
        meth = testSubject.getDeclaredMethod("readQuizzes", String.class);
        meth.setAccessible(true);
        field = testSubject.getDeclaredField("quizzesFileName");
        field.setAccessible(true);
        field.set(q, "quizretakes/quiz-orig-swe437_test2.xml");
        quizList = (quizzes) meth.invoke(q,"swe437");


        meth = testSubject.getDeclaredMethod("getLastQuizID", quizretakes.quizzes.class);
        meth.setAccessible(true);
        int lastID = (int) meth.invoke(q, quizList);
        assertEquals(2,lastID);
    }
    /*

     these tests are no longer needed as the way that the xml was added to the file was changed and the string return
        value was refactored out of both the test, and the code

     22 test means that these are no longer valid test
    // 20th test
    //first test of the full retake system
    // this test was no longer needed as this was a partial working of the final xml appender
    @Test
    public void newQuizTest1() throws Exception
    {
        String expected = "<id>3</id>";
        meth = testSubject.getDeclaredMethod("newQuiz",quizretakes.quizzes.class);
        meth.setAccessible(true);
        String returnVal = (String) meth.invoke(q, quizList);
        assertEquals(expected,returnVal);
    }
    // 21th
    // testing if quizlist is null sad path
    // 20th test
    //first test of the full retake system
    @Test(expected = Exception.class)
    public void newQuizTest2() throws Exception
    {
        String expected = "<id>3</id>";
        meth = testSubject.getDeclaredMethod("newQuiz",quizretakes.quizzes.class);
        meth.setAccessible(true);
        quizList = null;
        String returnVal = (String) meth.invoke(q, quizList);
    }
    // 22th test
    // testing the next level of the xml
    @Test
    public void newQuizTest3() throws Exception
    {
        String expected = "<quiz><id>3</id><dateGiven><month>3</month><day>6</day></dateGiven></quiz>";
        meth = testSubject.getDeclaredMethod("newQuiz",quizretakes.quizzes.class);
        meth.setAccessible(true);
        provideInput("6 3");
        String returnVal = (String) meth.invoke(q, quizList);
        assertEquals(expected,returnVal);
    }
    // 23th test
    //
    // testing the next level of the xml
    // test refactored into the next one as this one checks things that are nolonger valid
    @Test
    public void newQuizTest3() throws Exception
    {
        String expected = "<quiz><id>3</id><dateGiven><month>3</month><day>6</day><hour>15</hour><minute>30</minute></dateGiven></quiz>";
        meth = testSubject.getDeclaredMethod("newQuiz",quizretakes.quizzes.class);
        meth.setAccessible(true);
        String data = "6 3 15 30";
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        String returnVal = (String) meth.invoke(q, quizList);
        assertEquals(expected,returnVal);
    }
    */
    // 24th test
    // tests the final xml write of a new quiz
    // gives the input to write to a text xml
    // was refactored both in code and test a few times to its current state
    @Test
    public void testingXMLwrite1() throws Exception
    {
        Field field;
        //set quizzesFileName
        //grabs an unmodified version of the quizzes
        meth = testSubject.getDeclaredMethod("readQuizzes", String.class);
        meth.setAccessible(true);
        field = testSubject.getDeclaredField("quizzesFileName");
        field.setAccessible(true);
        field.set(q, "quizretakes/quiz-orig-swe437_test.xml");
        quizList = (quizzes) meth.invoke(q,"swe437");

        meth = testSubject.getDeclaredMethod("getLastQuizID", quizretakes.quizzes.class);
        meth.setAccessible(true);
        int newID = (int) meth.invoke(q,quizList) + 1;

        meth = testSubject.getDeclaredMethod("newQuiz",quizretakes.quizzes.class);
        meth.setAccessible(true);
        String data = "7 3 15 30";
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        String returnVal = (String) meth.invoke(q, quizList);

        meth = testSubject.getDeclaredMethod("readQuizzes", String.class);
        meth.setAccessible(true);
        quizzes result = (quizzes) meth.invoke(q,"swe437");

        quizBean newQuizBean = new quizBean(newID,3,7,15,30);
        quizList.addQuiz(newQuizBean);
        assertEquals(quizList.toString(),result.toString());

    }

    // 25th test
    // tests the final xml write of a new retake
    //will fail on compile
    // will have to add the code to make it run
    // added the code to make it compile
    // added the code to make it green bar
    // refactored the code
    // learned from last test
    @Test
    public void testingXMLwriteRetakes() throws Exception
    {
        Field field;
        //set retakesFileName
        //used to grab the unmodified already good retake list
        meth = testSubject.getDeclaredMethod("readRetakes", String.class);
        meth.setAccessible(true);
        field = testSubject.getDeclaredField("retakesFileName");
        field.setAccessible(true);
        field.set(q, "quizretakes/quiz-retakes-swe437_test.xml");
        retakeList = (retakes) meth.invoke(q,"swe437");

        meth = testSubject.getDeclaredMethod("getLastRetakeID", quizretakes.retakes.class);
        meth.setAccessible(true);
        int newID = (int) meth.invoke(q,retakeList) + 1;

        meth = testSubject.getDeclaredMethod("newRetake",quizretakes.retakes.class);
        meth.setAccessible(true);
        String data = "EB 1234\n7 3 15 30";
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        //String returnVal = (String)
        meth.invoke(q, retakeList);

        meth = testSubject.getDeclaredMethod("readRetakes", String.class);
        meth.setAccessible(true);
        retakes result = (retakes) meth.invoke(q,"swe437");

        retakeBean newRetakeBean = new retakeBean(newID,"EB 1234" ,3,7,15,30);
        retakeList.addRetake(newRetakeBean);
        assertEquals(retakeList.toString(),result.toString());

    }











    // this test has nothing to do with final project just a proof of concept
    // my spike
    @Test
    public void testingSequenceStream() throws Exception
    {
        meth = testSubject.getDeclaredMethod("double_scan");
        meth.setAccessible(true);
        String data1 = "hello world";
        String data2 = "world";
        ByteArrayInputStream first = new ByteArrayInputStream(data1.getBytes());
        ByteArrayInputStream second = new ByteArrayInputStream(data2.getBytes());
        System.setIn(first);
        String expected = "hello world";
        String result = (String) meth.invoke(q);
        assertEquals(expected,result);


    }


}



