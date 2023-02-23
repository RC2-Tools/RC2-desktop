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

package edu.uw.cse.ifrcdemo.upload.test;

public class VerificationTest {
	
    /* Tests are broken. Commenting them out while we make significant changes to the project.
	 * TODO: Fix and uncomment tests

	private VerificationModel model;
	private static final String TABLE_ID_1 = "registration";
	private static final String TABLE_ID_2 = "entitlements";
	private static final String TABLE_ID_3 = "authorizations";
	private static final String PREFIX = "src/test/resources/csv";
	private static final String INPUT_PATH_1 = "src/test/resources/csv/verification_input_1.csv";
	private static final String INPUT_PATH_2 = "src/test/resources/csv/verification_input_2.csv";
	private static final String INPUT_PATH_3 = "src/test/resources/csv/verification_input_3.csv";
	private static final String ADDRESS = "https://odk2redcross.appspot.com";
	private static final String APP_ID = "default";
	private static final String USERNAME = "orilevari";
	private static final String PASSWORD = "oriisawesome";
	private static final boolean UPLOAD = false;
	private String saveDir;
	
	@Before
	public void init() {
		Map<String, String> verMap = new HashMap<String, String>();
		verMap.put(TABLE_ID_1, INPUT_PATH_1);
		verMap.put(TABLE_ID_2, INPUT_PATH_2);
		verMap.put(TABLE_ID_3, INPUT_PATH_3);
		File path = new File("");
		System.out.println(path.getAbsolutePath() + "/" + PREFIX);
		this.model = new VerificationModel(verMap, path.getAbsolutePath() + "/" + PREFIX);
		model.addMetaDataColumns(TABLE_ID_1);
		model.removeDuplicates(TABLE_ID_1, "_id");
		model.addColumn(TABLE_ID_1, "operation", "update", 0);
		model.addMetaDataColumns(TABLE_ID_2);
		model.removeDuplicates(TABLE_ID_2, "_id");
		model.addColumn(TABLE_ID_2, "operation", "update", 0);
		model.addMetaDataColumns(TABLE_ID_3);
		model.removeDuplicates(TABLE_ID_3, "_id");
		model.addColumn(TABLE_ID_3, "operation", "update", 0);
		this.saveDir = model.writeToCSV();
	}
	
	
	@Test
	public void useCaseTest() {
		standardTests(6, TABLE_ID_1, INPUT_PATH_1);
		standardTests(9, TABLE_ID_2, INPUT_PATH_2);
		if (UPLOAD) {
			model.writeToScript(ADDRESS, APP_ID, USERNAME, PASSWORD);
		}
	}
	
	@Test
	public void duplicatedMetaTest() {
		try {
			CSVReader reader = new CSVReader(new FileReader
					(PREFIX + "/" + this.saveDir + "/" + TABLE_ID_3 + ".csv"));
			List<String[]> actualData = reader.readAll();
			reader = new CSVReader(new FileReader(PREFIX + "/verification_expected_3.csv"));
			List<String[]> expectedData = reader.readAll();
			reader.close();
			assertEquals(expectedData.size(), actualData.size());
			for (int i = 0; i < actualData.size(); i++) {
				String[] expectedRow = expectedData.get(i);
				String[] actualRow = actualData.get(i);
				assertEquals(expectedRow.length, actualRow.length);
				for (int j = 0; j < actualRow.length; j++) {
					if (!actualRow[j].equals(expectedRow[j])) {
						System.out.println(i + ", " + j);
						System.out.println(actualRow[j] + ", " + expectedRow[j]);
					}
					assertTrue(actualRow[j].equals(expectedRow[j]));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testMetaOrder() {
		checkMetaOrder(TABLE_ID_1);
		checkMetaOrder(TABLE_ID_2);
		checkMetaOrder(TABLE_ID_3);
	}
	
	public void checkMetaOrder(String tableId) {
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(PREFIX + "/" + this.saveDir + "/" + tableId + ".csv"));
			List<String[]> data = reader.readAll();
			String[] labels = data.get(0);
			assertTrue(labels != null);
			for (int i = 0; i < VerificationModel.HEAD_META_LENGTH; i++) {
				assertTrue(labels[i].equals(VerificationModel.META_ARRAY[i]));
			}
			for (int i = VerificationModel.META_ARRAY.length - VerificationModel.TAIL_META_LENGTH;
					i < VerificationModel.META_ARRAY.length; i++) {
				assertTrue(labels[labels.length + i - VerificationModel.META_ARRAY.length]
							.equals(VerificationModel.META_ARRAY[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void standardTests(int totalSize, String tableId, String inputPath) {
		try {
			CSVReader reader = new CSVReader(new FileReader(PREFIX + "/" + this.saveDir + "/" + tableId + ".csv"));
			List<String[]> data = reader.readAll();
			assertEquals(totalSize, data.size());
			int idIdx = findIndex("_id", data.get(0));
			assertTrue(idIdx != -1);
			Set<String> ids = new HashSet<String>();
			int dateIndex = findIndex("_savepoint_timestamp", data.get(0)); 
			assertTrue(dateIndex != -1);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
			for (int i = 1; i < data.size(); i++) {
				String[] row = data.get(i);
				ids.add(row[idIdx]);
				try {
					dateFormat.parse(row[dateIndex]);
				} catch (ParseException e) {
					fail();
				}
			}
			for (String id : ids) {
				assertEquals(1, query(data, "_id", id).size());
			}
			assertEquals(totalSize - 1, query(data, "_form_id", tableId).size());
			assertEquals(totalSize - 1, query(data, "_locale", "en_US").size());
			assertEquals(totalSize - 1,query(data, "_savepoint_type", "COMPLETE").size());
			assertEquals(totalSize - 1,query(data, "_savepoint_creator", "").size());
			assertEquals(totalSize - 1,query(data, "_row_etag", "").size());
			assertEquals(totalSize - 1,query(data, "_default_access", "").size());
			assertEquals(totalSize - 1,query(data, "_row_owner", "").size());
			assertEquals(totalSize - 1,query(data, "_group_read_only", "").size());
			assertEquals(totalSize - 1,query(data, "_group_modify", "").size());
			assertEquals(totalSize - 1,query(data, "_group_privileged", "").size());
			assertEquals(totalSize - 1,query(data, "operation", "force_update").size());
			reader = new CSVReader(new FileReader(inputPath));
			List<String[]> inputData = reader.readAll();
			assertRowInfoEqual(inputData, data);

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void assertRowInfoEqual(List<String[]> input, List<String[]> output) {
		String[] oldLabels = input.get(0);
		Map<Integer, Integer> indicies = new HashMap<Integer, Integer>();
		for (int i = 0; i < oldLabels.length; i++) {
			String label = oldLabels[i];
			if (!label.startsWith("_")) {
				int index = findIndex(label, output.get(0));
				assertTrue(index != -1);
				indicies.put(i, index);
			}
		}
		for (String[] row : output) {
			boolean found = false;
			for (String[] oldRow : input) {
				if (matching(indicies, oldRow, row)) {
					found = true;
				}
			}
			assertTrue(found);
		}
	}
	
	private boolean matching(Map<Integer, Integer> indicies, String[] input, String[] output) {
		for (Entry<Integer, Integer> entry : indicies.entrySet()){
			if (!input[entry.getKey()].equals(output[entry.getValue()])) {
				return false;
			}
		}
		return true;
	}
	
	private static List<String[]> query(List<String[]> data, String field, String value) {
		String[] labels = data.get(0);
		int fieldIndex = findIndex(field, labels);
		if (fieldIndex > -1) {
			List<String[]> results = new ArrayList<String[]>();
			for (String[] row : data) {
				if (row[fieldIndex].equals(value)) {
					String[] copy = new String[row.length];
					System.arraycopy(row, 0, copy, 0, row.length);
					results.add(copy);
				}
			}
			return results;
		} else {
			return null;
		}
	}
	
	private static int findIndex(String field, String[] currLine) {
		for (int i = 0; i < currLine.length; i++) {
			if (modifyString(currLine[i]).equals(field)) {
				return i;
			}
		}
		return -1;
	}
	
	public static String modifyString(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
*/

}
