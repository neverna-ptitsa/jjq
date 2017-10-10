JJQ
===

JJQ is a JQ inspired annotation processor and code generator for Java. It allows you
to use a similar query language to JQ to create concise implementations of basic
projection and selection operations over Java collections or singletons.

The project is currently in a prototype state. Usage and other details will be
added when the project reaches a level of usability.

Maintaining
-----------

JJQ uses ktlint to enforce some basic style restrictions on its source files (which 
are in Kotlin).

If you get build errors from ktlint you can format the whole source tree through 
Maven:

```bash
mvn antrun:run@ktlint-format
```