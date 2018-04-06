package de.dkfz.roddy.tools

import spock.lang.Specification

class RoddyIOHelperMethodsTest extends Specification {

    def "FindComponentIndexInPath"() {
        when:
        def result = RoddyIOHelperMethods.findComponentIndexInPath("/a/b/\${sample}/d", '${sample}')
        then:
        result == Optional.of(2)
    }
}
