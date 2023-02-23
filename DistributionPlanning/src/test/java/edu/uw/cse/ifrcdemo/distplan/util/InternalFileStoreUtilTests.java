package edu.uw.cse.ifrcdemo.distplan.util;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InternalFileStoreUtilTests {


    public static final String UNITTESTS_PROFILE = "unittests";

    public static Path getUnitTestProfilePath() throws IOException {
        return InternalFileStoreUtil.getProjectPath().resolve(InternalFileStoreUtil.PROFILES_PATH).resolve(UNITTESTS_PROFILE);
    }

    @BeforeEach
    void verifyProperlySetup() {
        File testOutputDir = null;
        try {
            testOutputDir = getUnitTestProfilePath().toFile();
            if (testOutputDir.exists()) {
                cleanOutputDirectory();
                fail("Previous test did not properly clean up test directory");
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem with test file");
        }
    }

    @AfterEach
    void cleanOutputDirectory() {
        try {
            InternalFileStoreUtil.setNoProfile();
        } catch (Exception e) {
            e.printStackTrace();
            fail("problem with test file", e);
        }

        File testOutputDir = null;
        try {
            testOutputDir = getUnitTestProfilePath().toFile();

            if (testOutputDir.exists()) {
                FileUtils.deleteDirectory(testOutputDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem with test file");
        } finally {
            try {
                testOutputDir = getUnitTestProfilePath().toFile();
                if (testOutputDir != null) {
                    assertFalse(testOutputDir.exists());
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Test
    void basicNothingTest() {
        assertTrue(true);
    }

    @Test
    void basicImportFilesTest() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File inputDirectory = new File(classLoader.getResource("testFiles/sample_output").getPath());
        assertTrue(inputDirectory.exists());
        CompletableFuture<Void> importFuture = null;
        try {
            importFuture = InternalFileStoreUtil.importData(UNITTESTS_PROFILE, inputDirectory);
            importFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during import", e);
        }

    }
}
