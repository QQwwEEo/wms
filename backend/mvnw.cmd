@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn.cmd") ELSE (SET "MVN_CMD=%__MVNW_ARG0_NAME__%")
@SET MAVEN_WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
@SET MAVEN_WRAPPER_PROPERTIES=%~dp0.mvn\wrapper\maven-wrapper.properties

@IF NOT EXIST "%MAVEN_WRAPPER_JAR%" (
  @IF NOT "%MVNW_REPOURL%" == "" (
    SET MVNW_DOWNLOAD_FROM_WRAPPER=true
  )
  @FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_WRAPPER_PROPERTIES%") DO (
    IF "%%A"=="wrapperUrl" SET WRAPPER_URL=%%B
  )
  powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%MAVEN_WRAPPER_JAR%' -UseBasicParsing" 2>nul
)

@IF EXIST "%MAVEN_WRAPPER_JAR%" (
  @SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
  java -jar "%MAVEN_WRAPPER_JAR%" %*
) ELSE (
  @ECHO Maven Wrapper JAR not found. Please run: .\mvnw.cmd
)
