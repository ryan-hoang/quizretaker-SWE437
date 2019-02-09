// JO 3-Jan-2019
package quizretakes;

import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.time.*;
import java.lang.Long;
import java.lang.String;
import java.lang.NumberFormatException;

import  java.util.*;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.BufferedReader;


/**
 * @author Jeff Offutt
 *         Date: January, 2019
 *
 * Wiring the pieces together:
 *    quizschedule.java -- Servlet entry point for students to schedule quizzes
 *    quizReader.java -- reads XML file and stores in quizzes.
                             Used by quizschedule.java
 *    quizzes.java -- A list of quizzes from the XML file
 *                    Used by quizschedule.java
 *    quizBean.java -- A simple quiz bean
 *                      Used by quizzes.java and readQuizzesXML.java
 *    retakesReader.java -- reads XML file and stores in retakes.
                             Used by quizschedule.java
 *    retakes.java -- A list of retakes from the XML file
 *                    Used by quizschedule.java
 *    retakeBean.java -- A simple retake bean
 *                      Used by retakes.java and readRetakesXML.java
 *    apptBean.java -- A bean to hold appointments

 *    quizzes.xml -- Data file of when quizzes were given
 *    retakes.xml -- Data file of when retakes are given
 */

public class quizschedule
{
   // Data files
   // location maps to /webapps/offutt/WEB-INF/data/ from a terminal window.
   // These names show up in all servlets
   // Creates a list of valid sign in options
   private ArrayList<String> optionsTeacher = new ArrayList<String>();
   private ArrayList<String> optionsStudent = new ArrayList<String>();


   private static final String dataLocation    = "quizretakes/data/";
   static private final String separator = ",";
   private static final String courseBase   = "course";
   private static final String quizzesBase = "quiz-orig";
   private static final String retakesBase = "quiz-retakes";
   private static final String apptsBase   = "quiz-appts";
   private static final String menuSeparator = "================================================================================";

   // Filenames to be built from above and the courseID parameter
   private String courseFileName;
   private String quizzesFileName;
   private String retakesFileName;
   private String apptsFileName;

   // Passed as parameter and stored in course.xml file (format: "swe437")
   private String courseID;
   private String signInOption;
   // Stored in course.xml file, default 14
   // Number of days a retake is offered after the quiz is given
   private int daysAvailable = 14;
   
   public quizschedule()
   {}
   
protected void go() throws IOException
{
   PrintWriter out = new PrintWriter(System.out, true);
   Scanner in = new Scanner(System.in);
   courseBean course = null;
   
   // Filenames to be built from above and the courseID
   String quizzesFileName = "";
   String retakesFileName = "";
   String apptsFileName = "";


   // addes the valid options for student and teacher
   optionsTeacher.add("t");
   optionsTeacher.add("T");
   optionsTeacher.add("teacher");
   optionsTeacher.add("Teacher");
   optionsStudent.add("s");
   optionsStudent.add("S");
   optionsStudent.add("student");
   optionsStudent.add("Student");


   // Load the quizzes and the retake times from disk
   quizzes quizList    = new quizzes();
   retakes retakesList = new retakes();
   quizReader qr = new quizReader();
   retakesReader rr = new retakesReader();
   boolean filesValid;
   boolean validOption = true;
   boolean badInput = false;
   do
   {
       if (!validOption)
       {
           out.println("Invalid entry. Valid entries are T, t, Teacher, teacher, S, s, Student, student.");
       }
       validOption = true;
       out.println("Please enter if you are a teacher or a student."); //Prompt for student or teacher view\
       signInOption = in.nextLine();
       if (optionsTeacher.contains(signInOption) == false && optionsStudent.contains(signInOption) == false) {
           validOption = false;
           System.out.print("\033[2K");
           System.out.print(String.format("\033[%dA",1)); // Move up
           System.out.print("\033[2K");
           System.out.print(String.format("\033[%dA",2)); // Move up
           System.out.print("\033[2K");


       }
   }while(!validOption);
   do {
       filesValid = true;
       out.println("Please enter a valid course ID."); //Prompt for CourseID
       courseID = in.nextLine();
       courseReader cr = new courseReader();
       courseFileName = dataLocation + courseBase + "-" + courseID + ".xml";
       
       //Try to read in Course xml file.
       try 
       {
          course = cr.read(courseFileName);
          daysAvailable = Integer.parseInt(course.getRetakeDuration());
          // Filenames to be built from above and the courseID
          quizzesFileName = dataLocation + quizzesBase + "-" + courseID + ".xml";
          retakesFileName = dataLocation + retakesBase + "-" + courseID + ".xml";
          apptsFileName   = dataLocation + apptsBase   + "-" + courseID + ".txt";
       } 
       catch (Exception e) {
          out.println("Can't find course data file for " + courseID + ".");
          filesValid = false;
          continue;
       }
       
       //Try to read in Quiz xml file.
       try 
       {
          quizList = qr.read (quizzesFileName);
       } 
       catch (Exception e)
       {
          out.println("Can't find quiz data file for " + courseID + ".");
          filesValid = false;
       }
       
       //Try to read in Retake xml file.
       try 
       {
          retakesList = rr.read (retakesFileName);
       } 
       catch (Exception e)
       {
          out.println("Can't find retakes data file for " + courseID + ".");
          filesValid = false;
       }  
       
       if(!filesValid)
       {
         out.println("Cannot find all necessary data files for CourseID: " + courseID + ".");
       }
       
    } while (!filesValid);
   if (optionsStudent.contains(signInOption))
   {
       printQuizScheduleForm(out, in, quizList, retakesList, course, signInOption);
   }
   else
   {
	   printQuizScheduleForm(out,in,quizList, retakesList, course, signInOption);
   }
}


// doPost saves an appointment in a file and prints an acknowledgement
protected void makeAppointment (String name, ArrayList<String> ids, String courseID) throws IOException
{
   // No saving if IOException
   boolean IOerrFlag = false;
   String IOerrMessage = "";

   // Filename to be built from above and the courseID
   String apptsFileName   = dataLocation + apptsBase + "-" + courseID + ".txt";

   // Get name and list of retake requests from parameters
   String studentName = name;
   String[] allIDs = ids.toArray(new String[ids.size()]);

   PrintWriter out = new PrintWriter(System.out,true);

   if(allIDs != null && studentName != null && studentName.length() > 0)
   {
      // Append the new appointment to the file
      try {
         File file = new File(apptsFileName);
         synchronized(file)
         { // Only one student should touch this file at a time.
            if (!file.exists())
            {
               file.createNewFile();
            }
            FileWriter     fw = new FileWriter(file.getAbsoluteFile(), true); //append mode
            BufferedWriter bw = new BufferedWriter(fw);

            for(String oneIDPair : allIDs)
            {
               bw.write(oneIDPair + separator + studentName + "\n");
            }

            bw.flush();
            bw.close();
         } // end synchronize block
      } catch (IOException e) {
         IOerrFlag = true;
         IOerrMessage = "I failed and could not save your appointment." + e;
      }

      // Respond to the student
      if (IOerrFlag)
      {
         out.println (IOerrMessage);
      } else {
         if (allIDs.length == 1)
            out.println (studentName + ", your appointment has been scheduled.");
         else
            out.println (studentName + ", your appointments have been scheduled.");
         out.println ("Please arrive in time to finish the quiz before the end of the retake period.");
         out.println ("If you cannot make it, please cancel by sending email to your professor.");
      }

   } else { // allIDs == null or name is null
      if(allIDs == null)
         out.println ("You didn't choose any quizzes to retake.");
      if(studentName == null || studentName.length() == 0)
         out.println ("You didn't give a name ... no anonymous quiz retakes.");
      out.println("You can try again if you like.");
   }

}
    /*
    * This is a very fancy method that will take a 2d string array
    * and sort it based on on of the columns
    * DO NOT USE THIS ON ANY COLUMNS OTHER THAN 0 OR 1
    * the column has to contain a number since it
    *
    * */
    public static void sortbyColumn(String arr[][], int col){
        Arrays.sort(arr, new Comparator<String[]>(){
            @Override
            public int compare (final String[] entry1, final String[] entry2){
                if (entry1[col] == null) { return 0;}
                if ( Integer.parseInt(entry1[col]) > Integer.parseInt(entry2[col])){
                    return 1;
                }
                else {
                    return -1;
                }
            }

        });
    }

protected void printQuizScheduleForm (PrintWriter out, Scanner in, quizzes quizList, retakes retakesList, courseBean course, String signInOption) throws IOException
{
   String studentName = "";
   String viewOption = "";
   int retakeID;
   int quizID;


   String[][] schRetakes = new String[50][3];
   String temp = "";
   String[] tempList = new String[3];
   File file = new File("./quizretakes/data/quiz-appts-swe437.txt");
   Scanner fileRead = new Scanner(file);
   int index = 0;
   while (fileRead.hasNextLine())
   {
       temp = fileRead.nextLine();
       tempList = temp.split(",");
       schRetakes[index] = tempList;
       index++;
   }
   sortbyColumn(schRetakes,0);



   // maps retake IDs to lists of valid quiz IDs. Used to perform check at the end to ensure the user entered a valid session/quiz pair.
   HashMap<Integer, ArrayList<Integer>> retakeQuizMap = new HashMap<Integer, ArrayList<Integer>>();

   // Check for a week to skip
   LocalDate startSkip = course.getStartSkip();
   LocalDate endSkip = course.getEndSkip();

    LocalDate today = LocalDate.now();
    LocalDate endDay = today.plusDays(new Long(daysAvailable));
    LocalDate origEndDay = endDay;
    // if endDay is between startSkip and endSkip, add 7 to endDay
    if (!endDay.isBefore(startSkip) && !endDay.isAfter(endSkip)) {  // endDay is in a skip week, add 7 to endDay
        endDay = endDay.plusDays(new Long(7));
    }

   if (optionsTeacher.contains(signInOption))
   {
       out.println("\nGMU quiz retake scheduler for " + course.getCourseTitle() + ".");
       out.print("Todays date is ");
       out.print((today.getDayOfWeek()) + ", " + today.getMonth() + " " + today.getDayOfMonth());
       out.println("\nWelcome professor, please choose a selection.");
       out.println("Enter \"1\" if you want the list of all quiz retakes that still need to be completed.");
       out.println("Enter \"2\" if you want the full list of all past and future quiz retakes.");

       viewOption = in.nextLine();

       while (!viewOption.equals("1") && !viewOption.equals("2"))
       {
           out.println("Invalid Entry please read the menu provided above.");
           viewOption = in.nextLine();
           System.out.print(String.format("\033[%dA",1)); // Move up
           System.out.print("\033[2K");
           System.out.print(String.format("\033[%dA",1)); // Move up
           System.out.print("\033[2K");
       }
       if (viewOption.equals("1"))
       {
           for (retakeBean r : retakesList)
           {
               LocalDate retakeDay = r.getDate();
               if (!(retakeDay.isBefore(today)) && !(retakeDay.isAfter(endDay)))
               {
                   out.println(menuSeparator);
                   out.println("Retake Session: " + r.getID() + " takes place on " + retakeDay.getDayOfWeek() + ", " +
                           retakeDay.getMonth() + " " +
                           retakeDay.getDayOfMonth() + ", at " +
                           r.timeAsString() + " in " +
                           r.getLocation());
                   out.println(menuSeparator);

                   retakeQuizMap.put(r.getID(), new ArrayList<Integer>());
               }
           }
       }


   }
   //the student view of the retake application allows for signing up
   else if (optionsStudent.contains(signInOption))
   {
       out.println("\nGMU quiz retake scheduler for " + course.getCourseTitle() + ".");
       out.println("You can sign up for quiz retakes within the next two weeks.");
       out.print("\nEnter your name (as it appears on the class roster), ");
       out.print("then select which date, time, and quiz you wish to retake from the following list.\n");



       out.print("\n  Today is ");
       out.print((today.getDayOfWeek()) + ", " + today.getMonth() + " " + today.getDayOfMonth());
       out.print("  Currently scheduling quizzes for the next two weeks, until ");
       out.println((endDay.getDayOfWeek()) + ", " + endDay.getMonth() + " " + endDay.getDayOfMonth());

       out.println("\nEnter Your Name: ");
       studentName = in.nextLine();

       for (retakeBean r : retakesList)
       {
           LocalDate retakeDay = r.getDate();
           if (!(retakeDay.isBefore(today)) && !(retakeDay.isAfter(endDay))) {
        /*
        format:
          ================================================================================
          Retake Session: 2 takes place on Friday, January 12, at 10:00am in EB 4430
          ================================================================================
        */
               out.println(menuSeparator);
               out.println("Retake Session: " + r.getID() + " takes place on " + retakeDay.getDayOfWeek() + ", " +
                       retakeDay.getMonth() + " " +
                       retakeDay.getDayOfMonth() + ", at " +
                       r.timeAsString() + " in " +
                       r.getLocation());
               out.println(menuSeparator);

               retakeQuizMap.put(r.getID(), new ArrayList<Integer>());

               for (quizBean q : quizList) {
                   LocalDate quizDay = q.getDate();
                   LocalDate lastAvailableDay = quizDay.plusDays(new Long(daysAvailable));
                   // To retake a quiz on a given retake day, the retake day must be within two ranges:
                   // quizDay <= retakeDay <= lastAvailableDay --> (!quizDay > retakeDay) && !(retakeDay > lastAvailableDay)
                   // today <= retakeDay <= endDay --> !(today > retakeDay) && !(retakeDay > endDay)

                   if (!quizDay.isAfter(retakeDay) && !retakeDay.isAfter(lastAvailableDay) &&
                           !today.isAfter(retakeDay) && !retakeDay.isAfter(endDay)) {
                       out.println("Quiz " + q.getID() + " from " + quizDay.getDayOfWeek() + ", " + quizDay.getMonth() + " " + quizDay.getDayOfMonth());
                       retakeQuizMap.get(r.getID()).add(q.getID()); // add this quiz to the list of quizzes that the current retake id maps to.
                   }
               }
           }
       }

       out.println("\n");

       boolean done = false;
       boolean failedAlready = false;
       ArrayList<String> idPairList = new ArrayList<String>();
       out.flush();
       while (!done) {
           String idPair = "";

           try {

               if (failedAlready) {
                   System.out.print("\033[2K");
                   System.out.print(String.format("\033[%dA", 2)); // Move up
                   System.out.print("\033[2K");
                   out.println("That is not a valid selection.");
               }
               out.println("Please enter which retake session you would like to attend: ");
               retakeID = Integer.parseInt(in.nextLine());

               out.println("Please enter which quiz you would like to retake.");
               quizID = Integer.parseInt(in.nextLine());
           } catch (NumberFormatException e) {
               failedAlready = true;
               System.out.print("\033[2K");
               System.out.print(String.format("\033[%dA", 2)); // Move up
               out.println("That is not a valid selection.");
               System.out.print("\033[2K");
               continue;
           }

           failedAlready = false;

           if ((retakeQuizMap.get(retakeID) != null) && retakeQuizMap.get(retakeID).contains(quizID)) {
               idPair = retakeID + "," + quizID;
               idPairList.add(idPair);
           } else {
               if (failedAlready) {
                   System.out.print(String.format("\033[%dA", 3));
                   System.out.println("\033[2K");
                   System.out.println("\033[2K");

                   System.out.println("\033[2K");
                   System.out.print(String.format("\033[%dA", 4));
                   System.out.print("\033[2K");
                   out.println("That is not a valid selection. please try again.");
                   System.out.println("\033[2K");
                   System.out.println("\033[2K");
                   System.out.print("\033[2K");
                   continue;
               } else {
                   failedAlready = true;
                   System.out.print("\033[2K");
                   System.out.print(String.format("\033[%dA", 4));
                   System.out.print("\033[2K");
                   out.println("That is not a valid selection. please try again.");
                   System.out.println("\033[2K");
                   System.out.println("\033[2K");
                   System.out.print("\033[2K");
                   continue;
               }
           }

           out.println("Would you like to make another selection? (y/n)");
           String input = in.next();
           if (input.equalsIgnoreCase("n")) {
               done = true;
           }
       }
       makeAppointment(studentName, idPairList, course.getCourseID());
   }
}


} // end quizschedule class
