rem set unicode code page
chcp 65001
rem set JAVA_HOME="D:\Distros\java\openjdk-16.0.2\bin"
set JAVA_HOME="D:\Distros\java\temurin-17.0.6\bin"
set MAT_HOME="D:\Distros\java\MemoryAnalyzer-1.14.0.20230315-win32.win32.x86_64\mat"
set PATH=%PATH%;%JAVA_HOME%
%JAVA_HOME%\java.exe --version

rem get pid
%JAVA_HOME%\jps.exe

rem get heap map
rem %JAVA_HOME%\jmap.exe -dump:live,format=b,file=/tmp/dump.hprof 3380

rem "D:\Distros\java\visualvm_215\visualvm_215\bin\visualvm.exe" --jdkhome "D:\Distros\java\openjdk-16.0.2" --userdir "D:\tmp"

%MAT_HOME%\MemoryAnalyzer.exe


rem @if errorlevel 1 pause

rem .*midorum.melbone.main.ui.IdentifyDialog.*