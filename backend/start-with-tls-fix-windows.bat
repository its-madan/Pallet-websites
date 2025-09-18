@echo off
echo Starting Pallet Builder Backend with TLS/SSL fixes for MongoDB Atlas (Windows)...

set JAVA_OPTS=-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dhttps.protocols=TLSv1.2,TLSv1.3 -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djava.security.egd=file:/dev/./urandom

mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause 