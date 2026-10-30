@echo off
rem Starts Aurora against PostgreSQL.
rem Set AURORA_DB_PASSWORD once as a system environment variable (run as administrator):
rem   setx /M AURORA_DB_PASSWORD "<the aurora database password>"
rem Never put the password in this file.
cd /d C:\Apps

if "%AURORA_DB_PASSWORD%"=="" (
    echo AURORA_DB_PASSWORD is not set. See migration\postgres\README.md.
    pause
    exit /b 1
)
set AURORA_DB_URL=jdbc:postgresql://localhost:5432/aurora
set AURORA_DB_USERNAME=aurora

java -Duser.timezone=Asia/Manila -jar aurora.war --spring.profiles.active=prod
