@echo off
setlocal enabledelayedexpansion

md Work
md Work\_tmp\XClipper.App\Data\helpers
md Work\_tmp\XClipper.Protect

SET parent=%~dp0
FOR %%a IN ("%parent:~0,-1%") DO SET grandparent=%%~dpa


copy "%grandparent%XClipper.App\Data\helpers\LicenseHelper.cs" "%parent%Work\_tmp\XClipper.App\Data\helpers\LicenseHelper.cs"
xcopy "%grandparent%XClipper.Protect" "%parent%Work\_tmp\XClipper.Protect" /E
rd /s /q "%parent%Work\_tmp\XClipper.Protect\bin" 
rd /s /q "%parent%Work\_tmp\XClipper.Protect\obj"
del "%parent%Work\_tmp\XClipper.Protect\*.user"
tar -C Work\_tmp -cvzf Work\protect.tar.gz *
rd /s /q Work\_tmp

if exist Work\encode.txt (
	del /s /q Work\encode.txt
)

certutil -encode "Work\protect.tar.gz" Work\encode-p.txt && findstr /v /c:- Work\encode-p.txt > Work\encode.txt
del /s /q Work\encode-p.txt

echo.
echo.
echo A "Work\protect.tar.gz" file is created if you want to back it up on your cloud for
echo safety reasons. Another file with encode.txt is created for you to upload as secret key for CI.
echo.
pause
