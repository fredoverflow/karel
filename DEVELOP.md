## How do I compile Karel from source?

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

## How do I install IntelliJ IDEA?

Download the `zip` or `tar.gz` from https://www.jetbrains.com/idea/download and extract it wherever you like.
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

## How do I import Karel into IntelliJ IDEA?

- If there are no projects open, pick the **Import Project** option from the *Welcome to IntelliJ IDEA* screen.
- Otherwise, pick **File > New > Project from Existing Sources...**

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

## How do I run Karel from within IntelliJ IDEA?

```
karel/src/main/kotlin/Main.kt (right-click)
Run 'MainKt'
```

## Which IntelliJ IDEA settings do you like to change after install?

**File > Settings...**

- Appearance & Behavior > Appearance
  - Zoom: 150%
- Keymap
  - Eclipse
- Editor > Font
  - Font: Fira Code (`sudo apt install fonts-firacode`)
- Editor > General > Code Folding
  - [ ] One-line methods
- Editor > General > Appearance
  - [ ] Caret blinking
  - [ ] Show intention bulb
