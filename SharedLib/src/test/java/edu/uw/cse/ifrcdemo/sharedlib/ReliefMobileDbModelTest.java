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

package edu.uw.cse.ifrcdemo.sharedlib;

import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.IndividualStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.ReliefMobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static edu.uw.cse.ifrcdemo.sharedlib.TestUtil.assertActualFilesMatchExpected;
import static edu.uw.cse.ifrcdemo.sharedlib.TestUtil.copyTestDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ReliefMobileDbModelTest {
    // Path to default test files
    private static final String CSV_ROOT = "csv/";
    private static final String MOBILE_DB_MODEL_TEST_PATH = CSV_ROOT + "/MobileDbModelTest/";

    // Path to test resources
    private static final String INPUT_FILES = "/inputs/";
    private static final String EXPECTED_OUTCOME_FILES = "/expected/";

    // Paths to specific test case resources
    private static final String INITIALIZATION_TEST_SOME_TABLES_EMPTY = MOBILE_DB_MODEL_TEST_PATH + "/TestInitializationSomeTablesEmpty/";
    private static final String INITIALIZATION_TEST_ALL_TABLES_EMPTY = MOBILE_DB_MODEL_TEST_PATH + "/TestInitializationAllTablesEmpty/";
    private static final String INITIALIZATION_TEST_SOME_TABLES_EMPTY_WITH_METADATA = MOBILE_DB_MODEL_TEST_PATH + "/TestInitializationSomeTablesEmptyWithMetadata/";
    private static final String INITIALIZATION_TEST_SOME_TABLES_WITH_DATA = MOBILE_DB_MODEL_TEST_PATH + "/TestInitializationSomeTablesWithData/";
    private static final String INITIALIZATION_TEST_SOME_TABLES_WITH_DATA_WITH_METADATA = MOBILE_DB_MODEL_TEST_PATH + "/TestInitializationSomeTablesWithDataWithMetadata/";

    private static final String WRITE_TEST_NO_CHANGES_ROOT = MOBILE_DB_MODEL_TEST_PATH + "/TestWriteNoChanges/";
    private static final String[] WRITE_TEST_NO_CHANGES_FILES = {
            FileUtil.getFileName(CsvBeneficiaryEntity.class),
            FileUtil.getFileName(CsvIndividual.class)};

    private static final String WRITE_TEST_NEW_ROWS_ROOT = MOBILE_DB_MODEL_TEST_PATH + "/TestWriteNewRows/";
    private static final String[] WRITE_TEST_NEW_ROWS_FILES = {
            FileUtil.getFileName(CsvBeneficiaryEntity.class),
            FileUtil.getFileName(CsvIndividual.class)};

    private static final String WRITE_TEST_UPDATE_ROWS_ROOT = MOBILE_DB_MODEL_TEST_PATH + "/TestWriteUpdateRows/";
    private static final String[] WRITE_TEST_UPDATE_ROWS_FILES = {
            FileUtil.getFileName(CsvBeneficiaryEntity.class),
            FileUtil.getFileName(CsvIndividual.class)};

    private static final String WRITE_TEST_NEW_TABLE_ROOT = MOBILE_DB_MODEL_TEST_PATH + "/TestWriteNewTables/";
    private static final String[] WRITE_TEST_NEW_TABLE_FILES = {
            FileUtil.getFileName(CsvBeneficiaryEntity.class),
            FileUtil.getFileName(CsvIndividual.class),
            FileUtil.getFileName(CsvAuthorization.class)};

    private static final String VALIDATION_TEST_NO_CONFLICTS = MOBILE_DB_MODEL_TEST_PATH + "/TestValidateNoConflicts/";
    private static final String VALIDATION_TEST_METADATA_CONFLICTS = MOBILE_DB_MODEL_TEST_PATH + "/TestValidateMetadataConflicts/";
    private static final String VALIDATION_TEST_BENEFICIARY_ENTITY_CONFLICTS_ROOT = MOBILE_DB_MODEL_TEST_PATH + "/TestValidateBeneficiaryEntityConflicts/";
    private static final String[] VALIDATION_TEST_BENEFICIARY_ENTITY_CONFLICTS_FILES = {
            FileUtil.getFileName(CsvBeneficiaryEntity.class),
    };
    private static final String VALIDATION_TEST_MULTIPLE_CONFLICTS = MOBILE_DB_MODEL_TEST_PATH + "/TestValidateMultipleConflictKeys/";

    private String basePath;

    /**
     * Before each test clean and create the test directory
     */
    @BeforeEach
    void setUpTest() {
        basePath = FileUtil.getDefaultSavePath().toAbsolutePath().toString();

        try {
            FileUtils.deleteDirectory(new File(basePath));
            Files.createDirectories(Paths.get(basePath));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create test data directory");
        }
    }


    /**
     * After each test remove the previous tests files and delete the test directory
     */
    @AfterEach
    void tearDownTest() {
        try {
            FileUtils.deleteDirectory(new File(basePath));
        } catch (IOException e) {
            fail("Failed to clean up and delete test data directory");
        }
    }

    /**
     * Test initialization on an empty directory
     */
    @Test
    void testDataModelInitializationEmptyDir() {

        assertThrows(IllegalArgumentException.class,
                () -> new ReliefMobileDbModel(Paths.get(basePath), ReliefMobileDbModel.tableDefinition),
                "Empty directory should fail to initialize"
        );
    }

    /**
     * Test initialization with empty tables
     */
    @ParameterizedTest
    @ValueSource(strings = { INITIALIZATION_TEST_SOME_TABLES_EMPTY, INITIALIZATION_TEST_ALL_TABLES_EMPTY,
            INITIALIZATION_TEST_SOME_TABLES_EMPTY_WITH_METADATA})
    void testDataModelInitializationEmpty(String testPath) {
        copyTestDir(basePath, testPath);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");
    }

    /**
     * Test initialization with data
     */
    @ParameterizedTest
    @ValueSource(strings = { INITIALIZATION_TEST_SOME_TABLES_WITH_DATA,
            INITIALIZATION_TEST_SOME_TABLES_WITH_DATA_WITH_METADATA})
    void testDataModelInitializationWithData(String testPath) {
        copyTestDir(basePath, testPath);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        assertNotNull(model, "New model shouldn't be null");

        List<CsvBeneficiaryEntity> beneficiaryEntityTable = model.getTable(CsvBeneficiaryEntity.class);
        assertEquals(4, beneficiaryEntityTable.size(), "beneficiary_entity table created with incorrect size");

        List<CsvIndividual> individualTable = model.getTable(CsvIndividual.class);
        assertEquals(5, individualTable.size(), "Individual table created with incorrect size");

        List<CsvEntitlement> entitlementTable = model.getTable(CsvEntitlement.class);
        assertNull(entitlementTable, "Non existent entitlements table should be null");
    }

    /**
     * Test writing back out to disk with no changes
     */
    @Test
    void testDataModelFlushToDiskNoChanges() {
        copyTestDir(basePath, WRITE_TEST_NO_CHANGES_ROOT + INPUT_FILES);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        try {
            model.flushToDisk();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to write tables to disk");
        }

        assertActualFilesMatchExpected(WRITE_TEST_NO_CHANGES_ROOT + EXPECTED_OUTCOME_FILES, basePath, WRITE_TEST_NO_CHANGES_FILES);
    }

    /**
     * Test writing back out to disk with changes
     */
    @Test
    void testDataModelFlushToDiskNewRows() {
        copyTestDir(basePath, WRITE_TEST_NEW_ROWS_ROOT + INPUT_FILES);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }

        CsvBeneficiaryEntity newRow1 = new CsvBeneficiaryEntity();
        newRow1.setBeneficiaryEntityId("5");
        newRow1.setStatus(BeneficiaryEntityStatus.ENABLED);

        CsvBeneficiaryEntity newRow2 = new CsvBeneficiaryEntity();
        newRow2.setBeneficiaryEntityId("6");
        newRow2.setStatus(BeneficiaryEntityStatus.ENABLED);

        List<CsvBeneficiaryEntity> beneficiaryEntitiesTable = model.getTable(CsvBeneficiaryEntity.class);
        beneficiaryEntitiesTable.add(newRow1);
        beneficiaryEntitiesTable.add(newRow2);

        model.addTable(CsvBeneficiaryEntity.class, beneficiaryEntitiesTable);

        try {
            model.flushToDisk();
        } catch (IOException e) {
            fail("Failed to write tables to disk");
        }

        assertActualFilesMatchExpected(WRITE_TEST_NEW_ROWS_ROOT + EXPECTED_OUTCOME_FILES, basePath, WRITE_TEST_NEW_ROWS_FILES);
    }


    /**
     * Test writing back out to disk with updated rows
     */
    @Test
    void testDataModelFlushToDiskUpdateRows() {
        copyTestDir(basePath, WRITE_TEST_UPDATE_ROWS_ROOT + INPUT_FILES);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        List<CsvIndividual> individualTable = model.getTable(CsvIndividual.class);
        for (CsvIndividual row : individualTable) {
            if (row.getStatus().equals(IndividualStatus.ENABLED)) {
                row.setStatus(IndividualStatus.DISABLED);
                row.setStatusReason("Test");
            }
        }

        try {
            model.flushToDisk();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to write tables to disk");
        }

        assertActualFilesMatchExpected(WRITE_TEST_UPDATE_ROWS_ROOT + EXPECTED_OUTCOME_FILES, basePath, WRITE_TEST_UPDATE_ROWS_FILES);
    }

    /**
     * Test writing back out to disk with new tables
     */
    @Test
    void testDataModelFlushToDiskNewTable() {
        copyTestDir(basePath, WRITE_TEST_NEW_TABLE_ROOT + INPUT_FILES);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        List<CsvAuthorization> authorizationTable = new LinkedList<>();

        CsvAuthorization headerRow = new CsvAuthorization();

        CsvAuthorization row1 = new CsvAuthorization();
        row1.setRowId("1");
        row1.setType(AuthorizationType.REQUIRED_REGISTRATION);
        row1.setItemId("1234");
        row1.setStatus(AuthorizationStatus.ACTIVE);
        row1.setExtraFieldEntitlements(ExtraFieldEntitlements.NONE);
        authorizationTable.add(row1);

        CsvAuthorization row2 = new CsvAuthorization();
        row2.setRowId("2");
        row2.setType(AuthorizationType.OPTIONAL_REGISTRATION);
        row2.setItemId("4321");
        row2.setStatus(AuthorizationStatus.ACTIVE);
        row2.setExtraFieldEntitlements(ExtraFieldEntitlements.NONE);
        authorizationTable.add(row2);

        model.addTable(CsvAuthorization.class, authorizationTable);

        try {
            model.flushToDisk();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to write tables to disk");
        }

        assertActualFilesMatchExpected(WRITE_TEST_NEW_TABLE_ROOT + EXPECTED_OUTCOME_FILES, basePath, WRITE_TEST_NEW_TABLE_FILES);
    }

    /**
     * Test validation when there are no conflict rows
     */
    @Test
    void testDataModelValidateNoConflicts() {
        copyTestDir(basePath, VALIDATION_TEST_NO_CONFLICTS);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        Map<String, List<CsvAuthorization>> authorizationConflicts = model.getTableConflicts(CsvAuthorization.class, CsvAuthorization::getRowId);
        assertEquals(0, authorizationConflicts.size(), "authorization conflict list should be empty");

        Map<String, List<CsvBeneficiaryEntity>> benficiaryEntityConflicts = model.getTableConflicts(CsvBeneficiaryEntity.class, CsvBeneficiaryEntity::getBeneficiaryEntityId);
        assertEquals(0, authorizationConflicts.size(), "beneficiary_entity conflict list should be empty");

        Map<String, List<CsvIndividual>> individualConflicts = model.getTableConflicts(CsvIndividual.class, CsvIndividual::getMemberId);
        assertEquals(0, authorizationConflicts.size(), "individual conflict list should be empty");
    }

    /**
     * Test validation when there are metadata conflict rows
     */
    @Test
    void testDataModelValidateMetadataConflicts() {
        copyTestDir(basePath, VALIDATION_TEST_METADATA_CONFLICTS);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        Map<String, List<CsvAuthorization>> authorizationConflicts = model.getTableConflicts(CsvAuthorization.class, CsvAuthorization::getRowId);
        assertEquals(1, authorizationConflicts.size(), "authorization conflict list should have 1 entry");

        List<CsvAuthorization> row1Conflicts = authorizationConflicts.get("1");
        assertNotNull(row1Conflicts, "Expected conflicts on authorization id 1");
        assertEquals(2, row1Conflicts.size(), "Expected 2 conflict rows on authorization id 1");
    }


    /**
     * Test validation when there are table level conflict rows
     */
    @Test
    void testDataModelValidateBeneficiaryEntityConflicts() {
        copyTestDir(basePath, VALIDATION_TEST_BENEFICIARY_ENTITY_CONFLICTS_ROOT + INPUT_FILES);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");


        Map<String, List<CsvBeneficiaryEntity>> benficiaryEntityConflicts = model.getTableConflicts(CsvBeneficiaryEntity.class, CsvBeneficiaryEntity::getBeneficiaryEntityId);
        assertEquals(1, benficiaryEntityConflicts.size(), "beneficiary_entity conflict list should have 1 conflict");

        List<CsvBeneficiaryEntity> row1Conflicts = benficiaryEntityConflicts.get("2");
        assertNotNull(row1Conflicts, "Expected conflicts on beneficiary_entity_id 2");
        assertEquals(2, row1Conflicts.size(), "Expected 2 conflict rows on beneficiary_entity_id 2");

        for (CsvBeneficiaryEntity row : row1Conflicts) {
            if (row.getRowId().equals("3")) {
                row.setStatus(BeneficiaryEntityStatus.DISABLED);
                row.setStatusReason("Duplicate");
            }
        }

        try {
            model.flushToDisk();
        } catch(IOException e) {
            e.printStackTrace();
            fail("Failed to write conflict updates to disk");
        }

        assertActualFilesMatchExpected(VALIDATION_TEST_BENEFICIARY_ENTITY_CONFLICTS_ROOT + EXPECTED_OUTCOME_FILES, basePath, VALIDATION_TEST_BENEFICIARY_ENTITY_CONFLICTS_FILES);
    }

    /**
     * Test validation when there are multiple keys that are conflicting with multiple rows
     */
    @Test
    void testDataModelValidateMultipleConflicts() {
        copyTestDir(basePath, VALIDATION_TEST_MULTIPLE_CONFLICTS);

        ReliefMobileDbModel model = null;
        try {
            model = new ReliefMobileDbModel(Paths.get(basePath), new LinkedList<>());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to create model");
        }
        assertNotNull(model, "Newly created model should not be null");

        Map<String, List<CsvAuthorization>> authorizationConflicts = model.getTableConflicts(CsvAuthorization.class, CsvAuthorization::getRowId);
        assertEquals(3, authorizationConflicts.size(), "authorization conflict list should have 3 conflict keys");

        List<CsvAuthorization> key2Conflicts = authorizationConflicts.get("2");
        assertNotNull(key2Conflicts, "Expected conflicts on authorization id 2");
        assertEquals(4, key2Conflicts.size(), "Expected 4 conflict rows on authorization id 2");

        List<CsvAuthorization> key3Conflicts = authorizationConflicts.get("3");
        assertNotNull(key3Conflicts, "Expected conflicts on authorization id 3");
        assertEquals(3, key3Conflicts.size(), "Expected 4 conflict rows on authorization id 3");

        List<CsvAuthorization> key4Conflicts = authorizationConflicts.get("4");
        assertNotNull(key4Conflicts, "Expected conflicts on authorization id 4");
        assertEquals(2, key4Conflicts.size(), "Expected 4 conflict rows on authorization id 4");
    }


}
