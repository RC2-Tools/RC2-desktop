# RC2 Desktop Modules

## Dependencies
- Maven 3.6 or higher
- Java 1.8 JDK

## Build
Run `mvn clean install`

##### Options
 - Use `-DskipTests` to skip tests.
 - Use `-Dapp-designer.directory=PATH` to use `PATH` instead of the `app-designer` submodule.

## Build HTML for Tables
In `app-designer`, run `grunt compile-config-templates`.
