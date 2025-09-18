@echo off
echo Starting Pallet Builder Backend with Java 17 TLS/SSL fixes for MongoDB Atlas...

set JAVA_OPTS=-Djdk.tls.client.protocols=TLSv1.2 -Dhttps.protocols=TLSv1.2 -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djava.security.egd=file:/dev/./urandom -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Djavax.net.ssl.keyStoreType=JKS -Djavax.net.ssl.keyStore= -Djavax.net.ssl.keyStorePassword= -Djavax.net.debug=ssl:handshake

mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

pause 