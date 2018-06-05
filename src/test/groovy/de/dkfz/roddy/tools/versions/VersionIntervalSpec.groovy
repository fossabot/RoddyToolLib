/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import spock.lang.Specification

class VersionIntervalSpec extends Specification  {

    def "version-interval containment" () {
        when:
        def interval = new VersionInterval(new Version(1, 1, 0, 1), new Version(1, 2, 0, 10))

        then:
        // At maximal precision VersionLevel
        interval.contains(new Version(1, 1, 0, 1), VersionLevel.REVISION)
        interval.contains(new Version(1, 2, 0, 10), VersionLevel.REVISION)

        !interval.contains(new Version(1, 1, 0, 0), VersionLevel.REVISION)
        !interval.contains(new Version(1, 2, 0, 11), VersionLevel.REVISION)

        // At default VersionLevel.
        interval.contains(new Version(1, 1, 0, 0))
        interval.contains(new Version(1, 2, 0, 11))

        !interval.contains(new Version(1, 0, 0, 0))
        !interval.contains(new Version(1, 2, 1, 0))

    }

    def "version interval-interval overlaps" () {
        given:
        def i1 = new VersionInterval(Version.fromString("1.0.0-0"), Version.fromString("1.0.0-0"))
        def i2 = new VersionInterval(Version.fromString("1.0.0-1"), Version.fromString("1.1.0-0"))
        def i3 = new VersionInterval(Version.fromString("1.1.0-0"), Version.fromString("1.2.0-0"))
        def i4 = new VersionInterval(Version.fromString("1.2.1-0"), Version.fromString("1.3.0-0"))
        expect:
        i1.overlaps(i2)
        !i1.overlaps(i2, VersionLevel.REVISION)
        i2.overlaps(i3, VersionLevel.REVISION)
        !i3.overlaps(i4, VersionLevel.PATCH)
        i3.overlaps(i4, VersionLevel.MINOR)
    }

}
