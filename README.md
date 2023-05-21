
# Tracked issues

#### Log4j shows the following warning when running with Java 11. No such warnings were shown when running with Java 8.
```
WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.
```
See: [Log4j 2 Performance issue with Java 11](https://issues.apache.org/jira/browse/LOG4J2-2537)

#### Log4j shows the following error when running in application built with maven-shade-plugin
```
ERROR StatusLogger Unrecognized format specifier [d]
ERROR StatusLogger Unrecognized conversion specifier [d] starting at position 16 in conversion pattern.
ERROR StatusLogger Unrecognized format specifier [thread]
ERROR StatusLogger Unrecognized conversion specifier [thread] starting at position 25 in conversion pattern.
ERROR StatusLogger Unrecognized format specifier [level]
ERROR StatusLogger Unrecognized conversion specifier [level] starting at position 35 in conversion pattern.
ERROR StatusLogger Unrecognized format specifier [logger]
...
```
See: 
- [LOG4J2-673](https://issues.apache.org/jira/browse/LOG4J2-673) 
- [LOG4J2-2621](https://issues.apache.org/jira/browse/LOG4J2-2621)

> The changes for LOG4J2-2621 will hopefully fix this problem. The plugin data will be captured in a Java file that should have a different package name in every jar so long as proper package naming conventions are followed. The Java class will be loaded with the ServiceLoader so as long as the shading process supports the java.util.ServiceLoader files in META-INF/services this problem should be resolved.
> 
> The changes for LOG4J2-2621 are only available on the master branch which will become Log4j 2 3.0.0.

 
