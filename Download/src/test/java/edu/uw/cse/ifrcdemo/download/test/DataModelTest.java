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

package edu.uw.cse.ifrcdemo.download.test;

import java.nio.file.Path;

public class DataModelTest {

    // TODO: Add back tests when migration to shared lib is done

    // Path to default test files
    private static final String CSV_ROOT = "/csv";
    private static final String DEFAULT_TEST_PATH =  CSV_ROOT + "/default/";

    // Path to test resources
    private static final String INPUT_FILES = "/inputs/";
    private static final String EXPECTED_OUTCOME_FILES = "/expected/";
    private static final String CSV_SUFFIX = ".csv";

    // Paths to specific test case resources
    private static final String REGISTRATION_TEST_EMPTY = CSV_ROOT + "/registration_empty";
    private static final String REGISTRATION_TEST_NO_COLLISIONS = CSV_ROOT + "/registration_no_collisions";
    private static final String REGISTRATION_TEST_ONE_COLLISION = CSV_ROOT + "/registration_one_collision";
    private static final String REGISTRATION_TEST_MANY_COLLISIONS = CSV_ROOT + "/registration_many_collisions";

	private Path basePath;

    /**
     * Before each test create the test directory and copy the default test download files
     * into the test data directory
     *
    @BeforeEach
	void setUpTest() {
        basePath = FileUtil.getDefaultSavePath();
        String basePathString = basePath.toAbsolutePath().toString();
        try {
            Files.deleteIfExists(Paths.get(basePathString, TableConsts.DEFAULT_APP_ID));
            Files.createDirectories(basePath);
        } catch (IOException e) {
            fail("Failed to create test data directory");
        }

		// Copy in the default test files (these may be overwritten for specific tests)
		copyTestTable(DEFAULT_TEST_PATH + TableConsts.REGISTRATION_TABLE_NAME + CSV_SUFFIX,
                TableConsts.REGISTRATION_TABLE_NAME);
        copyTestTable(DEFAULT_TEST_PATH + TableConsts.ENTITLEMENTS_TABLE_NAME + CSV_SUFFIX,
                TableConsts.ENTITLEMENTS_TABLE_NAME);
        copyTestTable(DEFAULT_TEST_PATH + TableConsts.REGISTRATION_MEMBER_TABLE_NAME + CSV_SUFFIX,
                TableConsts.REGISTRATION_MEMBER_TABLE_NAME);
	}


    /**
     * After each test remove the previous tests files and delete the test directory
     *
    @AfterEach
	void tearDownTest() {
        try {
            FileUtils.cleanDirectory(new File(basePath.toAbsolutePath().toString()));
            Files.deleteIfExists(basePath);
        } catch (IOException e) {
            fail("Failed to clean up and delete test data directory");
        }
	}

	/**
	 * Test initialization with an empty test directory
	 *
	@Test
	void testDataModelInitializationNoFiles() {

		// Clean out the test directory
		try {
			FileUtils.cleanDirectory(new File(basePath.toAbsolutePath().toString()));
		} catch (IOException e) {
			fail("Failed to clean the test directory");
		}

		// Initialize the data model but expect an exception

        assertThrows(IOException.class,
                () -> new DataModel(basePath)
        );
	}

	/**
	 * Test initialization with a valid test directory
	 *
	@Test
	void testDataModelInitializationValidFiles() {
		DataModel model = initializeModel(basePath);

		assertNotNull(model, "Model failed to initialize");
	}

    /**
     * Test validation on the registration test files
     *
    @ParameterizedTest
    @ValueSource(strings = { REGISTRATION_TEST_EMPTY, REGISTRATION_TEST_NO_COLLISIONS,
            REGISTRATION_TEST_ONE_COLLISION, REGISTRATION_TEST_MANY_COLLISIONS })
    void testValidationRegistrations(String testPath) {

        copyTestTable(testPath + INPUT_FILES + TableConsts.REGISTRATION_TABLE_NAME + CSV_SUFFIX,
                TableConsts.REGISTRATION_TABLE_NAME);

        DataModel model = initializeModel(basePath);
        assertNotNull(model, "Model failed to initialize");
        try {
            // Run validation
            model.resolveHouseholdBeneficiaryDuplicates();
        } catch (IOException e) {
            fail("Failed to run validation");
        }


        // Read in the results
        List<String[]> registrationOutcomeData = null;
        List<String[]> registrationExpectedData = null;
        try {
            registrationOutcomeData =
                    readCSV(FileUtil.getPathToCSV(basePath, TableConsts.REGISTRATION_TABLE_NAME).
                            toAbsolutePath().toString());
            registrationExpectedData =
                    readCSV(Paths.get(getClass().getResource(
                            testPath + EXPECTED_OUTCOME_FILES + TableConsts.REGISTRATION_TABLE_NAME + CSV_SUFFIX).
                            toURI()).toAbsolutePath().toString());
        } catch (URISyntaxException e) {
            fail("Failed to find the test table csv");
        }

        assertNotNull(registrationOutcomeData, "Failed to read outcome data");
        assertNotNull(registrationExpectedData, "Failed to read expected data");

        // Compare the results to expectations
        compareExpectedVsOutcomeCSVs(registrationExpectedData, registrationOutcomeData);
    }

    /**
     * Copy a test file into the test directory
     *
     * @param resourceName The name of the resource in the jar
     * @param tableName The name of the data table the test file represents
     *
    private void copyTestTable(String resourceName, String tableName) {
        try {
            // Copy the test empty registration table to the default directory
            Files.copy(Paths.get(getClass().getResource(resourceName).toURI()),
                    FileUtil.getPathToCSV(basePath, tableName), REPLACE_EXISTING);
        } catch (URISyntaxException e) {
            fail("Failed to find the test table csv");
        } catch (IOException e) {
            fail("Failed to copy the test table csv");
        }
    }

    /**
     * Read a CSV file from disk
     * @param pathToCSV path to the file to read
     * @return Representation of the CSV table
     *
    private List<String[]> readCSV(String pathToCSV) {
        try {
            // Create models for the data
            CSVReader csvReader = new CSVReader(new FileReader(pathToCSV));
            List<String[]> csvData = csvReader.readAll();
            csvReader.close();

            return csvData;
        } catch (IOException e) {
            fail("Failed to read");
        }

        return null; // This should be unreachable
    }

    /**
     * Iterate through each item in the expected CSV result vs the actual outcome CSV result
     * and ensure they match
     *
     * @param expected CSV of the desired outcome
     * @param outcome CSV of the actual outcome
     *
    private void compareExpectedVsOutcomeCSVs(List<String[]> expected, List<String[]> outcome) {

        assertEquals(expected.size(), outcome.size(), "Expected and outcome CSV sizes did not match");

        for (int i = 0; i < expected.size(); i++) {
            String[] expectedRow = expected.get(i);
            String[] outcomeRow = outcome.get(i);

            assertEquals(expectedRow.length, outcomeRow.length, "Expected and outcome CSV rows did not match");

            for (int j = 0; j < expectedRow.length; j++) {
                assertEquals(expectedRow[j], outcomeRow[j], "Expected and outcome CSV cells did not match");
            }
        }

    }

    /**
     * Initialize the DataModel object
     *
     * @param basePath The test directory
     * @return The initialized DataModel
     *
    private DataModel initializeModel(Path basePath) {
        try {
            // Initialize data model
            return new DataModel(basePath);
        } catch (IOException e) {
            fail("Failed to initialize Data Model");
        }
        return null; // This should be unreachable
    }
    */

}
