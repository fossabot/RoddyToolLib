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
        version[Version.VersionLevel.MAJOR] == 12
        version[Version.VersionLevel.MINOR] == 11
        version[Version.VersionLevel.PATCH] == 10
        version[Version.VersionLevel.REVISION] == 9
        def (major, minor, patch, revision) = version
        major == 12
        minor == 11
        patch == 10
        revision == 9
    }

    def "version-interval containment" () {
        when:
        def interval = new VersionInterval(new Version(1, 1, 0, 1), new Version(1, 2, 0, 10))

        then:
        // At maximal precision VersionLevel
        interval.contains(new Version(1, 1, 0, 1), Version.VersionLevel.REVISION)
        interval.contains(new Version(1, 2, 0, 10), Version.VersionLevel.REVISION)

        !interval.contains(new Version(1, 1, 0, 0), Version.VersionLevel.REVISION)
        !interval.contains(new Version(1, 2, 0, 11), Version.VersionLevel.REVISION)

        // At default VersionLevel.
        interval.contains(new Version(1, 1, 0, 0))
        interval.contains(new Version(1, 2,0,11))

        !interval.contains(new Version(1, 0, 0, 0))
        !interval.contains(new Version(1, 2,1,0))

    }

    def "version interval-interval overlaps" () {
        given:
        def i1 = new VersionInterval(Version.fromString("1.0.0-0"), Version.fromString("1.0.0-0"))
        def i2 = new VersionInterval(Version.fromString("1.0.0-1"), Version.fromString("1.1.0-0"))
        def i3 = new VersionInterval(Version.fromString("1.1.0-0"), Version.fromString("1.2.0-0"))
        def i4 = new VersionInterval(Version.fromString("1.2.1-0"), Version.fromString("1.3.0-0"))
        expect:
        i1.overlaps(i2)
        !i1.overlaps(i2, Version.VersionLevel.REVISION)
        i2.overlaps(i3, Version.VersionLevel.REVISION)
        !i3.overlaps(i4, Version.VersionLevel.PATCH)
        i3.overlaps(i4, Version.VersionLevel.MINOR)
    }

    def "version compatibility" (major1, minor1, patch1, revision1,
                                 major2, minor2, patch2, revision2,
                                 compatible) {
        when:
        def ranges = [
                new VersionInterval(new Version(0, 0, 0, 0), new Version(1, 0, 0, 0,)),
                new VersionInterval(new Version(1, 1, 0, 1), new Version(1, 1, 0, 10)),
                new VersionInterval(new Version(1, 1, 0, 10), new Version(1, 2, 0, 0)),
                new VersionInterval(new Version(1,2,0, 0), new Version(1,3,0,0)),
                new VersionInterval(new Version(1,3,0, 100), new Version(1,4,0,0))
        ]
        def version1 = new Version(major1, minor1, patch1, revision1)
        def version2 = new Version(major2, minor2, patch2, revision2)

        then:
        version1.compatibleTo(version2, ranges) == compatible

        where:
        major1 | minor1 | patch1 | revision1 | major2 | minor2 | patch2 | revision2 | compatible
             1 |      0 |      0 |         0 |      1 |      0 |      1 |         0 |      false  // inside/outside first interval
             0 |      0 |      0 |         0 |      1 |      1 |      0 |         1 |      false  // from first and second interval
             1 |      1 |      0 |         9 |      1 |      1 |      0 |        11 |       true  // from second and third interval but same patch level
             1 |      3 |      3 |         0 |      1 |      3 |      3 |       100 |       true  // outside intervals but same patch level
             1 |      1 |      0 |       100 |      1 |      1 |    100 |      1000 |       true  // different patch but same interval
             1 |      1 |      0 |         1 |      1 |      3 |      0 |         0 |       true  // transitive across overlapping intervals
             1 |      1 |      0 |         1 |      1 |      4 |      0 |         0 |       true  // transitive across multiple intervals including revision gap-bridging

    }

    def "version-level toString"() {
        when:
        def version = new Version(1,2,3,4)
        then:
        version.toString() == "1.2.3-4"
        version.toString(Version.VersionLevel.REVISION) == "1.2.3-4"
        version.toString(Version.VersionLevel.PATCH) == "1.2.3"
        version.toString(Version.VersionLevel.MINOR) == "1.2"
        version.toString(Version.VersionLevel.MAJOR) == "1"
    }

}