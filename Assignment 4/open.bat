@echo off
setlocal EnableDelayedExpansion

REM Check if PuTTY is in PATH
where putty >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo PuTTY is not installed or not in PATH.
    exit /b 1
)

REM Check if the command file exists
if not exist "commands.txt" (
    echo Command file commands.txt does not exist.
    exit /b 1
)

REM Set the base IP address
set "baseIP=129.69.210."
set "username=team6"
set "password=ohn2IDef"
set "ipEndings=60 61 62 64 65 66"
@REM set "ipEndings=61"

set "commandFile=commands.txt"

REM Iterate over the range of IP addresses
for %%i in (%ipEndings%) do (
    set "ipAddress=!baseIP!%%i"
    echo Processing IP address: !ipAddress!
    start "" putty -ssh !username!@!ipAddress! -pw !password! -m %commandFile%
)

REM End of the script
