![hangTheLampions](https://i.imgur.com/lTxnXAu.png)

## Background

Karel The Robot is a simple teaching environment for imperative programming basics.
The original idea was developed in the 1970s by Richard Pattis at Stanford University.

This project started in 2012 due to dissatisfaction with the available Karel environments,
and also to gain practical experience with [Scala](http://www.scala-lang.org).
It ended up being used to teach the basics of imperative programming to 1200+ university students.

In 2017, the project was migrated from Scala to [Kotlin](https://kotlinlang.org) to simplify future maintenance.

## How do I compile karel into an executable jar?
```
git clone https://github.com/fredoverflow/freditor
cd freditor
mvn install
cd ..
git clone https://github.com/fredoverflow/karel
cd karel
mvn package
```
The executable `karel.jar` will be located inside the `target` folder.

## How do I start the system?

Karel The Robot requires Java 7 or newer to run. Make sure you have Java installed!

On most operating systems, you can simply run a jar by double-clicking on it.

If double-clicking does not start the system, open a terminal where the jar lives and write:

    java -jar karel.jar

## How do I save my code?

The code is automatically saved to a new file each time you click the start button.
The save folder is named `karel`, and it is located in your home directory.
The full path is displayed in the title bar.

## Keyboard Shortcuts

    F1    moveForward();
    F2    turnLeft();
    F3    turnAround();
    F4    turnRight();
    F5    pickBeeper();
    F6    dropBeeper();

    F7    onBeeper()
    F8    beeperAhead()
    F9    leftIsClear()
    F10   frontIsClear()
    F11   rightIsClear()

    F12   start / step into / reset

    Tab or Enter   auto-indent
    Ctrl Space     auto-complete
    Ctrl D         delete line

    Ctrl C         copy
    Ctrl X         cut
    Ctrl V         paste

    Ctrl Z         undo
    Ctrl Y         redo

## How do I install IntelliJ IDEA?

Download the Community Edition `zip` or `tar.gz` from https://www.jetbrains.com/idea/download and extract it wherever you like.
Navigate to the `bin` folder and run the `idea.bat` or `idea.sh` script.
Then follow these instructions:
```
Complete Installation
(o) Do not import settings
OK

JetBrains Privacy Policy
Accept

Data Sharing Options
OK

Set UI theme

Next: Desktop Entry

Next: Launcher Script

Next: Default Plugins

Next: Featured Plugins

Start using IntelliJ IDEA
```

## How do I import karel into IntelliJ IDEA?
```
Welcome to IntelliJ IDEA
Import Project
Linux: /home/fred/git/karel
Windows: C:\Users\fred\git\karel
OK

Import Project
(o) Import project from external model
Maven
Next
Next
Next

Please select project SDK. This SDK will be used by default by all project modules.
+
JDK
Linux: /usr/lib/jvm/java-8-openjdk-amd64
Windows: C:\Program Files\Java\jdk1.8.0_...
OK
Next
Finish

Tip of the Day
[ ] Show tips on startup
Close
```

## How do I start karel from within IntelliJ IDEA?

```
karel/src/main/kotlin/Main.kt (right-click)
Run 'MainKt'
```
