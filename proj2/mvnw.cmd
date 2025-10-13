@ECHO OFF
SETLOCAL

set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPS=.mvn\wrapper\maven-wrapper.properties

for /f "tokens=2 delims==" %%A in ('findstr /R "^wrapperUrl=" "%WRAPPER_PROPS%"') do set WRAPPER_URL=%%A

IF NOT EXIST "%WRAPPER_JAR%" (
  mkdir .mvn\wrapper 2> NUL
  powershell -Command "Invoke-WebRequest -UseBasicParsing %WRAPPER_URL% -OutFile '%WRAPPER_JAR%'"
)

set JAVA_EXE=java
IF DEFINED JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe

REM Find project base directory
set MAVEN_PROJECTBASEDIR=%CD%
if exist ".mvn" goto baseDirFound
set DIR=%CD%
:findBaseDir
if exist "%DIR%\.mvn" set MAVEN_PROJECTBASEDIR=%DIR% & goto baseDirFound
cd ..
if "%DIR%"=="%CD%" goto baseDirFound
set DIR=%CD%
goto findBaseDir
:baseDirFound

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*

ENDLOCAL
