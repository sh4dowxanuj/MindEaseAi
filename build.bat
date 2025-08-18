@echo off
setlocal
set JAVA_HOME=%JAVA_HOME%
if not exist gradlew (
    echo gradlew not found. Please ensure you are in the project root and gradle wrapper is initialized.
    exit /b 1
)
REM Windows batch file to build Android app using Gradle wrapper
call gradlew.bat :app:assembleDebug
