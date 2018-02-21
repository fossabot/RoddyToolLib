/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools

import groovy.transform.CompileStatic
import org.junit.BeforeClass
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Created by heinold on 06.06.17.
 */
@CompileStatic
class NativeLinuxZipCompressorTest {

    public static TemporaryFolder testBaseDirRoot = new TemporaryFolder()
    public static File root

    public static File srcWithBadFile
    public static File targetWithBadFile
    public static File badFile

    public static File srcWithGoodFile
    public static File targetWithGoodFile
    public static File goodFile

    @BeforeClass
    static void setup() {
        testBaseDirRoot.create()
        root = testBaseDirRoot.root

        srcWithBadFile = new File(root, "fromWithBadChild")
        srcWithBadFile.mkdirs()
        new File(srcWithBadFile, "FileA") << "Sometext"
        new File(srcWithBadFile, "FileB") << "Sometext"
        new File(srcWithBadFile, "FileC") << "Sometext"
        badFile = new File(srcWithBadFile, "FileBAD")
        badFile << "Sometext"
        badFile.setReadable(false)
        targetWithBadFile = new File(root, "outBad.zip")

        srcWithGoodFile = new File(root, "fromWithGoodChildren")
        srcWithGoodFile.mkdirs()
        goodFile = new File(srcWithGoodFile, "FileA") << "Sometext"
        new File(srcWithGoodFile, "FileB") << "Sometext"
        new File(srcWithGoodFile, "FileC") << "Sometext"
        targetWithGoodFile = new File(root, "outGood.zip")
    }

    @Test
    void testCompressGoodFile() {
        new RoddyIOHelperMethods.NativeLinuxZipCompressor().compressFile(goodFile, targetWithGoodFile)
    }

    @Test(expected = IOException)
    void testCompressBadFile() {
        new RoddyIOHelperMethods.NativeLinuxZipCompressor().compressFile(badFile, targetWithBadFile)
    }

    @Test
    void testCompressGoodDirectory() {
        new RoddyIOHelperMethods.NativeLinuxZipCompressor().compressDirectory(srcWithGoodFile, targetWithGoodFile)
    }

    @Test(expected = IOException)
    void testCompressBadDirectory() {
        new RoddyIOHelperMethods.NativeLinuxZipCompressor().compressDirectory(srcWithBadFile, targetWithBadFile)
    }

    @Test
    void testGetCompressionString() {
        String cString = new RoddyIOHelperMethods.NativeLinuxZipCompressor().getCompressionString(srcWithBadFile, targetWithBadFile).toString()
        assert cString == "[[ -f \"${targetWithBadFile}\" ]] && rm ${targetWithBadFile}; cd ${root} && zip -r9 ${targetWithBadFile} ${srcWithBadFile.name} -x '*.svn*' > /dev/null && md5sum ${targetWithBadFile}".toString()
    }

}
