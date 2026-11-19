@echo off
setlocal
title JobTH - Career Platform
rem Run from the folder this file is in, wherever it was double-clicked from
cd /d "%~dp0"

rem 1. Check that a Java JDK (17 or newer) is installed
where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java JDK not found.
    echo Install JDK 17 or newer from https://adoptium.net then run this file again.
    echo.
    pause
    exit /b 1
)

rem 2. Compile all source files into the "out" folder
echo Compiling...
if exist out rmdir /s /q out
mkdir out
set "SOURCES=%TEMP%\jobth-sources.txt"
(
    echo Main.java
    for %%d in (data gui model service web) do (
        for %%f in (%%d\*.java) do echo %%d/%%~nxf
    )
) > "%SOURCES%"

javac -encoding UTF-8 -d out @"%SOURCES%"
if errorlevel 1 (
    del "%SOURCES%" >nul 2>nul
    echo.
    echo [ERROR] Compile failed. Make sure you have JDK 17 or newer: run "java -version" to check.
    echo.
    pause
    exit /b 1
)
del "%SOURCES%" >nul 2>nul

rem 3. Start the Swing app
echo Starting JobTH...
java -cp out jobth.Main %*
if errorlevel 1 (
    echo.
    echo [ERROR] The app stopped with an error. See the message above.
    pause
)
endlocal
