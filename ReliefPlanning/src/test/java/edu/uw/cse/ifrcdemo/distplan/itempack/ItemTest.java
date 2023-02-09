/*
 * Copyright (c) 2016-2022 University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the University of Washington nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.uw.cse.ifrcdemo.distplan.itempack;

import edu.uw.cse.ifrcdemo.distplan.ReliefAppSystem;
import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.InternalFileStoreUtil;
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

public class ItemTest {
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
    ReliefAppSystem.systemInit();
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
    Item item1 = new Item();
    Item item2 = new Item();

    item1.setRowId(UUID_12345);
    item1.setName(TEST_ITEM);

    item2.setRowId(UUID_12345);
    item2.setName(TEST_ITEM);

   try {
    ItemRepository itemRepository = ReliefDataInstance
        .getDataRepos()
        .getItemRepository();

        item1 = itemRepository
            .saveItem(item1)
            .join();

       item2 = itemRepository
                .saveItem(item2)
                .join();
    } catch (Exception e) {
        fail("Should be same item", e);
    }

    assertEquals(item1.getId(), item2.getId());

  }
}
