# Lexical Analyser for Swift programming language

Specification for each token is provided in description.txt file 

The application is guaranteed to work with Swift4.2 version 

## Description
The application will read source code from "in.txt" file and try to evaluate them, writing the results to "out.txt".
The expected "in.txt" is a file with source code, "out.txt" contains all found tokens or list with errors.

## Getting Started

To build the project from console, navigate to the root folder and type:
`javac -d bin -sourcepath src src/Main.java`

To build the jar archive, run:
`jar cvf bin/[DesiredName].jar src/Manifest.txt bin/*.class`

IDE builds can vary, see the respective IDE documentation for building.

### Dependencies

JRE 1.8
JUnit 5 for the tests


## Running the tests

To build and run the tests, navigate to the root folder and type:
'javac -d bin -sourcepath src -cp src/junit-platform-console-standalone-1.1.0.jar src/ParserTest.java'

To run the tests, frm the root folder:
'java -jar src/junit-platform-console-standalone-1.1.0.jar -cp bin/ -c ParserTest'

IDE testing can vary, see the respective IDE documentation for running the tests.

## Running

To run the application, create the "in.txt" file with the expressions to parse. Run from the "bin" folder

To run the class file from console:
`java Main`

To run the jar file:
`java -jar [DesiredName].jar`

The application will generate the "out.txt" file containing the result tokens or list with errors.



