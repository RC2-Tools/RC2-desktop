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

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDistribution;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisit;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvVisitProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.AUTHORIZATION_REPORT_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.AUTHORIZATION_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.DELIVERY_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.DISTRIBUTION_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.ENTITLEMENT_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.VISIT_PROGRAM_TABLE_NAME;
import static edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts.TableNames.VISIT_TABLE_NAME;

public class FileUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";

    private static final String DEFAULT_SAVE_PATH = "Downloaded_Data";
    private static final String CONFIG_FILENAME = "config";
    private static final String OUTPUT_FILENAME_TXT = "_output_";


    /**
     * Resolves a path with the current datetime
     *
     * @param path
     * @return
     */
    public static Path addDateWithPrefixToPath(Path path, String modulePrefix) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return path.resolve(modulePrefix + OUTPUT_FILENAME_TXT + dateFormat.format(new Date()));
    }

    /**
     * Returns the default save path
     *
     * @return absolute Path
     */
    public static Path getDefaultSavePath() {
        return Paths.get(DEFAULT_SAVE_PATH).toAbsolutePath();
    }

    /**
     * Creates a directory with the current datetime
     *
     * @param savePath
     * @return created directory path
     * @throws IOException
     */
    public static Path createDatedDirectory(String savePath, String modulePrefix) throws IOException {
        return createDatedDirectory(Paths.get(savePath), modulePrefix);
    }

    public static Path createDatedDirectory(Path savePath, String modulePrefix) throws IOException {
        return Files.createDirectories(addDateWithPrefixToPath(savePath, modulePrefix));
    }

    /**
     * Resolves path to csv
     *
     * @param basePath
     * @param tableId
     * @return
     */
    public static Path getPathToCSV(Path basePath, String tableId) {
        if (!tableId.endsWith(GenConsts.CSV_FILE_EXTENSION)) {
            tableId = getFileName(tableId);
        }

        return basePath.resolve(tableId);
    }

    public static <T extends BaseSyncRow> Path getPathToCsv(Path basePath, Class<T> tableType) {
        return getPathToCSV(basePath, getTableName(tableType));
    }

    private static final Map<Class<? extends BaseSyncRow>, String> classToTableNameMap;
    static {
        Map<Class<? extends BaseSyncRow>, String> map = new HashMap<>();
        map.put(CsvAuthorization.class, AUTHORIZATION_TABLE_NAME);
        map.put(CsvEntitlement.class, ENTITLEMENT_TABLE_NAME);
        map.put(CsvBeneficiaryEntity.class, BENEFICIARY_ENTITY_TABLE_NAME);
        map.put(CsvIndividual.class, INDIVIDUAL_TABLE_NAME);
        map.put(CsvDelivery.class, DELIVERY_TABLE_NAME);
        map.put(CsvAuthorizationReport.class, AUTHORIZATION_REPORT_TABLE_NAME);
        map.put(CsvVisit.class, VISIT_TABLE_NAME);
        map.put(CsvVisitProgram.class, VISIT_PROGRAM_TABLE_NAME);
        map.put(CsvDistribution.class, DISTRIBUTION_TABLE_NAME);
        classToTableNameMap = Collections.unmodifiableMap(map);
    }

    public static <T extends BaseSyncRow> String getTableName(Class<T> tableType) {
        return classToTableNameMap.get(tableType);
    }

    public static <T extends BaseSyncRow> String getFileName(Class<T> tableType) {
        return getFileName(classToTableNameMap.get(tableType));
    }

    public static String getFileName(String tableId) {
        return tableId + GenConsts.CSV_FILE_EXTENSION;
    }

    // TODO: consolidate this with getFileName(Class<T> tableType)
    public static String getConfigFileName() {
        return CONFIG_FILENAME + GenConsts.JSON_FILE_EXTENSION;
    }

}
