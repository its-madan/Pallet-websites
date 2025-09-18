@echo off
echo Testing alternative MongoDB Atlas connection string format...

REM Set JVM options for TLS compatibility
set JAVA_OPTS=-Djdk.tls.client.protocols=TLSv1.2 -Dhttps.protocols=TLSv1.2 -Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djava.security.egd=file:/dev/./urandom

echo.
echo Testing with alternative connection string format...
echo This will temporarily modify your application.properties
echo.

REM Backup current application.properties
copy src\main\resources\application.properties src\main\resources\application.properties.backup

REM Create temporary application.properties with alternative connection string
echo # MongoDB Configuration - Alternative Format > src\main\resources\application.properties
echo spring.data.mongodb.uri=mongodb://saravanatimbersweb:%%24%%40r%%40v%%40n%%40T%%21mber%%241@ac-ukj5qcl-shard-00-00.vxzvpna.mongodb.net:27017,ac-ukj5qcl-shard-00-01.vxzvpna.mongodb.net:27017,ac-ukj5qcl-shard-00-02.vxzvpna.mongodb.net:27017/pallet-builder?ssl=true^&replicaSet=atlas-hpb9p6-shard-0^&authSource=admin^&retryWrites=true^&w=majority >> src\main\resources\application.properties
echo spring.data.mongodb.database=pallet-builder >> src\main\resources\application.properties
echo. >> src\main\resources\application.properties
echo # Allow bean definition overriding >> src\main\resources\application.properties
echo spring.main.allow-bean-definition-overriding=true >> src\main\resources\application.properties
echo. >> src\main\resources\application.properties
echo # MongoDB Connection Settings >> src\main\resources\application.properties
echo spring.data.mongodb.auto-index-creation=true >> src\main\resources\application.properties
echo spring.data.mongodb.connection-pool.max-size=100 >> src\main\resources\application.properties
echo spring.data.mongodb.connection-pool.min-size=5 >> src\main\resources\application.properties
echo spring.data.mongodb.connection-pool.max-wait-time=120000 >> src\main\resources\application.properties
echo. >> src\main\resources\application.properties
echo # JWT Secret >> src\main\resources\application.properties
echo jwt.secret=your-super-strong-secretyour-super-strong-secretyour-super-strong-secret >> src\main\resources\application.properties
echo jwt.expirationMs=86400000 >> src\main\resources\application.properties
echo. >> src\main\resources\application.properties
echo # Email SMTP configuration for Gmail >> src\main\resources\application.properties
echo spring.mail.host=smtp.gmail.com >> src\main\resources\application.properties
echo spring.mail.port=587 >> src\main\resources\application.properties
echo spring.mail.username=your-email@gmail.com >> src\main\resources\application.properties
echo spring.mail.password=your-app-password >> src\main\resources\application.properties
echo spring.mail.properties.mail.smtp.auth=true >> src\main\resources\application.properties
echo spring.mail.properties.mail.smtp.starttls.enable=true >> src\main\resources\application.properties

echo Starting application with alternative connection string...
mvn spring-boot:run -Dspring-boot.run.jvmArguments="%JAVA_OPTS%"

echo.
echo Test completed. Restoring original application.properties...
copy src\main\resources\application.properties.backup src\main\resources\application.properties
del src\main\resources\application.properties.backup

echo.
echo Original configuration restored.
pause 