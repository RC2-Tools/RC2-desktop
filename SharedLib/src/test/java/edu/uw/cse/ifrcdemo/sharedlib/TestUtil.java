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

package edu.uw.cse.ifrcdemo.sharedlib;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtil {
  public static URL getResource(String name) {
    return TestUtil.class.getClassLoader().getResource(name);
  }

  public static InputStream getResourceAsStream(String name) {
    return TestUtil.class.getClassLoader().getResourceAsStream(name);
  }

  /**
   * Copy a test resource directory into the test directory
   *
   * @param testPath The name of the resource in the jar
   */
  public static void copyTestDir(String basePath, String testPath) {
    try {
      FileUtils.copyDirectory(new File(getResource(testPath).toURI()), new File(basePath));
    } catch (URISyntaxException e) {
      fail("Failed to find the test table csv");
    } catch (IOException e) {
      fail("Failed to copy the test table csv");
    }
  }

  /**
   * Compare the full contents of the expected folder with the actual folder
   *
   * @param expectedBasePath The root path of the expected files directory
   * @param actualBasePath The root path of the actual file directory
   * @param files The names of the files to compare
   */
  public static void assertActualFilesMatchExpected(String expectedBasePath, String actualBasePath, String[] files) {
    CsvMapper mapper = new CsvMapper().enable(CsvParser.Feature.WRAP_AS_ARRAY);

    for (int i = 0; i < files.length; i++) {
      List<String[]> expectedCSV = readCSV(mapper, getResource(expectedBasePath + "/" + files[i]));
      assertNotNull(expectedCSV, "Failed to read expected CSV: " + expectedBasePath + "/" + files[i]);
      List<String[]> actualCSV = readCSV(mapper, new File(actualBasePath + "/" + files[i]));
      assertNotNull(actualCSV, "Failed to read actual CSV: " + actualBasePath + "/" + files[i]);
      assertEquals(expectedCSV.size(), actualCSV.size(), "CSV row lengths do not match: " + files[i]);

      Map<String, Integer> expectedColumnMap = getColumnMap(expectedCSV);
      assertNotNull(expectedColumnMap, "Failed to create expected CSV column map: " + expectedBasePath + "/" + files[i]);
      Map<String, Integer> actualColumnMap = getColumnMap(actualCSV);
      assertNotNull(actualColumnMap, "Failed to create actual CSV: " + actualBasePath + files[i]);
      if (expectedColumnMap.size() != actualColumnMap.size()) {
        System.out.println();
      }
      assertEquals(expectedColumnMap.size(), actualColumnMap.size(), "CSV column numbers do not match: " + "/" + files[i]);

      assertCSVsEqual(files[i], expectedColumnMap, expectedCSV, actualColumnMap, actualCSV);
    }

  }

  /**
   * Read a CSV file from a URL
   * @param csvFile URL to the file to read
   * @return Representation of the CSV table
   */
  public static List<String[]> readCSV(CsvMapper mapper, URL csvFile) {

    List<String[]> csv = null;
    try {
      MappingIterator<String[]> iterator = mapper.readerFor(String[].class).readValues(csvFile);
      csv = iterator.readAll();
    } catch (IOException e) {
      fail("Failed to read CSV file from disk: " + csvFile);
    }
    return csv;
  }

  /**
   * Read a CSV file from disk
   * @param csvFile path to the file to read
   * @return Representation of the CSV table
   */
  public static List<String[]> readCSV(CsvMapper mapper, File csvFile) {

    List<String[]> csv = null;
    try {
      MappingIterator<String[]> iterator = mapper.readerFor(String[].class).readValues(csvFile);
      csv = iterator.readAll();
    } catch (IOException e) {
      fail("Failed to read CSV file from disk: " + csvFile);
    }
    return csv;
  }

  public static Map<String, Integer> getColumnMap(List<String[]> csv) {
    Map<String, Integer> columnMap = new HashMap<>();

    String[] headerRow = csv.get(0);
    for (int i = 0; i < headerRow.length; i++) {
      columnMap.put(headerRow[i], i);
    }

    return columnMap;
  }

  /**
   * Compare two CSVs for equality (ignoring column order). This function assumes you've already validated that the csv
   * sizes are identical and non-null.
   *
   * @param expectedColumnMap Map of column name to row number in the expected CSV
   * @param expectedCSV List of rows in the expected CSV
   * @param actualColumnMap Map of column name to row number in the actual CSV
   * @param actualCSV List of rows in the actual CSV
   * @return Whether the two match
   */
  public static void assertCSVsEqual(String fileName, Map<String, Integer> expectedColumnMap, List<String[]> expectedCSV, Map<String, Integer> actualColumnMap, List<String[]> actualCSV) {
    // Iterate though the CSV, but skip the first row since its the column names
    for (int i = 1; i < expectedCSV.size(); i++) {

      String[] expectedRow = expectedCSV.get(i);
      String[] actualRow = actualCSV.get(i);

      // Iterate through each column and compare their values
      for (String column : expectedColumnMap.keySet()) {
        Integer expectedColIndex = expectedColumnMap.get(column);
        Integer actualColIndex = actualColumnMap.get(column);

        assertNotNull(expectedColIndex, "Could not find index for " + column + " in expectedColumnMap");
        assertNotNull(actualColIndex, "Could not find index for " + column + " in actualColIndex");

        assertEquals(
            expectedRow[expectedColIndex],
            actualRow[actualColIndex],
            "CSV cell mismatch in file: " + fileName + "\n\tcolumn: " + column + "\n\trow: " + i
        );
      }
    }
  }
}
