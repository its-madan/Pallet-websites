# PowerShell script to manually download missing Maven dependencies
# Run this script from the backend directory

$mavenRepo = "$env:USERPROFILE\.m2\repository"

# Create the base repository directory if it doesn't exist
if (!(Test-Path $mavenRepo)) {
    New-Item -ItemType Directory -Path $mavenRepo -Force
}

# Function to download and place JAR file
function Download-JarFile {
    param(
        [string]$GroupId,
        [string]$ArtifactId,
        [string]$Version,
        [string]$DownloadUrl
    )
    
    # Create the directory structure
    $dirPath = "$mavenRepo\$($GroupId.Replace('.', '\'))\$ArtifactId\$Version"
    if (!(Test-Path $dirPath)) {
        New-Item -ItemType Directory -Path $dirPath -Force
    }
    
    $jarFile = "$dirPath\$ArtifactId-$Version.jar"
    $pomFile = "$dirPath\$ArtifactId-$Version.pom"
    
    Write-Host "Downloading $ArtifactId-$Version.jar..."
    
    try {
        # Download JAR file
        Invoke-WebRequest -Uri $DownloadUrl -OutFile $jarFile -UseBasicParsing
        Write-Host "SUCCESS: Downloaded $jarFile"
        
        # Try to download the real POM file
        $pomUrl = $DownloadUrl.Replace(".jar", ".pom")
        try {
            Invoke-WebRequest -Uri $pomUrl -OutFile $pomFile -UseBasicParsing
            Write-Host "SUCCESS: Downloaded real $pomFile"
        } catch {
            Write-Host "INFO: Could not download POM from $pomUrl. Creating a basic one."
            # Create a basic POM file if download fails
            $pomContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>$GroupId</groupId>
    <artifactId>$ArtifactId</artifactId>
    <version>$Version</version>
    <packaging>jar</packaging>
</project>
"@
            Set-Content -Path $pomFile -Value $pomContent
            Write-Host "SUCCESS: Created basic $pomFile"
        }
        
    } catch {
        Write-Host "FAILED: Failed to download $ArtifactId-$Version.jar: $($_.Exception.Message)"
    }
}

# List of problematic dependencies to download (updated for Spring Boot 3.2.0)
$dependencies = @(
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-beans"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-beans/6.1.1/spring-beans-6.1.1.jar"
    },
    @{
        GroupId = "org.springframework.data"
        ArtifactId = "spring-data-commons"
        Version = "3.2.0"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/data/spring-data-commons/3.2.0/spring-data-commons-3.2.0.jar"
    },
    @{
        GroupId = "org.springframework.security"
        ArtifactId = "spring-security-config"
        Version = "6.2.0"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/security/spring-security-config/6.2.0/spring-security-config-6.2.0.jar"
    },
    @{
        GroupId = "org.apache.tomcat.embed"
        ArtifactId = "tomcat-embed-core"
        Version = "10.1.16"
        DownloadUrl = "https://repo1.maven.org/maven2/org/apache/tomcat/embed/tomcat-embed-core/10.1.16/tomcat-embed-core-10.1.16.jar"
    },
    @{
        GroupId = "com.fasterxml.jackson.core"
        ArtifactId = "jackson-databind"
        Version = "2.15.2"
        DownloadUrl = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
    },
    @{
        GroupId = "net.bytebuddy"
        ArtifactId = "byte-buddy"
        Version = "1.14.10"
        DownloadUrl = "https://repo1.maven.org/maven2/net/bytebuddy/byte-buddy/1.14.10/byte-buddy-1.14.10.jar"
    },
    @{
        GroupId = "org.codehaus.plexus"
        ArtifactId = "plexus-utils"
        Version = "3.4.2"
        DownloadUrl = "https://repo1.maven.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.jar"
    },
    @{
        GroupId = "org.mongodb"
        ArtifactId = "mongodb-driver-core"
        Version = "4.11.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.11.1/mongodb-driver-core-4.11.1.jar"
    },
    @{
        GroupId = "org.springframework.data"
        ArtifactId = "spring-data-mongodb"
        Version = "4.2.0"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/data/spring-data-mongodb/4.2.0/spring-data-mongodb-4.2.0.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-context"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-context/6.1.1/spring-context-6.1.1.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-web"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-web/6.1.1/spring-web-6.1.1.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-webmvc"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-webmvc/6.1.1/spring-webmvc-6.1.1.jar"
    },
    @{
        GroupId = "org.mockito"
        ArtifactId = "mockito-core"
        Version = "5.7.0"
        DownloadUrl = "https://repo1.maven.org/maven2/org/mockito/mockito-core/5.7.0/mockito-core-5.7.0.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-core"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-core/6.1.1/spring-core-6.1.1.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-aop"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-aop/6.1.1/spring-aop-6.1.1.jar"
    },
    @{
        GroupId = "org.springframework"
        ArtifactId = "spring-expression"
        Version = "6.1.1"
        DownloadUrl = "https://repo1.maven.org/maven2/org/springframework/spring-expression/6.1.1/spring-expression-6.1.1.jar"
    },
    @{
        GroupId = "com.github.luben"
        ArtifactId = "zstd-jni"
        Version = "1.5.5-2"
        DownloadUrl = "https://repo1.maven.org/maven2/com/github/luben/zstd-jni/1.5.5-2/zstd-jni-1.5.5-2.jar"
    }
)

Write-Host "Starting manual download of missing Maven dependencies..."
Write-Host "Maven repository location: $mavenRepo"
Write-Host ""

foreach ($dep in $dependencies) {
    Download-JarFile -GroupId $dep.GroupId -ArtifactId $dep.ArtifactId -Version $dep.Version -DownloadUrl $dep.DownloadUrl
    Write-Host ""
}

Write-Host "Download completed!"
Write-Host "Now try running: mvn clean install" 