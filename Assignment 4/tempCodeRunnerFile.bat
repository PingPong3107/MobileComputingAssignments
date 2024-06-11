@echo off
setlocal EnableDelayedExpansion

REM Set the base IP address
set "baseIP=129.69.210."
set "username=team6"
set "password=ohn2IDef"
@REM set "ipEndings=60 61 62 64 65 66"
set "ipEndings=61"

set "commandFile=commands.txt"

REM Iterate over the range of IP addresses
for %%i in (%ipEndings%) do (
    set "ipAddress=!baseIP!%%i"
    echo Processing IP address: !ipAddress!
    start "" putty -ssh !username!@!ipAddress! -pw !password! -m %commandFile%
)



REM End of the script
