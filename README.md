# RC2 Desktop Modules

## About
- This is an open source project developed for humanitarian responses
- The user manual can be found at https://reliefweb.int/report/world/rc2-relief-user-manual-all-users

## Dependencies
- Maven 3.6 or higher
	- Can be downloaded at maven.apache.org/download.cgi
	- Ensure that your `PATH` is set to Maven's `bin` folder.
 		- Navigate to the enviromental variables for your system.
 		- `MAVEN_HOME` should be added and set to the directory.
   			- E.g. `C:\Program Files\Maven\apache-maven-3.8.4` (make adjustments for version and location)
     	- `M2` should be added and set to the `bin` folder.
       		- E.g. `%MAVEN_HOME%\bin` (Windows), `$MAVEN_HOME/bin` (Linux, Mac)
- Java 1.8 JDK
	- Older versions of Java can be downloaded at oracle.com/java/technologies/downloads/archive/ (requires an Oracle account)
 	- `JAVA_HOME` variable should be set to the pathing for `jdk-1.8` in enviromental variables
 		- E.g. `C:\Program Files\Java\jdk-1.8` (make adjustments for location)
   	- Once install is complete, it may also be neccessary to ensure that the editor being used is also set utilize 1.8

## Clone the repo
```bash
cd <directory you want to download into>
git clone --recurse-submodules https://github.com/RC2-Tools/RC2-desktop.git

#After creating a fork into your own repository
git clone --recurse-submodules https://github.com/<YourUserName>/RC2-desktop.git
```
- After logging into or making an account on GitHub, use button on upper right to make a fork before doing second step
- `git` can be downloaded from https://git-scm.com/downloads

## Build
Run `mvn clean install`
To make the installer, run `mvn clean deploy`

##### Options
 - Use `-DskipTests` to skip tests.
 - Use `-Dapp-designer.directory=PATH` to use `PATH` instead of the `app-designer` submodule.

## Build HTML for Tables
In `app-designer`, run `grunt compile-config-templates`.
