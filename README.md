JJQ
===

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b2a3a353d9e64d56a30ebb3a9b2e8a36)](https://www.codacy.com/app/neverna-ptitsa/jjq?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=neverna-ptitsa/jjq&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/neverna-ptitsa/jjq.svg?branch=master)](https://travis-ci.org/neverna-ptitsa/jjq)
[![codecov](https://codecov.io/gh/neverna-ptitsa/jjq/branch/master/graph/badge.svg)](https://codecov.io/gh/neverna-ptitsa/jjq)

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

