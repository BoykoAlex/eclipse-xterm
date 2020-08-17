# eclipse-xterm
Xterm based terminal Eclipse integration. Contributes **Xterm** view to Eclipse and actions on projects to open Xterm view for them

**Requires**: the mavev project from https://github.com/BoykoAlex/xterm

1. Clone https://github.com/BoykoAlex/xterm, cd into **xterm** folder and run `./mvnw clean install`
2. Run maven update on the eclipse xterm plugin project thus `/lib/xterm.jar` is created.

**Note**: current state of the project requires **xterm** Spring Boot app launched separately on port **8080**, i.e. run `./mvnw spring-boot:run` from the **xterm** project folder
