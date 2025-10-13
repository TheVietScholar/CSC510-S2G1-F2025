@ECHO OFF
SETLOCAL
set MVN_CMD=mvn
set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPS=.mvn\wrapper\maven-wrapper.properties
IF NOT EXIST %WRAPPER_JAR% (
  mkdir .mvn\wrapper 2> NUL
  powershell -Command "Invoke-WebRequest -UseBasicParsing https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar -OutFile %WRAPPER_JAR%"
)
"%MVN_CMD%" %*
ENDLOCAL
