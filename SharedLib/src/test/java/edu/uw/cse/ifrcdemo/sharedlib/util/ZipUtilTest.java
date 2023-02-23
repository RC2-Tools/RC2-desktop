/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.TestUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ZipUtilTest {
    private static final String TEST_ZIP_DIR = "TestZipDir";
    private static final String TEST_ZIP_OUTPUT_DIR = "TestZipOutputDir";
    private static final String TEST_ZIP_ZIP = "TestZip.zip";
    private static final String TEST_CSV_DIR = "csv";
    private static final String TEST_INPUT_DIR = "CsvTest";

    private File readOnlyDir;
    private File zipContDir;
    private File zipOutputDir;
    private File zipCheckLocation;

    private File zipFile;

    public ZipUtilTest() {
        String baseDir = System.getProperty("basedir");
        ClassLoader classLoader = this.getClass().getClassLoader();
        File testCsvDir = new File(classLoader.getResource(TEST_CSV_DIR).getPath());
        readOnlyDir = (testCsvDir.toPath()).resolve(TEST_INPUT_DIR).toFile();
        assertTrue(readOnlyDir.isDirectory());
        zipContDir = new File(baseDir, TEST_ZIP_DIR);
        zipOutputDir = new File(baseDir, TEST_ZIP_OUTPUT_DIR);
        zipCheckLocation = new File(baseDir, TEST_ZIP_OUTPUT_DIR + File.separator + TEST_ZIP_DIR);
        zipFile = new File(baseDir, TEST_ZIP_ZIP);
    }

    private void helperVerifyDirectoryAreEqual(File original, File check) throws IOException {
        assertTrue(original.isDirectory() && check.isDirectory());

        File[] children = original.listFiles();
        for (File childFile : children) {
            long checkSumOrig = FileUtils.checksumCRC32(childFile);
            File checkFileChild = new File(check.getAbsolutePath() + File.separator + childFile.getName());
            assertTrue(checkFileChild.exists());
            long checkSumCheck = FileUtils.checksumCRC32(checkFileChild);
            assertEquals(checkSumOrig, checkSumCheck);
        }

    }

    @BeforeEach
    void verifyProperlySetup() throws IOException {
        assertTrue(readOnlyDir.exists());
        if (zipContDir.exists()) {
            cleanUpZipDirectory();
            fail("Previous test did not properly clean up test directory");
        }
        if (zipFile.exists()) {
            cleanUpZipFile();
            fail("Previous test did not properly clean up test zip");
        }

        if (zipOutputDir.exists()) {
            cleanUpZipOutputDirectory();
            fail("Previous test did not properly clean up zip output directory");
        }

    }

    @AfterEach
    void cleanUpZipDirectory() {
        try {
            if (zipContDir.exists())
                FileUtils.deleteDirectory(zipContDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertFalse(zipContDir.exists());
    }

    @AfterEach
    void cleanUpZipOutputDirectory() {
        try {
            if (zipOutputDir.exists())
                FileUtils.deleteDirectory(zipOutputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertFalse(zipOutputDir.exists());
    }

    @AfterEach
    void cleanUpZipFile() {
        if (zipFile.exists())
            zipFile.delete();
        assertFalse(zipFile.exists());
    }

    @Test
    void createZipFileFromDirectory() {
        try {
            zipContDir.mkdirs();
            FileUtils.copyDirectory(readOnlyDir, zipContDir);
            assertTrue(zipContDir.exists());
            ZipUtil.zipDirectory(zipContDir.toPath(), zipFile.toPath());

            ZipUtil.extractZip(zipFile.toPath(), zipOutputDir.toPath());
            helperVerifyDirectoryAreEqual(readOnlyDir, zipCheckLocation);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception Thrown");

        }

    }

}
