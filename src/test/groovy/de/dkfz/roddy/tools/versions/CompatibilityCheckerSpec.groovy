/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import spock.lang.Specification

class CompatibilityCheckerSpec extends Specification {

    def "version compatibility with ranges" (major1, minor1, patch1, revision1,
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
        CompatibilityChecker.compatibleTo(version1, version2, ranges, VersionLevel.PATCH) == compatible

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

    def "test backwards compatibility without ranges" (String version1, String version2, VersionLevel level, Boolean compatible) {
        expect:
        CompatibilityChecker.isBackwardsCompatibleTo(
                Version.fromString(version1),
                Version.fromString(version2),
                level) == compatible

        where:
        version1  | version2  | level                 | compatible
        "3.0.0-0" | "3.0.0-0" | VersionLevel.REVISION | true
        "2.0.0-0" | "3.0.0-0" | VersionLevel.REVISION | false
        "3.0.0-1" | "3.0.0-0" | VersionLevel.REVISION | true
        "3.0.0-2" | "3.0.0-0" | VersionLevel.REVISION | true
        "3.0.0-1" | "3.0.0-0" | VersionLevel.REVISION | true
        "3.0.0-0" | "3.0.0-1" | VersionLevel.REVISION | false
        "3.0.1-0" | "3.0.0-0" | VersionLevel.MINOR    | true
        "3.1.0-0" | "3.0.0-0" | VersionLevel.MINOR    | true
        "3.1.0-0" | "4.0.0-0" | VersionLevel.MINOR    | false
        "3.0.0-0" | "3.1.0-0" | VersionLevel.MINOR    | false
        "3.1.0-0" | "3.0.100-1000" | VersionLevel.MINOR    | true

    }


}
