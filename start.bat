rem set unicode code page
chcp 65001
"D:\Distros\java\openjdk-16.0.2\bin\java.exe" -Dlog4j.configurationFile=log4j2.xml -Dstorage=test -jar  main\target\melbone-uber-1.1.2.jar
@if errorlevel 1 pause



rem https://github.com/nipafx/module-system-woes/tree/main/testing
rem https://sormuras.github.io/blog/2018-09-11-testing-in-the-modular-world.html
rem https://github.com/forax/pro