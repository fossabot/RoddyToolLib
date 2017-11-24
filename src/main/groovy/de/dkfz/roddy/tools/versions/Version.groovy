/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools.versions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import java.text.ParseException
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * VersionInterval allow to check whether a Version is within the interval. To simplify the implementation of
 * version compatibility given a set of compatibility intervals as equivalence class (in particular with respect
 * to transitivity) version interval are Comparable and can be merged (when overlapping).
 */
@CompileStatic
class VersionInterval implements Comparable<VersionInterval> {
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

    boolean contains(final Version query, Version.VersionLevel level = Version.defaultCompatibilityLevel) {
        return this.from.compareTo(query, level) <= 0 &&
                this.to.compareTo(query, level) >= 0
    }

    boolean overlaps(VersionInterval other, Version.VersionLevel level = Version.defaultCompatibilityLevel) {
        /** Relations can be partial overlap, containment of one in the other and disjunction. */
        return this.contains(other.from, level) || this.contains(other.to, level) ||
                other.contains(this.from, level) || other.contains(this.to, level)
    }

    /** Lexicographic ordering of VersionIntervals. */
    @Override
    int compareTo(VersionInterval o) {
        return compareTo(o, Version.defaultCompatibilityLevel)
    }
    int compareTo(VersionInterval other, Version.VersionLevel level) {
        return this.from.compareTo(other.from, level) ?: this.to.compareTo(other.to, level)
    }

    /** Attempt to merge two interval, if they overlap. Otherwise return Optional.empty(). */
    Optional<VersionInterval> merge(VersionInterval other, Version.VersionLevel level = Version.defaultCompatibilityLevel) {
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

/**
 * Version class for semantic versioning. In particular a disciplined way of increasing versions at different semantic
 * versioning levels is supported as well as a compatibility relation as equivalence relation and tolerating differences
 * at any chosen version level. By default revision-level difference of otherwise identical versions are considered
 * compatible.
 *
 * Version is comparable to allow "versionA < versionB" expressions.
 */
@CompileStatic
class Version implements Comparable<Version> {

    static enum VersionLevel {
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
            return null;
        }

        /** With the next() and previous() methods VersionLevel can be used in Groovy range expression alla
         *  PATCH..REVISION. Taken from https://kousenit.org/2008/03/19/turning-java-enums-into-groovy-ranges
         */
        VersionLevel next() {
            VersionLevel[] vals = VersionLevel.values()
            return vals[(this.ordinal() + 1) % vals.length]
        }

        VersionLevel previous() {
            VersionLevel[] vals = VersionLevel.values()
            return vals[(this.ordinal() - 1 + vals.length) % vals.length]
        }
    }

    final Integer major
    final Integer minor
    final Integer patch
    final Integer revision

    /** Global default compatibility level. */
    static final VersionLevel defaultCompatibilityLevel = VersionLevel.PATCH

    Version (Integer major, Integer minor, Integer patch, Integer revision = 0) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.revision = revision
    }

    Version increaseMajor() {
        return new Version(major + 1, 0, 0, 0)
    }

    Version increaseMinor() {
        return new Version(major, minor + 1, 0, 0)
    }

    Version increasePatch() {
        return new Version(major, minor, patch + 1, 0)
    }

    Version increaseRevision() {
        return new Version(major, minor, patch, revision + 1)
    }

    String toString() {
        return "${major}.${minor}.${patch}-${revision}"
    }

    String toString(VersionLevel level) {
        String result = getAt(VersionLevel.MAJOR)
        if (level != VersionLevel.MAJOR) {
            for (l in VersionLevel.MINOR..level) {
                if (l != VersionLevel.REVISION)
                    result += '.' + getAt(l)
                else
                    result += '-' + getAt(l)
            }
        }
        return result
    }

    private static final Pattern versionPattern = Pattern.compile(/^(\d+)\.(\d+)\.(\d+)(-(\d+))?$/)

    static Version fromString (String versionString) {
        Matcher matcher = versionPattern.matcher(versionString)
        if (matcher.matches()) {
            if (null != matcher.group(5)) {
                return new Version(
                        matcher.group(1).toInteger(),
                        matcher.group(2).toInteger(),
                        matcher.group(3).toInteger(),
                        matcher.group(5).toInteger(), // the inner group of "(-(\d))+"
                )
            } else {
                return new Version(
                        matcher.group(1).toInteger(),
                        matcher.group(2).toInteger(),
                        matcher.group(3).toInteger(),
                        0
                )
            }
        } else {
            throw new ParseException("Could not parse version string '${versionString}'", 0)
        }
    }


    /** The indexed access allows selecting the version level of the version. Additionally, getAt() allows a
     *  destructuring bind of the form `def (major, minor, patch, revision) = version`.
     *
     * @param level    VersionLevel to be returned.
     * @return
     */
    @CompileDynamic
    Integer getAt (VersionLevel level) {
        if (level == VersionLevel.MAJOR) major
        else if (level == VersionLevel.MINOR) minor
        else if (level == VersionLevel.PATCH) patch
        else if (level == VersionLevel.REVISION) revision
        else throw new IndexOutOfBoundsException("Use 1-based indices: 1.2.3-4")
    }

    /** Convenience method. */
    @CompileDynamic
    Integer getAt (int level) {
        return getAt(VersionLevel.fromInteger(level))
    }

    /** Note: These equals and compareTo are too important to mess around with compatibility levels. Therefore, for
     *        these methods the most exact comparison is done. E.g. when compatibility intervals are to be sorted
     *        this needs to be done exactly.
     *
     * @param other
     * @return exact semantic equality of Versions
     */
    Boolean equals (Version other) {
        return this.compareTo(other, VersionLevel.REVISION) == 0
    }

    @Override
    int compareTo(Version o) {
        return compareTo(o, VersionLevel.REVISION)
    }

    /** A more generic version of comparison function that allows to compare up to a specific version level. This is
     *  used to determine compatibility. */
    int compareTo(Version o, VersionLevel level) {
        for (l in VersionLevel.MAJOR..level) {
            int result = this[l].compareTo(o[l])
            if (result != 0)
                return result
        }
        return 0
    }

    /** The compatibility-level is the lowest VersionLevel (order as in the VersionLevel enum; with MAJOR first (low)
     *  and REVISION last (high)) two compared versions need to be identical in, to be considered compatible.
     *
     *  By default, two versions are compatible, if they only differ in the REVISION. The default compatibility-level
     *  is therefore VersionLevel.PATCH. For instance, by default the versions 1.1.1-0 and 1.1.1-1 are considered
     *  compatible, but 1.1.2-0 is not compatible to the first two. If the compatibility level for the comparisons is
     *  set to null, then all versions are compatible.
     *
     *  The second way to declare compatibility is by explicitly declaring compatibility intervals. If there is any
     *  interval that contains both versions (this and the other) then the two versions are compatible. Also these
     *  checks account for implicit compatibility defined by the compatibility level. Note that, if two versions are
     *  compatible then all intermediate versions are also compatible. Therefore, compatibility generates an equivalence
     *  relation on versions (reflexive, symmetric, transitive).
     */

    /** This is used to ensure transitivity of compatibility */
    @CompileStatic(TypeCheckingMode.SKIP)
    private Collection<VersionInterval> mergeOverlappingIntervals(final Collection<VersionInterval> intervals, VersionLevel level = defaultCompatibilityLevel) {
        return intervals.toSorted().inject(new LinkedList<VersionInterval>()) { List<VersionInterval> result, VersionInterval next ->
            if (result.isEmpty()) {
                [next]
            } else {
                def merged = result.first().merge(next, level)
                if (merged.isPresent()) {
                    result.drop(1)
                    result.add(0, merged.get())
                } else {
                    result.add(0, next)
                }
                result
            }
        }
    }

    /**
     *
     * @param other
     * @param compatibilities   List of compatibility intervals. Will be curated into equivalence classes.
     * @param level             Compatibility level used for implicit compatibilities. By default REVISIONs are
     *                          compatible. A value of "null" serves to let all versions be compatible.
     * @return
     */
    boolean compatibleTo(final Version other, final Collection<VersionInterval> compatibilities, VersionLevel level = defaultCompatibilityLevel) {
        if (null == level) {
            return true
        }
        def mergeCompatibilities = mergeOverlappingIntervals(compatibilities, level)
        Boolean sameCompatibilityLevel = this.compareTo(other, level) == 0
        if (sameCompatibilityLevel) {
            return true
        } else {
            VersionInterval sharedInterval = mergeCompatibilities.find { interval ->
                interval.contains(this, level) && interval.contains(other, level)
            }
            return null != sharedInterval
        }
    }


}

