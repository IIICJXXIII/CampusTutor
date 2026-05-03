@echo off
chcp 65001 >nul
mysql -u root -p200512 --default-character-set=utf8mb4 campus_tutor_db < data.sql
pause
