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
import com.github.nevernaptitsa.model.Type
import com.github.nevernaptitsa.model.Method
import com.github.nevernaptitsa.model.Visibility
import com.github.nevernaptitsa.model.FieldAccessorInfo
import com.github.nevernaptitsa.model.Field
import com.github.nevernaptitsa.model.AccessorType
import kotlin.test.assertEquals

/* Main Test Body */

@RunWith(JUnitPlatform::class)
class JJQMethodWriterTest : Spek({

    given("an object selection expression" ) {
        val objectSelectExpr = ".foo.bar"
        it("generates a method signature has a data class for input and a string for output") {
            val fooBar = Baz(Foo("a-value"))
            val output = runTestWithInputs(objectSelectExpr, Baz::class.java, String::class.java, fooBar)
            assertEquals("a-value", output)
        }
        it("generates a method signature that has a list for input and a list of string for output") {
            val bazList = BazList()
            bazList.add(Baz(Foo("a-value")))
            bazList.add(Baz(Foo("b-value")))
            val output = runTestWithInputs(objectSelectExpr, BazList::class.java, StringList::class.java, bazList)
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
            val output = runTestWithInputs(listExpression, BazListList::class.java, StringList::class.java, bazListList)
            assertEquals("a-value", output.get(0))
            assertEquals("b-value", output.get(1))
            assertEquals("c-value", output.get(2))
            assertEquals("d-value", output.get(3))
        }
    }

    given("a boolean literal expression of true") {
        val booleanLiteralExpression = "true"
        it("generates a method that always evaluates to true") {
            val output = runTestWithInputs(booleanLiteralExpression, Any::class.java, java.lang.Boolean::class.java, "foo")
            assertEquals(java.lang.Boolean(true), output)
        }
    }

    given("a boolean literal expression containing an operator that always evaluates as false") {
        val booleanAndExpression = "true and false"
        it("generates a method that always evaluates to false") {
            val output = runTestWithInputs(booleanAndExpression, Any::class.java, java.lang.Boolean::class.java, "foo")
            assertEquals(java.lang.Boolean(false), output)
        }
    }

    given("a boolean or expression containing access to a field and an operator") {
        val booleanSelectExpression = ".snuggly or false"
        it("generates a method that evaluates to true with a true input") {
            val output = runTestWithInputs(booleanSelectExpression, Cat::class.java, java.lang.Boolean::class.java, Cat(true))
            assertEquals(java.lang.Boolean(true), output)
        }
        it("generates a method that evaluates to false with a false input") {
            val output = runTestWithInputs(booleanSelectExpression, Cat::class.java, java.lang.Boolean::class.java, Cat(false))
            assertEquals(java.lang.Boolean(false), output)
        }
    }

    given("a boolean and expression containing access to a field and an operator that always evaluates false") {
        val booleanSelectExpression = ".snuggly and false"
        it("generates a method that always evaluates to false with a true input") {
            val output = runTestWithInputs(booleanSelectExpression, Cat::class.java, java.lang.Boolean::class.java, Cat(true))
            assertEquals(java.lang.Boolean(false), output)
        }
        it("generates a method that always evaluates to false with a false input") {
            val output = runTestWithInputs(booleanSelectExpression, Cat::class.java, java.lang.Boolean::class.java, Cat(false))
            assertEquals(java.lang.Boolean(false), output)
        }
    }
})

/* Helpers */

internal class ReflectedGetterFieldLocator : FieldLocator {

    override fun locateFieldInfo(currentType: Type, logicalField: Field): FieldAccessorInfo? {
        val methodOptional = Arrays.stream(Class.forName(currentType.name).declaredMethods)
                .filter({ m->
                    m.name == "get" + logicalField.name.substring(0, 1).toUpperCase() +
                            if (logicalField.name.length > 1) logicalField.name.substring(1, logicalField.name.length)
                            else ""
                })
                .findFirst()
        if (methodOptional.isPresent) {
            val method = methodOptional.get()
            return FieldAccessorInfo(method.name, Type(method.returnType.name), AccessorType.METHOD)
        } else {
            return null
        }
    }

}

internal class ReflectedTypeInspector : TypeInspector {

    override fun collectionContents(typeToUnpack: Type): Type {
        return Type((Class.forName(typeToUnpack.name).genericSuperclass as ParameterizedType).actualTypeArguments[0].typeName)
    }

    override fun subclasses(typeToCheck: Type, typeToSubclass: Type): Boolean {
        when (typeToCheck.name) {
            "boolean", "byte", "short", "int", "long", "char", "float", "double" -> return false
        }
        return Class.forName(typeToSubclass.name).isAssignableFrom(Class.forName(typeToCheck.name))
    }

}

data class Foo(val bar: String)
data class Baz(val foo: Foo)
data class Cat(val snuggly: Boolean)
class BazList : ArrayList<Baz>()
class StringList : ArrayList<String>()
class BazListList : ArrayList<BazList>()
data class PrintWriterAndOutputStream(val writer: PrintWriter, val os: StringBuilderWriter)
var classNum = 0

fun <I: Any, O: Any> runTestWithInputs(objectSelectExpr: String, inputClass: Class<I>, outputClass: Class<O>, testData: I): O {
    val writerAndStream = startClassWriter()
    val engine = JJQMethodWriter(
            output=writerAndStream.writer,
            expression=objectSelectExpr,
            method= Method(name="method",
                    visibility = Visibility.PUBLIC,
                    outputType = Type(outputClass.name),
                    inputType = Type(inputClass.name)),
            fieldLocator = ReflectedGetterFieldLocator(),
            typeInspector = ReflectedTypeInspector()
    )
    engine.writeMethod()
    val clazz = finishClass(writerAndStream)
    val clazzInstance = clazz.newInstance()
    val method = clazz.getMethod("method", inputClass)
    return outputClass.cast(method.invoke(clazzInstance, testData))
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