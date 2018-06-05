/*
 * Copyright (c) 2018 German Cancer Research Center (DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */
package de.dkfz.roddy.tools.versions

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic
class CompatibilityChecker {

    final static VersionLevel defaultCompatibilityLevel = VersionLevel.REVISION

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
    private static Collection<VersionInterval> mergeOverlappingIntervals(final Collection<VersionInterval> intervals,
                                                                         VersionLevel level = defaultCompatibilityLevel) {
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
    static boolean compatibleTo(final Version first, final Version second, final Collection<VersionInterval> compatibilities,
                                VersionLevel level = defaultCompatibilityLevel) {
        if (null == level) {
            return true
        }
        def mergeCompatibilities = mergeOverlappingIntervals(compatibilities, level)
        Boolean sameCompatibilityLevel = first.compareTo(second, level) == 0
        if (sameCompatibilityLevel) {
            return true
        } else {
            VersionInterval sharedInterval = mergeCompatibilities.find { interval ->
                interval.contains(first, level) && interval.contains(second, level)
            }
            return null != sharedInterval
        }
    }

    @CompileDynamic
    static boolean isBackwardsCompatibleTo(final Version query, final Version base,
                                    VersionLevel firstNonEqualLevel = defaultCompatibilityLevel) {
        assert(firstNonEqualLevel != VersionLevel.MAJOR)
        return query.compareTo(base, firstNonEqualLevel.previous()) == 0 &&
                query[firstNonEqualLevel] >= base[firstNonEqualLevel]
    }


}
