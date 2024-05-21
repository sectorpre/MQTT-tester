# MQTT-tester
Csv files generated for my execution of the program is within assignment\ 3/

To run the code
1) cd assignment\ 3/
2) mvn package
3) mvn compile exec:java -Dexec.mainClass="org.example.Publisher" (This runs the 5 instances of Publisher)
4) mvn compile exec:java -Dexec.mainClass="org.example.Subscriber" (This runs a subscriber to $SYS topics)
5) mvn compile exec:java -Dexec.mainClass="org.example.Analyser" (This runs the Analyser which will send queries to the broker) 
