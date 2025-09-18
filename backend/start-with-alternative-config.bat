@echo off
echo Starting Pallet Builder Backend with alternative MongoDB configuration...

set JAVA_OPTS=-Djdk.tls.client.protocols=TLSv1.2 -Dhttps.protocols=TLSv1.2 -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djava.security.egd=file:/dev/./urandom

mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%" -Dspring.config.location=classpath:application-alternative.properties

pause 