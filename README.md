# minoinject
A simple hooker to JVM, editing function while some JAR is running

# What is it ?
minoinject is hooking to another Java App using JVM, then it retrieves all functions and properties.
In this example I took for test a Java App with a `simpleFunc` that was printing `Hello World`

# What does it do ?
After targeting a JAR (here: Simple.jar => [here](https://github.com/minot0r/minoinject/blob/bd368b15c4609d3aa1d0525987bd79a1921d6dc8/src/com/minoinject/hook/Hooker.java#L28))
It searches for my function `simpleFunc` and is inserting `...println("Modified from JavaAgent")` using Javassist
