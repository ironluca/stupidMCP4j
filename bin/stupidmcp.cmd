@echo off
setlocal

rem Fail fast if required variables are missing
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set. Please set JAVA_HOME=C:\path\to\jdk-17 >&2
    exit /b 1
)

if "%STUPIDMCP_HOME%"=="" (
    echo ERROR: STUPIDMCP_HOME is not set. Please set STUPIDMCP_HOME=C:\path\to\stupidMCP4j >&2
    exit /b 1
)

rem Verify the java binary exists
set "JAVACMD=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVACMD%" (
    echo ERROR: Cannot find executable java at %JAVACMD% >&2
    exit /b 1
)

rem Build classpath using STUPIDMCP_HOME (Windows uses semicolons)
set "CP=%STUPIDMCP_HOME%\lib\*;%STUPIDMCP_HOME%\plugin"

echo Starting stupidMCP4j (Home: %STUPIDMCP_HOME%)...
"%JAVACMD%" -cp "%CP%" io.github.ironluca.stupidmcp4j.StupidMCP %*

endlocal