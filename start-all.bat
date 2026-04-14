@echo off
chcp 65001 >nul
title CampusTutor Start

echo ============================================
echo   CampusTutor - Start All Services
echo ============================================
echo.

set ROOT_DIR=%~dp0

echo [1/3] Starting Backend (localhost:8080)...
start "CampusTutor-Backend" cmd /k "cd /d %ROOT_DIR%campus-backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

timeout /t 3 /nobreak >nul

echo [2/3] Starting Web Frontend (localhost:5173)...
start "CampusTutor-Web" cmd /k "cd /d %ROOT_DIR%campus-web && npm run dev"

echo [3/3] Starting Admin Panel (localhost:3001)...
start "CampusTutor-Admin" cmd /k "cd /d %ROOT_DIR%campus-web-admin && npm run dev"

echo.
echo ============================================
echo   All services started!
echo ============================================
echo.
echo   Backend API:    http://localhost:8080
echo   API Docs:       http://localhost:8080/doc.html
echo   Web Frontend:   http://localhost:5173
echo   Admin Panel:    http://localhost:3001
echo.
echo   To stop: close each window or run stop-all.bat
echo ============================================
pause