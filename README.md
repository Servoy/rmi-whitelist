# rmi-whitelist

## Description

This repository provides a class that implements the service provider interface for RMIClassLoader.
It is designed as a defence against vulnerability in RMI as described in
[What Do WebLogic, WebSphere, JBoss, Jenkins, OpenNMS, and Your Application Have in Common? This Vulnerability] (http://foxglovesecurity.com/2015/11/06/what-do-weblogic-websphere-jboss-jenkins-opennms-and-your-application-have-in-common-this-vulnerability/#opennms)

When this class is installed as RMIClassLoaderSpi it will check all classes to be loaded against a whitelist and a blacklist.
A default whitelist is included and can be configured via a system property.

When the class is packaged as the jar from the maven build and placed in the system class path, RMI will automatically pick-up the RMIClassLoaderSpi and the whitelisting is active.

## Building

Check out the git repository and build the jar with maven:

	maven clean package

Pick up the jar from the target directory.

## Configuring

The whitelist is configured via the system property `rmi.whitelist.config`.
It contains whitelist and blacklist entries separated by a colon (`:`).
Blacklist-entries are tagged with a minus (`-`), whitelist entries are not tagged.
Classes to be loaded are validated by checking wether the full class name starts with the configured entry.

For example, `rmi.whitelist.config=com.example.foo.:-com.example.foo.bar.` means allow all classes from com.example.foo and its subpackages, except classes from com.example.foo.bar and its subpackages.

By default, the blacklist is empty and the whitelist contains `"["` (for arrays), `"java."` and `"sun."` for standard classes and `"javax.management."` for jmx.

## Installing

Include the `target/rmi-whitelist-<version>.jar` jar file in your system classpath.

## Verifying

A proof-of-concept tool for [ysoserial] (https://github.com/frohoff/ysoserial) is available to test your server for vulnerability.
The tool should no longer get access to your server and in the logs you should see something like the following:

    2015-11-14 14:39:06,237 WARN [RMI TCP Connection(2)-127.0.0.1] com.servoy.rmi.whitelist.WhitelistingRMIClassLoaderSpi - Class not whitelisted for RMI: org.apache.commons.collections.functors.InvokerTransformer [ ]
    


