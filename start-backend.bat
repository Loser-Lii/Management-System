@echo off
cd /d "%~dp0backend"
echo Starting backend server...
mvn spring-boot:run
