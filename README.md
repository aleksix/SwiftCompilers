# Lexical Analyser for Swift programming language

Specification for each token is provided in description.txt file 

The application is guaranteed to work with Swift4.2 version 

## Description

The application will read expressions from "in.txt" file and try to evaluate them, writing the results to "out.txt".
The expected "in.txt" is a file with line-separated expressions, "out.txt" contains the results, with each line corresponding to the line in "in.txt"
The application supports bracketed expressions and the following operators: "+", "-", "*", ">", "<", "="
Comparison operators produce numerical representations of boolean values, 1 for "True" and 0 for "False"

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

The application will generate the "out.txt" file containing the result of the evaluation, if any.



