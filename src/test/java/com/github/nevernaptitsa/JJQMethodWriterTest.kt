package com.github.nevernaptitsa

import org.apache.commons.io.output.StringBuilderWriter
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mdkt.compiler.InMemoryJavaCompiler
import java.io.PrintWriter
import java.lang.reflect.ParameterizedType
import java.util.Arrays
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName
import kotlin.test.assertEquals

/* Main Test Body */

@RunWith(JUnitPlatform::class)
class JJQMethodWriterTest : Spek({

    given("an object selection expression" ) {
        val objectSelectExpr = ".foo.bar"
        it("generates a method signature has a data class for input and a string for output") {
            val fooBar = Baz(Foo("a-value"))
            val output = runTestWithInputs(objectSelectExpr, Baz::class, String::class, fooBar)
            assertEquals("a-value", output)
        }
        it("generates a method signature that has a list for input and a list of string for output") {
            val bazList = BazList()
            bazList.add(Baz(Foo("a-value")))
            bazList.add(Baz(Foo("b-value")))
            val output = runTestWithInputs(objectSelectExpr, BazList::class, StringList::class, bazList)
            assertEquals("a-value", output.get(0))
            assertEquals("b-value", output.get(1))
        }
    }

    given("a flatmap expression followed by a structure selector") {
        val listExpression = "[] | .foo.bar"
        it("generates a method signature that has a list-list for input and a list of string for output") {
            val bazList1 = BazList()
            bazList1.add(Baz(Foo("a-value")))
            bazList1.add(Baz(Foo("b-value")))
            val bazList2 = BazList()
            bazList2.add(Baz(Foo("c-value")))
            bazList2.add(Baz(Foo("d-value")))
            val bazListList = BazListList()
            bazListList.add(bazList1)
            bazListList.add(bazList2)
            val output = runTestWithInputs(listExpression, BazListList::class, StringList::class, bazListList)
            assertEquals("a-value", output.get(0))
            assertEquals("b-value", output.get(1))
            assertEquals("c-value", output.get(2))
            assertEquals("d-value", output.get(3))
        }
    }
})

/* Helpers */

class ReflectedGetterFieldLocator : JJQMethodWriter.FieldLocator {

    override fun locateFieldInfo(currentType: JJQMethodWriter.Type, logicalField: JJQMethodWriter.Field): JJQMethodWriter.FieldAccessorInfo? {
        val methodOptional = Arrays.stream(Class.forName(currentType.name).declaredMethods)
                .filter({ m->
                    m.name == "get" + logicalField.name.substring(0, 1).toUpperCase() +
                            if (logicalField.name.length > 1) logicalField.name.substring(1, logicalField.name.length)
                            else ""
                })
                .findFirst()
        if (methodOptional.isPresent) {
            val method = methodOptional.get()
            return JJQMethodWriter.FieldAccessorInfo(method.name, JJQMethodWriter.Type(method.returnType.name), JJQMethodWriter.AccessorType.METHOD)
        } else {
            return null
        }
    }

}

class ReflectedTypeInspector : JJQMethodWriter.TypeInspector {

    override fun collectionContents(typeToUnpack: JJQMethodWriter.Type): JJQMethodWriter.Type {
        return JJQMethodWriter.Type((Class.forName(typeToUnpack.name).genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName)
    }

    override fun subclasses(typeToCheck: JJQMethodWriter.Type, typeToSubclass: JJQMethodWriter.Type): Boolean {
        return Class.forName(typeToSubclass.name).isAssignableFrom(Class.forName(typeToCheck.name))
    }

}

data class Foo(val bar: String)
data class Baz(val foo: Foo)
class BazList : ArrayList<Baz>()
class StringList : ArrayList<String>()
class BazListList : ArrayList<BazList>()
data class PrintWriterAndOutputStream(val writer: PrintWriter, val os: StringBuilderWriter)
var classNum = 0

fun <I: Any, O: Any> runTestWithInputs(objectSelectExpr: String, inputClass: KClass<I>, outputClass: KClass<O>, testData: I): O {
    val writerAndStream = startClassWriter()
    val engine = JJQMethodWriter(
            output=writerAndStream.writer,
            expression=objectSelectExpr,
            method= JJQMethodWriter.Method(name="method",
                    visibility = JJQMethodWriter.Visibility.PUBLIC,
                    outputType = JJQMethodWriter.Type(outputClass.jvmName),
                    inputType = JJQMethodWriter.Type(inputClass.jvmName)),
            fieldLocator = ReflectedGetterFieldLocator(),
            typeInspector = ReflectedTypeInspector()
    )
    engine.writeMethod()
    val clazz = finishClass(writerAndStream)
    val clazzInstance = clazz.newInstance()
    val method = clazz.getMethod("method", inputClass.java)
    return outputClass.java.cast(method.invoke(clazzInstance, testData))
}

fun startClassWriter(): PrintWriterAndOutputStream {
    val output = StringBuilderWriter()
    val writer = PrintWriter(output)
    writer.printf("public class TestClass%s{\n", ++classNum)
    return PrintWriterAndOutputStream(writer, output)
}

fun finishClass(wos: PrintWriterAndOutputStream): Class<*> {
    wos.writer.printf("}")
    wos.writer.close()
    val clazzContents = wos.os.builder.toString()
    println(clazzContents)
    return InMemoryJavaCompiler.compile("TestClass"+classNum, clazzContents)
}