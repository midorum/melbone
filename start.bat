rem set unicode code page
chcp 65001
"D:\Distros\java\openjdk-16.0.2\bin\java.exe" -Dlog4j.configurationFile=log4j2.xml -Dstorage=test -jar  main\target\melbone-uber-1.1.2.jar
@if errorlevel 1 pause