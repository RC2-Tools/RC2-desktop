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

package edu.uw.cse.ifrcdemo.sharedlib.model.db;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.BeneficiaryEntityStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Authorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.AuthorizationReport;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.BeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Delivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Entitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.ModelStub;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MobileDbModel {


    // This is the definition of the mobile db. There are 6 tables, each with its own row class that extends
    // BaseSyncRow. This list is used to construct the data table map.
    public static final List<Class<? extends BaseSyncRow>> tableDefinition;

    static {
        List<Class<? extends BaseSyncRow>> list = new LinkedList<>();
        list.add(CsvAuthorization.class);
        list.add(CsvEntitlement.class);
        list.add(CsvBeneficiaryEntity.class);
        list.add(CsvIndividual.class);
        list.add(CsvDelivery.class);
        list.add(CsvAuthorizationReport.class);

        tableDefinition = Collections.unmodifiableList(list);
    }

    private static final Map<Class, Class> csvToStubMap;
    static {
        Map<Class, Class> map = new LinkedHashMap<>();
        map.put(CsvAuthorization.class, Authorization.class);
        map.put(CsvEntitlement.class, Entitlement.class);
        map.put(CsvBeneficiaryEntity.class, BeneficiaryEntity.class);
        map.put(CsvIndividual.class, Individual.class);
        map.put(CsvDelivery.class, Delivery.class);
        map.put(CsvAuthorizationReport.class, AuthorizationReport.class);

        csvToStubMap = Collections.unmodifiableMap(map);
    }

    private static final Map<Class, String> csvToName;
    static {
        Map<Class, String> map = new LinkedHashMap<>();
        map.put(CsvAuthorization.class, MobileDbConsts.TableNames.AUTHORIZATION_TABLE_NAME);
        map.put(CsvEntitlement.class, MobileDbConsts.TableNames.ENTITLEMENT_TABLE_NAME);
        map.put(CsvBeneficiaryEntity.class, MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME);
        map.put(CsvIndividual.class, MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME);
        map.put(CsvDelivery.class, MobileDbConsts.TableNames.DELIVERY_TABLE_NAME);
        map.put(CsvAuthorizationReport.class, MobileDbConsts.TableNames.AUTHORIZATION_REPORT_TABLE_NAME);

        csvToName = Collections.unmodifiableMap(map);
    }


    Path basePath;

    // Read/Write tables from disk
    CsvMapper mapper;

    // Data tables. See the tableDefinition list for the list of tables it will contain
    private Map<Class<? extends BaseSyncRow>, List<? extends BaseSyncRow>> mobileDb;

    /**
     * Construct a model of the mobile database. Try to read in all expected tables.
     *
     * @param basePath The folder containing
     */
    public MobileDbModel(Path basePath, List<Class<? extends BaseSyncRow>> requiredTables) throws IOException, IllegalArgumentException {

        this.basePath = basePath;
        this.mapper = CsvMapperUtil.getCsvMapper(true);

        this.mobileDb = new HashMap<>();

        // Iterate through each table type defined for the db, and add it to the map.
        boolean foundTable = false;
        for (Class<? extends BaseSyncRow> tableType : tableDefinition) {
            List<? extends BaseSyncRow> table = readTableFromDisk(tableType,
                    FileUtil.getPathToCSV(basePath, FileUtil.getFileName(tableType)).toUri());

            if (table != null) {
                foundTable = true;
            }
            mobileDb.put(tableType, table);
        }

        List<Class<? extends BaseSyncRow>> missingTables = new ArrayList<>();

        for (Class<? extends BaseSyncRow> tableType : requiredTables) {
            if (mobileDb.get(tableType) == null) {
                missingTables.add(tableType);

            }
        }

        if (missingTables.size() > 0) {
            StringBuilder errMsg = new StringBuilder(TranslationUtil.getTranslations().getString(TranslationConsts.MISSING_CSV_ERROR));

            for (Class<? extends BaseSyncRow> tableType: missingTables) {
                errMsg.append(GenConsts.NEW_LINE);
                errMsg.append(csvToName.get(tableType));
            }
            throw new IllegalArgumentException(errMsg.toString());
        }
    }

    /**
     * Retrieve a table from the database
     *
     * @param tableType The type associated with the desired table
     * @param <T> The type associated with the desired table
     * @return The list of rows
     */
    public <T extends BaseSyncRow> List<T> getTable(Class<T> tableType) {
        return (List<T>) mobileDb.get(tableType);
    }

    /**
     *
     *
     * @return A collection of the types of tables present in this mobileDb
     */
    public Collection<Class<? extends BaseSyncRow>> getPresentTableTypes() {
        return mobileDb.keySet()
                .stream()
                .filter(type -> mobileDb.get(type) != null)
                .collect(Collectors.toList());
    }

    /**
     * Add or replace a table in the database
     *
     * @param tableType The type of table
     * @param table The table itself
     * @param <T> The type of table
     */
    public <T extends BaseSyncRow> void addTable(Class<T> tableType, List<T> table) {
        mobileDb.put(tableType, table);
    }

    /**
     * Write the representation of the mobile db back to disk (overwriting the old files)
     *
     * @throws IOException
     */
    public void flushToDisk() throws IOException {
        
        for (Class<? extends BaseSyncRow> tableType : tableDefinition) {

            List<? extends BaseSyncRow> table = mobileDb.get(tableType);

            if (table != null) {
                writeTableToDisk(tableType, FileUtil.getPathToCSV(
                        basePath, FileUtil.getFileName(tableType)).toFile());
            }
        }
    }

    /**
     * Read a CSV representation of one of the supported tables from disk into memory.
     *
     * If the table is missing do not throw an error. This is expected in common cases. Instead, return null.
     *
     * @param type The type of table you want to read
     * @param path The path to the csv file
     * @param <T> The type of table you want to read
     * @return The table data
     */
    private <T extends BaseSyncRow> List<T> readTableFromDisk(Class<T> type, URI path) throws IOException {
        try {
            MappingIterator<T> iterator = CsvMapperUtil
                    .getReader(type, mapper, true)
                    .readValues(path.toURL());

            return iterator.readAll();
        } catch (FileNotFoundException e) {
            System.out.println(LogStr.LOG_DID_NOT_FIND_OR_READ_TABLE + type.getSimpleName());
        }

        return null;
    }

    /**
     * Write a CSV out to disk (overwrite whatever was there before)
     *
     * @param type Tye type of table you want to write
     * @param out The file to write to
     * @param <T> The type of table you want to write
     * @throws IOException
     */
    private <T extends BaseSyncRow> void writeTableToDisk(Class<T> type, File out) throws IOException {
        List<T> table = (List<T>) mobileDb.get(type);
        Class<? extends ModelStub> clazz = (Class<? extends ModelStub>) type;
        SequenceWriter sw = null;
        try {
            ObjectWriter writer = CsvMapperUtil.getWriter(clazz, csvToStubMap.get(type), mapper, true);
            sw = writer.writeValues(out).writeAll(table);
        } catch (IOException e) {
            System.out.println(LogStr.LOG_FAILED_TO_WRITE_TABLE_TO_DISK + type.getName());
            throw e;
        } finally {
            if(sw != null) {
              sw.close();
            }
        }
    }

    /**
     * Iterate through the table, searching for duplicate keys and creating a map of keys to conflict rows
     *
     * @param tableType The type of table to validate
     * @param keyGetter The getter method for the key
     * @param <T> The type of table
     * @return The map of conflicts
     */
    public <T extends BaseSyncRow> Map<String, List<T>> getTableConflicts(Class<T> tableType, Function<T, String> keyGetter) {
        Map<String, List<T>>  indexMap = new HashMap<>();
        Set<String> conflictSet = new HashSet<>();

        List<T> table = (List<T>) mobileDb.get(tableType);


        // Create a map from each key to a list of rows that share that key. If there are no conflicts each list will
        // only contain one row.
        for (T row : table) {
            String key = keyGetter.apply(row);

            // do not generate a conflict for disabled beneficiaries that have the same beneficiary entity id
            // note that the second equality check is hacky way of seeing that the current keyGetter is for beneficiaryEntityId, not rowId
            if (row instanceof CsvBeneficiaryEntity
                    && ((CsvBeneficiaryEntity) row).getBeneficiaryEntityId().equals(key)
                    && ((CsvBeneficiaryEntity) row).getStatus() == BeneficiaryEntityStatus.DISABLED) {
                continue;
            }

            List<T> conflictList;
            if (indexMap.containsKey(key)) {
                conflictList = indexMap.get(key);

                // If we're adding to an existing key, then this row is in conflict and we will add it to the set
                conflictSet.add(key);
            } else {
                conflictList = new LinkedList<>();
            }

            conflictList.add(row);
            indexMap.put(key, conflictList);
        }

        // Now that we have our full map, and the set of keys that have conflicts, return to the user only the lists
        // of conflict rows. This map is identical to the indexMap but has all lists of with size < 2 removed.
        Map<String, List<T>> conflictRows = new HashMap<>(conflictSet.size());
        for (String key : conflictSet) {
            conflictRows.put(key, indexMap.get(key));
        }

        return conflictRows;
    }
}
