@echo off
chcp 65001 >nul
title CampusTutor 一键启动

echo ============================================
echo   CampusTutor 校园智教 - 一键启动全部服务
echo ============================================
echo.

:: 获取项目根目录
set ROOT_DIR=%~dp0

:: -------- 1. 启动后端 Spring Boot (端口 8080) --------
echo [1/4] 启动后端服务 (localhost:8080)...
start "CampusTutor-Backend (8080)" cmd /k "cd /d %ROOT_DIR%campus-backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

:: 等待3秒，给后端一点启动时间
timeout /t 3 /nobreak >nul

:: -------- 2. 启动教师端 (端口 5174) --------
echo [2/4] 启动教师端 (localhost:5174)...
start "CampusTutor-Teacher (5174)" cmd /k "cd /d %ROOT_DIR%campus-web-teacher && npm run dev"

:: -------- 3. 启动家长端 (端口 5175) --------
echo [3/4] 启动家长端 (localhost:5175)...
start "CampusTutor-Parents (5175)" cmd /k "cd /d %ROOT_DIR%campus-web-parents && npm run dev"

:: -------- 4. 启动管理后台 (端口 3001) --------
echo [4/4] 启动管理后台 (localhost:3001)...
start "CampusTutor-Admin (3001)" cmd /k "cd /d %ROOT_DIR%campus-web-admin && npm run dev"

echo.
echo ============================================
echo   全部服务已启动！
echo ============================================
echo.
echo   后端 API:    http://localhost:8080
echo   API 文档:    http://localhost:8080/doc.html
echo   教师端:      http://localhost:5174
echo   家长端:      http://localhost:5175
echo   管理后台:    http://localhost:3001
echo.
echo   关闭方式: 逐个关闭各命令行窗口
echo            或运行 stop-all.bat 一键停止
echo ============================================
pause
