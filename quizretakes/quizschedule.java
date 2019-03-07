// JO, Jan 2019
package quizretakes;

// A command line interface (CLI) for the quizschedule servlet
// SWE 437, Assignment 2, Spring 2019
// This class is based on the quizschedule.java servlet
// Removed all the servlet stuff
// New code has the comment /* CLI */

import java.util.Scanner; /* CLI */

//=============================================================================

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Collection;
import java.time.*;
import java.lang.Long;
import java.lang.String;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties; /* CLI */

/**
 * @author Jeff Offutt
 *         Date: January, 2019
 *
 * Wiring the pieces together:
 *    quizschedule.java -- Servlet entry point for students to schedule quizzes
 *    quizReader.java -- reads XML file and stores in quizzes.
 *                       Used by quizschedule.java
 *    quizzes.java -- A list of quizzes from the XML file
 *                    Used by quizschedule.java
 *    quizBean.java -- A simple quiz bean
 *                     Used by quizzes.java and readQuizzesXML.java
 *    retakesReader.java -- reads XML file and stores in retakes.
 *                          Used by quizschedule.java
 *    retakes.java -- A list of retakes from the XML file
 *                    Used by quizschedule.java
 *    retakeBean.java -- A simple retake bean
 *                       Used by retakes.java and readRetakesXML.java
 *    apptBean.java -- A bean to hold appointments
 *
 *    quizzes.xml -- Data file of when quizzes were given
 *    retakes.xml -- Data file of when retakes are given
 *    course.xml -- Data file with information about the course
 */

public class quizschedule
{
   // Used to pass retakeID,quizID pairs from printQuizScheduleForm() to readInputSave()
   private static Properties retakeQuizIDProps = new Properties(); /* CLI */

   // Data files
   /* CLI: All variables changed to static to use in main() */
   private static final String dataLocation = "quizretakes/"; /* CLI */
   private static final String separator    = ",";
   private static final String courseBase   = "course";
   private static final String quizzesBase  = "quiz-orig";
   private static final String retakesBase  = "quiz-retakes";
   private static final String apptsBase    = "quiz-appts";

   // Filenames to be built from above and the courseID
   private static String courseFileName;
   private static String quizzesFileName;
   private static String retakesFileName;
   private static String apptsFileName;

   // Stored in course.xml file, default 14
   // Number of days a retake is offered after the quiz is given
   private static int daysAvailable = 14;

   //Teacher or student
   private static boolean isTeacher = false;

// ===============================================================
// Prints the form to schedule a retake
public static void main(String []argv) /* CLI */
{
   Scanner sc = new Scanner(System.in); /* CLI */

   // Get course ID from user (could be passed as a command line parameter ...)
   String courseID = readCourseID(sc); /* CLI */
   buildFileNames(courseID); /* CLI */

   // Get information about the course
   courseBean course;
   try {
      course = readCourseFile(courseID);
   } catch(Exception e) {
      System.out.println("Can't find the data files for course ID " + courseID + ". You can try again with a different course ID.");
      return;
   }

   daysAvailable = Integer.parseInt(course.getRetakeDuration());

   try { // Read the files and print the form
      quizzes quizList; /* CLI */
      retakes retakesList; /* CLI */
      quizList    =  readQuizzes(courseID); /* CLI */
      retakesList = readRetakes(courseID); /* CLI */
      // Inside try-block so this won't print if files can't be read
      isTeacher = teacher_or_studendt();
      if (isTeacher == true)
      {
        String choice = quiz_or_retake(sc);
        if (choice.equals("Retake")){
            newRetake(retakesList);
            System.out.println("Thank you professor for adding another retake session");
        }
        else if (choice.equals("Quiz")){
            newQuiz(quizList);
            System.out.println("thank you professor for adding another quiz");
        }
        return;
      }
      printQuizScheduleForm(quizList, retakesList, course);
   } catch(Exception e) {
      System.out.println("Can't read the data files for course ID " + courseID + ". You can try again with a different courseID.");
      return;
   }

   // This replaces the submit-response (was doPost() )
   readInputSave(sc, courseID); /* CLI */
}  // end main()

// ===============================================================
// Was doPost()
// Called from main to read student's choice and save to file
private static void readInputSave(Scanner sc, String courseID) /* CLI */
{
   // Get name and list of retake requests from user
   System.out.print("What is your name? ");
   String studentName = sc.next();

   System.out.print("Enter a number from the list to schedule a retake: "); /* CLI */
   String retake = sc.next(); /* CLI */
   String retakeQuizID = retakeQuizIDProps.getProperty(retake); /* CLI */

   // Append the new appointment to the file
   try {
      File file = new File(apptsFileName);
      if(retakeQuizID != null) /* CLI */
      {  // user must choose one of the numbers on screen
         if(!file.exists())
         {
            file.createNewFile();
         }
         FileWriter     fw = new FileWriter(file.getAbsoluteFile(), true); //append mode
         BufferedWriter bw = new BufferedWriter(fw);

         bw.write(retakeQuizID + separator + studentName + "\n");

         bw.flush();
         bw.close();

         // CLI: simplified the logic in this method somewhat from the servlet version.
         // Respond to the student
         System.out.println("");
         System.out.println(studentName + ", your appointment has been scheduled.");
         System.out.println("Please arrive in time to finish the quiz before the end of the retake period.");
         System.out.println("If you cannot make it, please cancel by sending email to your professor.");
      } else {
         System.out.println("");
         System.out.println("I don't have a retake time for that number. Please try again.");
      }
   } catch(IOException e) {
      System.out.println("");
      System.out.println("I failed and could not save your appointment.\nException message is: " + e);
   }
}

// ===============================================================
/* CLI: Dropped parameter "out", now we print to screen */
// Print the quiz retake choices (maybe should also change the method name?)
private static void printQuizScheduleForm(quizzes quizList, retakes retakesList, courseBean course)
{
   // Check for a week to skip
   boolean skip = false;
   LocalDate startSkip = course.getStartSkip();
   LocalDate endSkip   = course.getEndSkip();

   System.out.println("");
   System.out.println("");
   System.out.println("******************************************************************************");
   System.out.println("GMU quiz retake scheduler for class " + course.getCourseTitle());
   System.out.println("******************************************************************************");
   System.out.println("");
   System.out.println("");

   // print the main form
   System.out.println("You can sign up for quiz retakes within the next two weeks. ");
   System.out.println("Enter your name (as it appears on the class roster), ");
   System.out.println("then select which date, time, and quiz you wish to retake from the following list.");
   System.out.println("");

   LocalDate today  = LocalDate.now();
   LocalDate endDay = today.plusDays(new Long(daysAvailable));
   LocalDate origEndDay = endDay;
   // if endDay is between startSkip and endSkip, add 7 to endDay
   if(!endDay.isBefore(startSkip) && !endDay.isAfter(endSkip))
   {  // endDay is in a skip week, add 7 to endDay
      endDay = endDay.plusDays(new Long(7));
      skip = true;
   }

   System.out.print  ("Today is ");
   System.out.println((today.getDayOfWeek()) + ", " + today.getMonth() + " " + today.getDayOfMonth() );
   System.out.print  ("Currently scheduling quizzes for the next two weeks, until ");
   System.out.println((endDay.getDayOfWeek()) + ", " + endDay.getMonth() + " " + endDay.getDayOfMonth() );
   System.out.print("");

   // Unique integer for each retake and quiz pair
   int quizRetakeCount = 0; /* CLI */
   for(retakeBean r: retakesList)
   {
      LocalDate retakeDay = r.getDate();
      if(!(retakeDay.isBefore(today)) && !(retakeDay.isAfter(endDay)))
      {
         // if skip && retakeDay is after the skip week, print a message
         if(skip && retakeDay.isAfter(origEndDay))
         {  // A "skip" week such as spring break.
            System.out.println("      Skipping a week, no quiz or retakes.");
            // Just print for the FIRST retake day after the skip week
            skip = false;
         }
         // format: Friday, January 12, at 10:00am in EB 4430
         System.out.println("RETAKE: " + retakeDay.getDayOfWeek() + ", " +
                            retakeDay.getMonth() + " " +
                            retakeDay.getDayOfMonth() + ", at " +
                            r.timeAsString() + " in " +
                            r.getLocation());

         for(quizBean q: quizList)
         {
            LocalDate quizDay = q.getDate();
            LocalDate lastAvailableDay = quizDay.plusDays(new Long(daysAvailable));
            // To retake a quiz on a given retake day, the retake day must be within two ranges:
            // quizDay <= retakeDay <= lastAvailableDay --> (!quizDay > retakeDay) && !(retakeDay > lastAvailableDay)
            // today <= retakeDay <= endDay --> !(today > retakeDay) && !(retakeDay > endDay)
            if(!quizDay.isAfter(retakeDay) && !retakeDay.isAfter(lastAvailableDay) &&
                !today.isAfter(retakeDay) && !retakeDay.isAfter(endDay))
            {
               quizRetakeCount++; /* CLI */
               // Put in a properties structure for writing to retake schedule file (CLI)
               retakeQuizIDProps.setProperty(String.valueOf(quizRetakeCount), r.getID() + separator + q.getID()); /* CLI */
               System.out.print  ("    " + quizRetakeCount + ") "); /* CLI */
               System.out.println("Quiz " + q.getID() + " from " + quizDay.getDayOfWeek() + ", " + quizDay.getMonth() + " " + quizDay.getDayOfMonth() );
            }
         }
      }
   }
   System.out.println("");
}

// ===============================================================
// adds a new quiz
// originally put into string and returned the xml form of the new quiz
//refactored to now update the xml and no need to return anything anymore
private static void newQuiz(quizzes quizList) throws Exception
{
    Scanner scan = new Scanner(System.in);
    int newQuizID = getLastQuizID(quizList) + 1;
    //String newQuizXML = "<quiz><id>" + newQuizID + "</id>";
    int[] dayMonth = getNewDate(scan);
    //newQuizXML += "<dateGiven><month>" + dayMonth[1] + "</month><day>" + dayMonth[0] + "</day>";
    //newQuizXML += "<hour>" + dayMonth[2] + "</hour><minute>"+ dayMonth[3] + "</minute></dateGiven></quiz>";

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
            .newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory
            .newDocumentBuilder();
    Document document = documentBuilder.parse(quizzesFileName);

    Element root = document.getDocumentElement();
    Element rootElement = document.getDocumentElement();
    Collection<quizBean> quiz = new ArrayList<quizBean>();
    quiz.add(new quizBean(newQuizID,dayMonth[1],dayMonth[0],dayMonth[2],dayMonth[3]));

    for (quizBean i : quiz){
        Element quiz1 = document.createElement("quiz");
        rootElement.appendChild(quiz1);

        Element id = document.createElement("id");
        id.appendChild(document.createTextNode(Integer.toString(newQuizID)));
        quiz1.appendChild(id);

        Element dateGiven = document.createElement("dateGiven");
        quiz1.appendChild(dateGiven);

        Element month = document.createElement("month");
        month.appendChild(document.createTextNode(Integer.toString(dayMonth[1])));
        dateGiven.appendChild(month);

        Element day = document.createElement("day");
        day.appendChild(document.createTextNode(Integer.toString(dayMonth[0])));
        dateGiven.appendChild(day);

        Element hour = document.createElement("hour");
        hour.appendChild(document.createTextNode(Integer.toString(dayMonth[2])));
        dateGiven.appendChild(hour);

        Element minute = document.createElement("minute");
        minute.appendChild(document.createTextNode(Integer.toString(dayMonth[3])));
        dateGiven.appendChild(minute);

        root.appendChild(quiz1);
    }

    DOMSource source = new DOMSource(document);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StreamResult result = new StreamResult(quizzesFileName);
    transformer.transform(source, result);

}


// ===============================================================
// adds a new retake
// originally just returned the a string form of the xml back to the caller
// now updates the xml and does not need to return anything
// leading to a refactor in the tests as some are now invalid
private static void newRetake(retakes retakeList) throws Exception
{
    Scanner scan = new Scanner(System.in);
    int newQuizID = getLastRetakeID(retakeList) + 1;
    //String newRetakeXML = "<retake><id>" + newQuizID + "</id>";
    String location = getRetakeLocation(scan);
    //newRetakeXML += "<location>" + location + "</location>";
    int[] dayMonth = getNewDate(scan);
    //newRetakeXML += "<dateGiven><month>" + dayMonth[1] + "</month><day>" + dayMonth[0] + "</day>";
    //newRetakeXML += "<hour>" + dayMonth[2] + "</hour><minute>"+ dayMonth[3] + "</minute></dateGiven></retake>";
    //quizBean newQuizBean = new quizBean(newQuizID,dayMonth[1],dayMonth[0],dayMonth[2],dayMonth[3]);
    //quizList.addQuiz(newQuizBean);

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
            .newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory
            .newDocumentBuilder();
    Document document = documentBuilder.parse(retakesFileName);

    Element root = document.getDocumentElement();
    Element rootElement = document.getDocumentElement();
    Collection<retakeBean> retake = new ArrayList<retakeBean>();
    retake.add(new retakeBean(newQuizID,location,dayMonth[1],dayMonth[0],dayMonth[2],dayMonth[3]));

    for (retakeBean i : retake){
        Element retake1 = document.createElement("retake");
        rootElement.appendChild(retake1);

        Element id = document.createElement("id");
        id.appendChild(document.createTextNode(Integer.toString(newQuizID)));
        retake1.appendChild(id);

        Element retakeloc = document.createElement("location");
        retakeloc.appendChild(document.createTextNode(location));
        retake1.appendChild(retakeloc);


        Element dateGiven = document.createElement("dateGiven");
        retake1.appendChild(dateGiven);

        Element month = document.createElement("month");
        month.appendChild(document.createTextNode(Integer.toString(dayMonth[1])));
        dateGiven.appendChild(month);

        Element day = document.createElement("day");
        day.appendChild(document.createTextNode(Integer.toString(dayMonth[0])));
        dateGiven.appendChild(day);

        Element hour = document.createElement("hour");
        hour.appendChild(document.createTextNode(Integer.toString(dayMonth[2])));
        dateGiven.appendChild(hour);

        Element minute = document.createElement("minute");
        minute.appendChild(document.createTextNode(Integer.toString(dayMonth[3])));
        dateGiven.appendChild(minute);

        root.appendChild(retake1);
    }

    DOMSource source = new DOMSource(document);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    StreamResult result = new StreamResult(retakesFileName);
    transformer.transform(source, result);


}

// ===============================================================
private static String quiz_or_retake(Scanner scan) throws  Exception
{
    System.out.println("Welcome Profesor.");
    System.out.println("Please select and option below:");
    System.out.println("Enter 1 if you would like to add a retake session.");
    System.out.println("Enter 2 if you would like to add a quiz session.");
    int choice = scan.nextInt();
    if (choice == 1) {
        return "Retake";
    }
    else if (choice == 2){
        return "Quiz";
    }
    else{
        throw new IllegalArgumentException("Must enter a 1 or 2");
    }

}



/*
// ===============================================================
// gets the time of the quiz or retake  from the teacher
// refactored the code to have the same function getting both the date and time to localize the process
    private static int[] getNewTime()
    {

    /*
        //Hard code
        int[] returnVal = new int[] {15,30};
        return returnVal;

        Scanner scan = new Scanner(System.in);
        // added both in a while loop until they get a valin input
        int hour = -1;
        int minute = -1;
        while (hour >23 || hour < 0) {
            System.out.print("Please enter the hour for the Quiz or Retake( 1 - 23 ): ");
            hour = scan.nextInt();
        }
        while (minute < 0 || minute > 59) {
            System.out.print("Please enter the minute of the new Quiz or Retake ( 0 - 59: ");
            minute = scan.nextInt();
        }
        int[] returnVal = new int[]{hour,minute};
        return returnVal;
    }

    this code is being merged into the get new date as it is to hard to test test them together since they take in user input
*/


// ===============================================================
// gets the date from the teacher
private static int[] getNewDate(Scanner scan)
{
    /*
    //Hard code
    int[] returnVal = new int[] {3,4};
    return returnVal;
    */
    // added both in a while loop until they get a valin input
    int day = 0;
    int month = 0;
    while (day >31 || day < 1) {
        System.out.print("Please enter the day for the Quiz or Retake( 1 - 31 ): ");
        day = scan.nextInt();
    }
    while (month < 1 || month > 12) {
        System.out.print("Please enter the month of the new Quiz or Retake: ");
        month = scan.nextInt();
    }
    int hour = -1;
    int minute = -1;
    while (hour >23 || hour < 0) {
        System.out.print("Please enter the hour for the Quiz or Retake( 1 - 23 ): ");
        hour = scan.nextInt();
    }
    while (minute < 0 || minute > 59) {
        System.out.print("Please enter the minute of the new Quiz or Retake ( 0 - 59: ");
        minute = scan.nextInt();
    }
    int[] returnVal = new int[]{day,month,hour,minute};
    return returnVal;
}
// ===============================================================
// gets the location from the teacher
private static String getRetakeLocation(Scanner scan)
{
    /*
    return "EB 5321";
    */
    System.out.println("Please enter the location of the retake: ");
    String location = scan.nextLine();
    while (location.equals("") || location.equals("\n"))
    {
        location = scan.nextLine();
    }
    return location;

}
// ===============================================================
// grabs the latest quiz id
    private static int getLastRetakeID(retakes retakeList)
    {
    /*
    return 10;
     */
        int lastID = 0;
        // added for check
        if (retakeList == null)
        {
            throw new IllegalArgumentException("retake List was null.");
        }
        for (retakeBean r : retakeList)
        {
            if (lastID < r.getID()) {
                lastID = r.getID();
            }
        }
        return lastID;
    }
// ===============================================================
// grabs the latest retake id
private static int getLastQuizID(quizzes quizList)
{
    /*
    return 10;
     */
    int lastID = 0;
    // added for check
    if (quizList == null)
    {
        throw new IllegalArgumentException("retake List was null.");
    }
    for (quizBean q : quizList)
    {
        if (lastID < q.getID()) {
            lastID = q.getID();
        }
    }
    return lastID;
}

// ===============================================================
// asks if user is a teacher or student adding because second test failed
private static boolean teacher_or_studendt() throws  Exception
{
   Scanner scan = new Scanner(System.in);
   String response;
   System.out.print("Are you a teacher: [enter Yes or No]");
   response = scan.next();
    return user_type(response);
}

//part of my spike has not function in the overall operation of the code
private static String double_scan(){
    Scanner scan = new Scanner(System.in);
    String response;
    response = scan.next();
    response += " " + spike(scan);
    return response;
}
private static String spike(Scanner scan){
    String response = scan.next();
    return response;
}

private static boolean user_type(String response)throws Exception
{
    if (response.equals("Yes") || response.equals("Y") ||response.equals("yes") || response.equals("y") ){
        return true;
    }
    else if (response.equals("No") || response.equals("N") || response.equals("no") || response.equals("n")){
        return false;
    }
    else{
        throw new IllegalArgumentException("Must enter a Yes or No");
    }
}

// ===============================================================
// Build the file names in one place to make them easier to change
private static void buildFileNames(String courseID) /* CLI */
{
   courseFileName  = dataLocation + courseBase  + "-" + courseID + ".xml"; /* CLI */
   quizzesFileName = dataLocation + quizzesBase + "-" + courseID + ".xml"; /* CLI */
   retakesFileName = dataLocation + retakesBase + "-" + courseID + ".xml"; /* CLI */
   apptsFileName   = dataLocation + apptsBase   + "-" + courseID + ".txt"; /* CLI */
}

// ===============================================================
// Get the course ID from the user
private static String readCourseID(Scanner sc) /* CLI */
{
   System.out.print("Enter courseID: "); /* CLI */
   return(sc.next()); /* CLI */
}

// ===============================================================
// Read the course file
private static courseBean readCourseFile(String courseID) throws Exception /* CLI */
{
   courseBean course; /* CLI */
   courseReader cr = new courseReader(); /* CLI */
   course          = cr.read(courseFileName);
   return(course); /* CLI */
}

// ===============================================================
// Read the quizzes file
private static quizzes readQuizzes(String courseID) throws Exception /* CLI */
{
   quizzes quizList = new quizzes(); /* CLI */
   quizReader qr    = new quizReader(); /* CLI */
   quizList         = qr.read(quizzesFileName); /* CLI */
   return(quizList); /* CLI */
}

// ===============================================================
// Read the retakes file
private static retakes readRetakes(String courseID) throws Exception /* CLI */
{
   retakes retakesList = new retakes();
   retakesReader rr    = new retakesReader();
   retakesList         = rr.read(retakesFileName);
   return(retakesList); /* CLI */
}

} // end quizschedule class
