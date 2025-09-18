Write-Host "Starting Pallet Builder Backend with MongoDB Atlas configuration..." -ForegroundColor Green

$env:JAVA_OPTS = "-Djavax.net.ssl.trustStoreType=KeychainStore -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dcom.sun.net.ssl.checkRevocation=false -Dcom.sun.net.ssl.enableSNIExtension=false -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dhttps.protocols=TLSv1.2,TLSv1.3"

mvn spring-boot:run -Dspring-boot.run.jvmArguments="$env:JAVA_OPTS"

Read-Host "Press Enter to continue..." 