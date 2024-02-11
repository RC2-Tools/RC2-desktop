# RC2 Desktop Modules

## About
- This is an open source project developed for humanitarian responses
- The user manual can be found at https://reliefweb.int/report/world/rc2-relief-user-manual-all-users

## Dependencies
- Maven 3.6 or higher
	- Can be downloaded at https://maven.apache.org/download.cgi
	- Ensure that your `PATH` is set to Maven's `bin` folder.
 		- Navigate to the enviromental variables for your system.
 		- `MAVEN_HOME` should be added and set to the directory.
   			- E.g. `C:\Program Files\Maven\apache-maven-3.8.4` (make adjustments for version and location)
     	- `M2` should be added and set to the `bin` folder.
       		- I.e. `%MAVEN_HOME%\bin` (Windows), `$MAVEN_HOME/bin` (Linux, Mac)
- Java 1.8 JDK
	- Older versions of Java can be downloaded at oracle.com/java/technologies/downloads/archive/ (requires an Oracle account)
 	- `JAVA_HOME` variable should be set to the pathing for `jdk-1.8` in enviromental variables
 		- E.g. `C:\Program Files\Java\jdk-1.8` (make adjustments for location)
   	- Once install is complete, it may also be neccessary to ensure that the editor being used is also set utilize 1.8

### Setting Enviromental Variables
#### Windows
- `Control Panel` > `System and Security` > `System` > `Advanced System Settings` > `Enviroment Variables` > (under System Variables) `New`

  **<ins>OR</ins>** search `system environment variables` in `Search Bar` > select `Edit the system enviroment variables` > `Enviroment Variables` > (under System Variables) `New`
	- Set `Variable name` to [respective name]
	- Set `Variable value` to [respective path]
#### Linux/Mac
- Open `Terminal`
```bash
export M2_HOME=<respective path>
export M2=$M2_HOME/bin
export JAVA_HOME=<respective path>
```

## Clone the repo
```bash
cd <directory you want to download into>
git clone --recurse-submodules https://github.com/RC2-Tools/RC2-desktop.git

#After creating a fork into your own repository
git clone --recurse-submodules https://github.com/<YourUserName>/RC2-desktop.git
```
- `git` can be downloaded from https://git-scm.com/downloads
- After logging into or making an account on GitHub, use button on upper right to make a `Fork` before doing second step

## Build
Run `mvn clean install`
To make the installer, run `mvn clean deploy`

##### Options
 - Use `-DskipTests` to skip tests.
 - Use `-Dapp-designer.directory=PATH` to use `PATH` instead of the `app-designer` submodule.

## Build HTML for Tables
In `app-designer`, run `grunt compile-config-templates`.
- `grunt` can be installed from https://gruntjs.com/getting-started
	- Requires `npm` which is an extension of `nvm`
		- For Windows, get `nvm`/`npm` from https://github.com/coreybutler/nvm-windows?tab=readme-ov-file
		- For Linux/Mac get `npm`/`npm` https://github.com/nvm-sh/nvm
		- Once `nvm` is installed, it may be neccessary to turn `nvm on` in the command line before `npm` is recognized
