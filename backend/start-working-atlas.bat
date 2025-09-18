@echo off
echo Starting Pallet Builder Backend with WORKING MongoDB Atlas configuration...

REM Force TLS 1.2 only - this is the key fix for MongoDB Atlas
set JAVA_OPTS=-Djdk.tls.client.protocols=TLSv1.2 -Dhttps.protocols=TLSv1.2 -Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djava.security.egd=file:/dev/./urandom

echo Using JVM options: %JAVA_OPTS%
echo.
echo Starting application...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause 