@echo off
setlocal

rem Fail fast if required variables are missing
if "%JAVA_HOME%"=="" (
    echo ERROR: JAVA_HOME is not set. Please set JAVA_HOME=C:\path\to\jdk-17 >&2
    exit /b 1
)

if "%MCP_HOME%"=="" (
    echo ERROR: MCP_HOME is not set. Please set MCP_HOME=C:\path\to\stupidMCP4j >&2
    exit /b 1
)

rem Verify the java binary exists
set "JAVACMD=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVACMD%" (
    echo ERROR: Cannot find executable java at %JAVACMD% >&2
    exit /b 1
)

rem Build classpath using MCP_HOME (Windows uses semicolons)
set "CP=%MCP_HOME%\lib\*;%MCP_HOME%\tools\*"

echo Starting stupidMCP4j (Home: %MCP_HOME%)...
"%JAVACMD%" -cp "%CP%" io.github.ironluca.stupidmcp4j.server.MCPServer %*

set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%