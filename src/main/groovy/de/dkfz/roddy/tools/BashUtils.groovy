/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools

import groovy.transform.CompileStatic

import java.util.regex.Matcher

@CompileStatic
class BashUtils {
    static final String strongQuote(final String input) {
        "'${input.replaceAll("'", Matcher.quoteReplacement("'\\''"))}'"
    }
}
