![hangTheLampions](https://i.imgur.com/lTxnXAu.png)

## Background

Karel The Robot is a simple teaching environment for imperative programming basics.
The original idea was developed in the 1970s by Richard Pattis at Stanford University.

This project started in 2012 due to dissatisfaction with the available Karel environments,
and also to gain practical experience with [Scala](https://www.scala-lang.org).
Since then, thousands of German university students have been introduced to the basics of imperative programming via this project.

In 2017, the Scala code was migrated to [Kotlin](https://kotlinlang.org), hoping to simplify future maintenance.

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

Karel The Robot requires Java 8 or newer to run. Make sure you have Java installed!

On Windows, you can simply run a jar by double-clicking on it.

On other operating systems, open a terminal where the jar lives and write:

    java -jar karel.jar

Java animations tend to stutter on Linux.
Replacing `xrender` with `opengl` may help:

    java -jar -Dsun.java2d.opengl=True karel.jar

## How do I save my code?

The code is automatically saved to a new file each time you click the start button.
The save folder is named `karel`, and it is located in your home directory.
The full path is displayed in the title bar.

## Keyboard Shortcuts

```
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
```

## How do I install IntelliJ IDEA?

Download the Community Edition `zip` or `tar.gz` from https://www.jetbrains.com/idea/download and extract it wherever you like.
Navigate to the `bin` folder and run the `idea.bat` or `idea.sh` script.
Then follow these instructions:

```
Import IntelliJ IDEA Settings From...
(o) Do not import settings
OK

JetBrains Privacy Policy
[x] I confirm that I have read and accept the terms of this User Agreement
Continue

Data Sharing
Don't send

Skip Remaining and Set Defaults
```

## How do I import karel into IntelliJ IDEA?

* If there are no projects open, pick the **Import Project** option from the *Welcome to IntelliJ IDEA* screen.
* Otherwise, pick **File > New > Project from Existing Sources...**

```
Windows: C:\Users\fred\git\karel
Linux: /home/fred/git/karel
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
Windows: C:\Program Files\Java\jdk1.8.0_...
Linux: /usr/lib/jvm/java-8-openjdk-amd64
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

## What IntelliJ IDEA settings do you like to change after install?

**File > Settings...**

* Keymap
  * Eclipse
* Editor > Font
  * Font: Source Code Pro
  * Size: 16
* Editor > General > Code Folding
  * [ ] One-line methods
* Editor > General > Appearance
  * [ ] Caret blinking
  * [ ] Show intention bulb
