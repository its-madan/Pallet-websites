@echo off
echo Starting Pallet Builder Backend with final MongoDB Atlas configuration...

REM Set comprehensive Java security properties for Windows + MongoDB Atlas
set JAVA_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djava.security.egd=file:/dev/./urandom -Djavax.net.debug=ssl:handshake

REM Alternative: Use system default trust store with more debugging
REM set JAVA_OPTS=-Djavax.net.ssl.trustStoreType=KeychainStore -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dhttps.protocols=TLSv1.2,TLSv1.3 -Djavax.net.debug=ssl:handshake

echo Using JVM options: %JAVA_OPTS%
echo.
echo Starting application...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause 