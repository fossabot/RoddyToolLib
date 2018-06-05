/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import groovy.transform.CompileStatic

@CompileStatic
enum VersionLevel {
    MAJOR,
    MINOR,
    PATCH,
    REVISION;

    static VersionLevel fromInteger(int x) {
        switch(x) {
            case 0:
                return MAJOR;
            case 1:
                return MINOR;
            case 2:
                return PATCH;
            case 3:
                return REVISION;
            default:
                throw new NumberFormatException("Cannot interpret integer ${x} as VersionLevel")
        }
    }

    /** With the next() and previous() methods VersionLevel can be used in Groovy range expression alla
     *  PATCH..REVISION. Taken from https://kousenit.org/2008/03/19/turning-java-enums-into-groovy-ranges
     */
    VersionLevel next() {
        VersionLevel[] vals = values()
        return vals[(this.ordinal() + 1) % vals.length]
    }

    VersionLevel previous() {
        VersionLevel[] vals = values()
        return vals[(this.ordinal() - 1 + vals.length) % vals.length]
    }
}

