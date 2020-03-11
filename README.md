# simbox_failure_jenkins
prova java con lancio poi su jenkins


Il lancio deve essere fatto includendo un proxy del tipo:

java -Dhttps.proxyHost=ap028rco -Dhttps.proxyPort=808 -Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2 -jar simbox-failure-jenkins-1.0-SNAPSHOT-jar-with-dependencies.jar
