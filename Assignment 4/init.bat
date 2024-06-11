@echo off
setlocal EnableDelayedExpansion

REM Check if PuTTY is in PATH
where pscp >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo PuTTY is not installed or not in PATH.
    exit /b 1
)

REM Check if at least one filename is provided as an argument
if "%~1"=="" (
    echo Usage: %~nx0 filename1 [filename2 ... filenameN]
    exit /b 1
)

REM Check if each provided filename exists
:checkFiles
for %%a in (%*) do (
    if not exist "%%~a" (
        echo File %%~a does not exist.
        exit /b 1
    )
)

REM Set the base IP address
set "baseIP=129.69.210."
set "username=team6"
set "password=ohn2IDef"
set "ipEndings=60 61 62 64 65 66"

REM Iterate over the range of IP addresses
for %%i in (%ipEndings%) do (
    set "ipAddress=!baseIP!%%i"
    echo Processing IP address: !ipAddress!

    REM Iterate over all provided filenames
    for %%f in (%*) do (
        set "filename=%%~f"
        echo Processing file: !filename!
        
        REM Transfer the file
        pscp -pw !password! "!filename!" !username!@!ipAddress!:/home/team6/!filename!
    )
)

REM End of the script
