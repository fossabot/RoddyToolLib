/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import groovy.transform.CompileStatic

/**
 * VersionInterval allow to check whether a Version is within the interval. To simplify the implementation of
 * version compatibility given a set of compatibility intervals as equivalence class (in particular with respect
 * to transitivity) version interval are Comparable and can be merged (when overlapping).
 */
@CompileStatic
class VersionInterval implements Comparable<VersionInterval> {
    protected static final VersionLevel defaultCompatibilityLevel = VersionLevel.PATCH
    final Version from
    final Version to

    VersionInterval(Version from, Version to) {
        if (from <= to) {
            this.from = from
            this.to = to
        } else {
            this.to = from
            this.from = to
        }
    }

    boolean contains(final Version query, VersionLevel level = defaultCompatibilityLevel) {
        return this.from.compareTo(query, level) <= 0 &&
                this.to.compareTo(query, level) >= 0
    }

    boolean overlaps(VersionInterval other, VersionLevel level = defaultCompatibilityLevel) {
        /** Relations can be partial overlap, containment of one in the other and disjunction. */
        return this.contains(other.from, level) || this.contains(other.to, level) ||
                other.contains(this.from, level) || other.contains(this.to, level)
    }

    /** Lexicographic ordering of VersionIntervals. */
    @Override
    int compareTo(VersionInterval o) {
        return compareTo(o, defaultCompatibilityLevel)
    }
    int compareTo(VersionInterval other, VersionLevel level) {
        return this.from.compareTo(other.from, level) ?: this.to.compareTo(other.to, level)
    }

    /** Attempt to merge two interval, if they overlap. Otherwise return Optional.empty(). */
    Optional<VersionInterval> merge(VersionInterval other, VersionLevel level = defaultCompatibilityLevel) {
        if (this.overlaps(other, level)) {
            return Optional.of(new VersionInterval([this.from, other.from].min(), [this.to, other.to].max()))
        } else {
            return Optional.empty()
        }
    }

    @Override
    String toString() {
        "[${this.from}, ${this.to}]"
    }
}
