# eclipse-xterm
Xterm based terminal Eclipse integration. Contributes **Xterm** view to Eclipse and actions on projects to open Xterm view for them

**Requires**: the mavev project from https://github.com/BoykoAlex/xterm

1. Clone https://github.com/BoykoAlex/xterm, cd into **xterm** folder and run `./mvnw clean install`
2. Run maven update on the eclipse xterm plugin project thus `/lib/xterm.jar` is created.

**Note**: current state of the project may require manually copying `xterm.jar` into the lib folder (xterm built JAR)
