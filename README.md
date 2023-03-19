# RC2 Desktop Modules

## About
- This is an open source project developed for humanitarian responses
- The user manual can be found at https://reliefweb.int/report/world/rc2-relief-user-manual-all-users

## Dependencies
- Maven 3.6 or higher
	- Can be downloaded at maven.apache.org/download.cgi
	- Ensure that your `PATH` is set to Maven's `bin` folder.
- Java 1.8 JDK
	- Older versions of Java can be downloaded at oracle.com/java/technologies/downloads/archive/ (requires an Oracle account)

## Clone the repo
```bash
cd <directory you want to download into>
git clone --recurse-submodules https://github.com/RC2-Tools/RC2-desktop.git

#After creating a fork into your own repository
git clone --recurse-submodules https://github.com/<YourUserName>/RC2-desktop.git
```
## Build
Run `mvn clean install`
To make the installer, run `mvn clean deploy`

##### Options
 - Use `-DskipTests` to skip tests.
 - Use `-Dapp-designer.directory=PATH` to use `PATH` instead of the `app-designer` submodule.

## Build HTML for Tables
In `app-designer`, run `grunt compile-config-templates`.