@echo off
chcp 65001 >nul
title CampusTutor Stop

echo ============================================
echo   CampusTutor - Stop All Services
echo ============================================
echo.

echo [1/3] Stopping Backend (port 8080)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo   Killing PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo [2/3] Stopping Web Frontend (port 5173)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173 " ^| findstr "LISTENING"') do (
    echo   Killing PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo [3/3] Stopping Admin Panel (port 3001)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3001 " ^| findstr "LISTENING"') do (
    echo   Killing PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo ============================================
echo   All services stopped!
echo ============================================
pause