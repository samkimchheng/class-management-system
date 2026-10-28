@echo off
chcp 65001 >nul
title ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀន ២០២៧ (Local Server)
echo ===================================================================
echo     ប្រព័ន្ធគ្រប់គ្រងថ្នាក់រៀនអនឡាញ (Class Management System 2027)
echo     កំពុងដំណើរការ Server លើម៉ាស៊ីនកុំព្យូទ័រផ្ទាល់ (Local)...
echo     អាសយដ្ឋាន៖ http://localhost:8080
echo ===================================================================
timeout /t 3 >nul
start "" http://localhost:8080
call .\mvnw.cmd spring-boot:run
pause
