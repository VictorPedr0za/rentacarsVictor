@echo off
title AutoRent - TODO LOCAL (DynamoDB Local)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\iniciar-local.ps1"
echo.
echo Se detuvo. Si DynamoDB Local sigue en otra ventana, cierrala tambien.
pause
