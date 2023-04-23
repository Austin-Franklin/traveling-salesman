@echo off

set no_java=false

REM check if java exists
where java >nul 2>nul
if errorlevel 1 (
    echo Java JRE not installed.
    set no_java=true
)

where javac >nul 2>nul
if errorlevel 1 (
    echo Java SDK not installed.
    set no_java=true
)

if %no_java%==true (
    exit /b 1
)

REM check if jar file exists
if not exist "target\traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar" (
    REM try to install
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo Maven not installed.
        exit /b 1
    )

    mvn install
)

REM check if enough memory is available
set /a total_ram=0
for /f "tokens=2" %%m in ('systeminfo ^| findstr /C:"Total Physical Memory:"') do set /a total_ram=%%m / 1024

if %total_ram% lss 2048 (
    echo Your system only has %total_ram% MB of RAM, 2048 MB are required.
    exit /b 1
)

:set_input
set /p user_input="Run 'normal' or 'demo': "

if /i "%user_input%" equ "demo" (
    java -jar -Xms256m -Xmx2048m target\traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar < demo.txt
) else if /i "%user_input%" equ "normal" (
    java -jar -Xms256m -Xmx2048m target\traveling-salesman-1.0-SNAPSHOT-jar-with-dependencies.jar
) else (
    echo Invalid input, enter only 'normal' or 'demo'.
    goto :set_input
)
