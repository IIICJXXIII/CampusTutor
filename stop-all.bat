@echo off
chcp 65001 >nul
title CampusTutor 一键停止

echo ============================================
echo   CampusTutor 校园智教 - 一键停止全部服务
echo ============================================
echo.

:: 停止后端 Java 进程（Spring Boot 默认端口 8080）
echo [1/4] 停止后端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    echo   正在终止 PID: %%a (端口 8080)
    taskkill /F /PID %%a >nul 2>&1
)

:: 停止教师端 (端口 5174)
echo [2/4] 停止教师端...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5174 " ^| findstr "LISTENING"') do (
    echo   正在终止 PID: %%a (端口 5174)
    taskkill /F /PID %%a >nul 2>&1
)

:: 停止家长端 (端口 5175)
echo [3/4] 停止家长端...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5175 " ^| findstr "LISTENING"') do (
    echo   正在终止 PID: %%a (端口 5175)
    taskkill /F /PID %%a >nul 2>&1
)

:: 停止管理后台 (端口 3001)
echo [4/4] 停止管理后台...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3001 " ^| findstr "LISTENING"') do (
    echo   正在终止 PID: %%a (端口 3001)
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo ============================================
echo   全部服务已停止！
echo ============================================
pause
