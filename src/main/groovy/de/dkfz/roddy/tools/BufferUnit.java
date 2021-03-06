/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools;

/**
 * Some buffer units like M(egabyte), G(igagbyte), T(erabyte)
 */
public enum BufferUnit {
    k(1024),
    K(1024),
    m(1024 * 1024),
    M(1024 * 1024),
    g(1024 * 1024 * 1024),
    G(1024 * 1024 * 1024),
    t(1024L * 1024 * 1024 * 1024),
    T(1024L * 1024 * 1024 * 1024),
    p(1024L * 1024 * 1024 * 1024 * 1024),
    P(1024L * 1024 * 1024 * 1024 * 1024);

    public final long multiplier;

    BufferUnit(long multiplier) {
        this.multiplier = multiplier;
    }


}
