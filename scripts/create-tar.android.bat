@echo off
setlocal enabledelayedexpansion

md Work
md Work\_tmp\XClipper.Android\modules\core-private

SET parent=%~dp0
FOR %%a IN ("%parent:~0,-1%") DO SET grandparent=%%~dpa

xcopy "%grandparent%XClipper.Android\modules\core-private" "%parent%Work\_tmp\XClipper.Android\modules\core-private" /E
rd /s /q "%parent%Work\_tmp\XClipper.Android\modules\core-private\build"
rd /s /q "%parent%Work\_tmp\XClipper.Android\modules\core-private\src\androidTest"
rd /s /q "%parent%Work\_tmp\XClipper.Android\modules\core-private\src\test"
del "%parent%Work\_tmp\XClipper.Android\modules\core-private\consumer-rules.pro"
del "%parent%Work\_tmp\XClipper.Android\modules\core-private\license.iml"
tar -C Work\_tmp -cvzf Work\library_utils.tar.gz *
rd /s /q Work\_tmp

if exist Work\encode-android.txt (
	del /s /q Work\encode-android.txt
)

certutil -encode "Work\library_utils.tar.gz" Work\encode-p.txt && findstr /v /c:- Work\encode-p.txt > Work\encode-android.txt
del /s /q Work\encode-p.txt

echo.
echo.
echo A "Work\library_utils.tar.gz" file is created if you want to back it up on your cloud for
echo safety reasons. Another file with "encode-android.txt" is created for you to upload as secret key for CI.
echo.
pause
