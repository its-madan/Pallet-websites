@echo off
echo Starting Pallet Builder Backend with MongoDB Atlas configuration...

set JAVA_OPTS=-Djavax.net.ssl.trustStoreType=KeychainStore -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dhttps.protocols=TLSv1.2,TLSv1.3

mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause 