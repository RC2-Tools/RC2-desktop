package edu.uw.cse.ifrcdemo.distplan.itempack;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ItemPackTest {
  private static final String UNITTESTS_PROFILE = "rcunittests";
  private static final String SETUP_FILES_PATH = "testFiles/templatetest/setupfiles";
  public static final String UUID_12345 = "uuid-12345";
  public static final String TEST_ITEM = "Test Item";

  private static Path getUnitTestProfilePath() throws IOException {
    return InternalFileStoreUtil.getProjectPath()
        .resolve(InternalFileStoreUtil.PROFILES_PATH)
        .resolve(UNITTESTS_PROFILE);
  }

  @BeforeEach
  void verifyProperlySetup() throws IOException, IllegalAccessException {
    File testOutputDir = getUnitTestProfilePath().toFile();
    if (testOutputDir.exists()) {
      cleanOutputDirectory();
      fail("Previous test did not properly clean up test directory");
    }

    ClassLoader classLoader = this.getClass().getClassLoader();
    File inputDirectory = new File(classLoader.getResource(SETUP_FILES_PATH).getPath());
    assertTrue(inputDirectory.exists());

    InternalFileStoreUtil
        .importData(UNITTESTS_PROFILE, inputDirectory)
        .join();
  }

  @AfterEach
  void cleanOutputDirectory() throws IOException, IllegalAccessException {
    InternalFileStoreUtil.setNoProfile();

    File testOutputDir;
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
  void duplicateRowIdTest() {
    ItemPack itemPack1 = new ItemPack();
    ItemPack itemPack2 = new ItemPack();

    itemPack1.setRowId(UUID_12345);
    itemPack1.setName(TEST_ITEM);

    itemPack2.setRowId(UUID_12345);
    itemPack2.setName(TEST_ITEM);

   try {
    ItemPackRepository itemPackRepository = DataInstance
        .getDataRepos()
        .getItemPackRepository();

        itemPack1 = itemPackRepository
            .saveItemPack(itemPack1)
            .join();

       itemPack2 = itemPackRepository
                .saveItemPack(itemPack2)
                .join();
    } catch (Exception e) {
        fail("Should be same item", e);
    }

    assertEquals(itemPack1.getId(), itemPack2.getId());

  }
}
