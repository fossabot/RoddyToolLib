/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools.versions

import spock.lang.*

import java.text.ParseException

class VersionSpec extends Specification {

    def "construct version from string" () {
        expect:
        new Version(1,2,3,4) == Version.fromString("1.2.3-4")
        new Version(1,2,0) == Version.fromString("1.2.0")
        new Version(1, 0, 0, 1) == Version.fromString("1.0.0-1")

        when:
        Version.fromString("1.2,3")
        then:
        final ParseException e1 = thrown()

        when:
        Version.fromString("1.0.1-r")
        then:
        final ParseException e2 = thrown()
    }


    def "construct version from partial version string" (String theString, String theParseResult) {
        expect:
        Version.fromString(theString).toString() == theParseResult

        where:
        theString | theParseResult
        "1"       | "1.0.0-0"
        "1.2"     | "1.2.0-0"
        "1.2.3"   | "1.2.3-0"
        "1.2.3-4" | "1.2.3-4"
    }



    def "increasing the major number" () {
        when:
        Version version = new Version (1, 2, 3, 4)
        version = version.increaseMajor()
        then:
        version.major == 2
        version.minor == 0
        version.patch == 0
        version.revision == 0
    }

    def "increasing the minor number" () {
        when:
        Version version = new Version (4, 3, 2, 1)
        version = version.increaseMinor()
        then:
        version.major == 4
        version.minor == 4
        version.patch == 0
        version.revision == 0
    }

    def "increasing the patch" () {
        when:
        Version version = new Version (3, 1, 4, 9)
        version = version.increasePatch()
        then:
        version.major == 3
        version.minor == 1
        version.patch == 5
        version.revision == 0
    }

    def "increasing the revision" () {
        when:
        Version version = new Version (1, 2, 3, 4)
        version = version.increaseRevision()
        then:
        version.major == 1
        version.minor == 2
        version.patch == 3
        version.revision == 5
    }

    def "getAt and destructuring binding" () {
        when:
        Version version = new Version (12, 11, 10, 9)
        then:
        version[VersionLevel.MAJOR] == 12
        version[VersionLevel.MINOR] == 11
        version[VersionLevel.PATCH] == 10
        version[VersionLevel.REVISION] == 9
        def (major, minor, patch, revision) = version
        major == 12
        minor == 11
        patch == 10
        revision == 9
    }

}