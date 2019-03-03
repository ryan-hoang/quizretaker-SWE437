# Initial target servlet front end
quizschedule : quizschedule.class
quizschedule.class : quizschedule.java retakeBean.class retakes.class retakesReader.class quizBean.class quizzes.class quizReader.class apptBean.class apptsReader.class courseBean.class courseReader.class printQuizScheduleFormTests.class retakeTest.class
	cd ..; javac quizretakes/quizschedule.java ; cd quizretakes

quizReader.class : quizReader.java quizzes.class quizBean.class
	cd ..; javac quizretakes/quizReader.java ; cd quizretakes

retakesReader.class : retakesReader.java retakes.class retakeBean.class
	cd ..; javac quizretakes/retakesReader.java ; cd quizretakes

quizzes.class : quizzes.java quizBean.class
	cd ..; javac quizretakes/quizzes.java ; cd quizretakes

quizBean.class : quizBean.java
	cd ..; javac quizretakes/quizBean.java ; cd quizretakes

retakes.class : retakes.java retakeBean.class
	cd ..; javac quizretakes/retakes.java ; cd quizretakes

retakeBean.class : retakeBean.java
	cd ..; javac quizretakes/retakeBean.java ; cd quizretakes

apptsReader.class : apptsReader.java apptBean.class
	cd ..; javac quizretakes/apptsReader.java ; cd quizretakes

apptBean.class : apptBean.java
	cd ..; javac quizretakes/apptBean.java ; cd quizretakes

courseReader.class : courseReader.java courseBean.class
	cd ..; javac quizretakes/courseReader.java ; cd quizretakes

courseBean.class : courseBean.java
	cd ..; javac quizretakes/courseBean.java ; cd quizretakes 
printQuizScheduleFormTests.class : printQuizScheduleFormTests.java
	cd ..; javac -Xlint:unchecked quizretakes/printQuizScheduleFormTests.java ; cd quizretakes
retakeTest.class : retakeTest.java
	cd ..; javac -Xlint:unchecked quizretakes/retakeTest.java ; cd quizretakes
