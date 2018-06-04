/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import spock.lang.Specification

class VersionLevelSpec extends Specification {

    def "version-level toString"() {
        when:
        def version = new Version(1,2,3,4)
        then:
        version.toString() == "1.2.3-4"
        version.toString(VersionLevel.REVISION) == "1.2.3-4"
        version.toString(VersionLevel.PATCH) == "1.2.3"
        version.toString(VersionLevel.MINOR) == "1.2"
        version.toString(VersionLevel.MAJOR) == "1"
    }


}
