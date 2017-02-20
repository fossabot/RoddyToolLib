/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.eilslabs.tools.conversion

import de.dkfz.eilslabs.tools.conversion.ConversionHelperMethods
import groovy.transform.CompileStatic

/**
 * Created by heinold on 14.07.16.
 */
@CompileStatic
class ConversionHelperMethodsTest extends GroovyTestCase {
    void testToInt() {
        assert 1 == ConversionHelperMethods.toInt("1")
        assert 1 == ConversionHelperMethods.toInt("a0", 1)
    }

    void testToFloat() {
        assert 1.0f == ConversionHelperMethods.toFloat("1.0")
        assert 1.0f == ConversionHelperMethods.toFloat("a0", 1.0f)
    }

    void testToDouble() {
        assert 1.0d == ConversionHelperMethods.toDouble("1.0")
        assert 1.0d == ConversionHelperMethods.toDouble("a0", 1.0)
    }

    void testToBoolean() {
        assert true == ConversionHelperMethods.toBoolean("true", false)
        assert true == ConversionHelperMethods.toBoolean("TRUE", false)
        assert true == ConversionHelperMethods.toBoolean("True", false)
        assert true == ConversionHelperMethods.toBoolean("1", false)

        // Would fail! toBoolean is either true or false and throws no exceptions.
        assert true == ConversionHelperMethods.toBoolean("Flack", true)

        assert false == ConversionHelperMethods.toBoolean("0", false)
        assert false == ConversionHelperMethods.toBoolean("false", true)
        assert false == ConversionHelperMethods.toBoolean("False", true)

        assert false == ConversionHelperMethods.toBoolean("Flack", false)
    }

    void testIsInteger() {
        assert ConversionHelperMethods.isInteger("1")
        assert ConversionHelperMethods.isInteger("10")

        assert !ConversionHelperMethods.isInteger("1.0")
        assert !ConversionHelperMethods.isInteger("")
        assert !ConversionHelperMethods.isInteger("1.a")
        assert !ConversionHelperMethods.isInteger("a")

    }

    void testIsFloat() {
        assert ConversionHelperMethods.isFloat("1")
        assert ConversionHelperMethods.isFloat("1.0e10f")

        assert !ConversionHelperMethods.isFloat("1.0")
        assert !ConversionHelperMethods.isFloat("1.0e10")

        assert !ConversionHelperMethods.isFloat("")
        assert !ConversionHelperMethods.isFloat("1.a")
        assert !ConversionHelperMethods.isFloat("a")

    }

    void testIsDouble() {
        assert ConversionHelperMethods.isDouble("1")
        assert ConversionHelperMethods.isDouble("1.0")
        assert ConversionHelperMethods.isDouble("1.0e10")

        assert !ConversionHelperMethods.isDouble("")
        assert !ConversionHelperMethods.isDouble("1.a")
        assert !ConversionHelperMethods.isDouble("a")
    }

    void testIsNullOrEmpty() {
        assert ConversionHelperMethods.isNullOrEmpty([])
        assert ConversionHelperMethods.isNullOrEmpty((String) null)
        assert ConversionHelperMethods.isNullOrEmpty((Collection) null)
        assert ConversionHelperMethods.isNullOrEmpty("")

        assert !ConversionHelperMethods.isNullOrEmpty("a")
        assert !ConversionHelperMethods.isNullOrEmpty(["a"])
        assert !ConversionHelperMethods.isNullOrEmpty([1])
    }

    void testIsDefinedArray() {
        assert ConversionHelperMethods.isDefinedArray("( )")
        assert ConversionHelperMethods.isDefinedArray("( a b )")
        assert ConversionHelperMethods.isDefinedArray("( a )")
        assert !ConversionHelperMethods.isDefinedArray("")
        assert !ConversionHelperMethods.isDefinedArray("a b c")
        assert !ConversionHelperMethods.isDefinedArray("a;b;c")
    }

    /**
     * Test for deteremineTypeOfValue in ConfigurationValue class.
     * Test is here for low level of method logic. Nearly all the logic is in
     * the conversion methods.
     */
   /* void testDetermineTypeOfValue() {
        assert ConfigurationValue.determineTypeOfValue("( a b c )") == "bashArray"
        assert ConfigurationValue.determineTypeOfValue('"( a b c )"') == "string"
        assert ConfigurationValue.determineTypeOfValue("'( a b c )'") == "string"
        assert ConfigurationValue.determineTypeOfValue("1.0") == "double"
        assert ConfigurationValue.determineTypeOfValue("1.0f") == "float"
        assert ConfigurationValue.determineTypeOfValue("1") == "integer"
        assert ConfigurationValue.determineTypeOfValue("") == "string"
        assert ConfigurationValue.determineTypeOfValue("ba") == "string"
    }
*/
}
