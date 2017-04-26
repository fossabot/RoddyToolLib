/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.tools.logging

import de.dkfz.roddy.tools.RoddyIOHelperMethods
import groovy.transform.CompileStatic
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.rules.TemporaryFolder

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

/**
 * Test class to cover RoddyIOHelperMethods.
 *
 * Created by heinold on 11.11.15.
 */
@CompileStatic
public class RoddyIOHelperMethodsTest {

    public static TemporaryFolder testBaseDirRoot = new TemporaryFolder()
    public static File testBaseDir
    public static File testBaseDirCopy

    @BeforeClass
    static void setupClass() {
        testBaseDirRoot.create()
        testBaseDir = new File(testBaseDirRoot.root, "test_a")
        testBaseDirCopy = new File(testBaseDirRoot.root, "test_copy")

    }

    @AfterClass
    static void cleanupClass() {
        testBaseDir.delete()
    }

    @Test
    public void testGetMD5OfText() {
        assert RoddyIOHelperMethods.getMD5OfText("ABCD") == "cb08ca4a7bb5f9683c19133a84872ca7";
    }

    @Test
    public void testGetMD5OfFile() {
        String md5
        File testFile
        try {
            testFile = new File(testBaseDir, "A");
            testFile << "ABCD";
            md5 = RoddyIOHelperMethods.getMD5OfFile(testFile);
        } finally {
            testFile?.delete();
        }
        assert md5 == "cb08ca4a7bb5f9683c19133a84872ca7";
    }


    private List<String> getMD5OfFilesInDirectories(File testBaseDir, File md5TestDir, List<String> filenames) {
        String md5TestDirShort = (md5TestDir.absolutePath - testBaseDir.absolutePath)[1..-1];
        return filenames.collect {
            File f = new File(md5TestDir, it)
            f << it
            return RoddyIOHelperMethods.getMD5OfText("${md5TestDirShort}/${it}") + RoddyIOHelperMethods.getMD5OfFile(f)
        }
    }

    @Test
    public void testGetSingleMD5OfFilesInDifferentDirectories() {
        File md5TestDir1 = new File(testBaseDir, "md5sumtest1");
        File md5TestDir2 = new File(md5TestDir1, "md5sumtest2");
        md5TestDir1.mkdirs();
        md5TestDir2.mkdirs();

        assert getMD5OfFilesInDirectories(testBaseDir, md5TestDir1, ["A", "B"]).join("\n") != getMD5OfFilesInDirectories(testBaseDir, md5TestDir2, ["A", "B"]).join("\n")
        assert getMD5OfFilesInDirectories(testBaseDir, md5TestDir1, ["A", "B"]).join("\n") != getMD5OfFilesInDirectories(testBaseDir, md5TestDir1, ["A", "C"]).join("\n")
    }

    @Test
    public void testGetSingleMD5OfFilesInDirectory() {
        File md5TestBaseDir = RoddyIOHelperMethods.assembleLocalPath(RoddyIOHelperMethodsTest.testBaseDir, "testGetSingleMD5OfFilesInDirectory_a")
        File md5TestDir = RoddyIOHelperMethods.assembleLocalPath(testBaseDir, "testGetSingleMD5OfFilesInDirectory_a", "md5sumtest");
        File md5TestSubDir = new File(md5TestDir, "sub");
        md5TestSubDir.mkdirs();

        List<String> aList = getMD5OfFilesInDirectories(md5TestBaseDir, md5TestDir, ["A", "B", "C", "D"])
        aList += getMD5OfFilesInDirectories(md5TestBaseDir, md5TestSubDir, ["E", "F"]);

        String text = aList.join(System.getProperty("line.separator"))
        def md5OfFirstTestDirectory = RoddyIOHelperMethods.getSingleMD5OfFilesInDirectory(md5TestDir)
        assert md5OfFirstTestDirectory == RoddyIOHelperMethods.getMD5OfText(text);

        // Replicate the test but move one file!
        File md5TestDir2 = RoddyIOHelperMethods.assembleLocalPath(testBaseDir, "testGetSingleMD5OfFilesInDirectory_b", "md5sumtest");
        File md5TestSubDir2 = new File(md5TestDir2, "sub");
        md5TestSubDir2.mkdirs();
        ["A", "B", "C", "D"].each { new File(md5TestDir2, it) << it }
        new File(md5TestSubDir2, "E") << "E"
        new File(md5TestSubDir2, "G") << "F"

        assert md5OfFirstTestDirectory != RoddyIOHelperMethods.getSingleMD5OfFilesInDirectory(md5TestDir2)

        // Replicate the test but don't move one file!
        File md5TestDir3 = RoddyIOHelperMethods.assembleLocalPath(testBaseDir, "testGetSingleMD5OfFilesInDirectory_c", "md5sumtest");
        File md5TestSubDir3 = new File(md5TestDir3, "sub");
        md5TestSubDir3.mkdirs();
        ["A", "B", "C", "D"].each { new File(md5TestDir3, it) << it }
        new File(md5TestSubDir3, "E") << "E"
        new File(md5TestSubDir3, "F") << "F"

        assert md5OfFirstTestDirectory == RoddyIOHelperMethods.getSingleMD5OfFilesInDirectory(md5TestDir3)
    }

    @Test
    public void testCopyDirectory() {

        File base = testBaseDirCopy
        File src = new File(base, "src");
        File dst = new File(base, "dst");
        File dst2 = new File(dst, "dst")

        String nonExecutableButWritable = "nonExecutableButWritable"
        String executableButNotWritable = "executableButNotWritable"

        src.mkdirs();

        File nebw = new File(src, nonExecutableButWritable)
        nebw << "a"
        nebw.setExecutable(false)
        nebw.setWritable(true)

        File exbnw = new File(src, executableButNotWritable)
        exbnw << "b"
        exbnw.setExecutable(true);
        exbnw.setWritable(false)

        assert !nebw.canExecute()
        assert nebw.canWrite()


        assert exbnw.canExecute()
        assert !exbnw.canWrite()

        // To non existing directory with new name
        RoddyIOHelperMethods.copyDirectory(src, dst)
        assert dst.exists()
        File nebw2 = new File(dst, nonExecutableButWritable)
        assert !nebw2.canExecute()
        assert nebw2.canWrite()

        File exbnw2 = new File(dst, executableButNotWritable)
        assert exbnw2.canExecute()
        assert !exbnw2.canWrite()

        // To existing directory without new name
        RoddyIOHelperMethods.copyDirectory(src, dst2)
        assert dst2.exists()
        File nebw3 = new File(dst2, nonExecutableButWritable)
        assert !nebw3.canExecute()
        assert nebw3.canWrite()

        File exbnw3 = new File(dst2, executableButNotWritable)
        assert exbnw3.canExecute()
        assert !exbnw3.canWrite()

    }

    @Test
    public void testSymbolicToNumericAccessRights() throws Exception {

        Map<String, String> valuesAndExpectedMap = [
                "u=rwx,g=rwx,o=rwx": "0777", //rwx,rwx,rwx
                "u=rwx,g=rwx,o-rwx": "0770", //rwx,rwx,---
                "u+rwx,g+rwx,o-rwx": "0770", //rwx,rwx,---
                "u+rw,g-rw,o-rwx"  : "0710", //rwx,---,---
                "u+rw,g+rw,o-rwx"  : "0770", //rwx,rw-,---
                "u+rw,g+rw"        : "0775", //rwx,rw-,r--
                "u-w,g+rw,u-r"     : "0175", //--x,rwx,r-x  Careful here, u ist set two times!
        ]

        valuesAndExpectedMap.each {
            String rights, String res ->
                assert res == RoddyIOHelperMethods.symbolicToNumericAccessRights(rights, 0022)
        }
    }

    @Test
    public void testConvertUMaskToAccessRights() throws Exception {
        Map<String, String> valuesAndResults = [
                "0000": "0777",
                "0007": "0770",
                "0067": "0710",
                "0002": "0775",
                "0602": "0175",
        ]

        valuesAndResults.each {
            String rights, String res ->
                assert res == RoddyIOHelperMethods.convertUMaskToAccessRights(rights);
        }
    }
}