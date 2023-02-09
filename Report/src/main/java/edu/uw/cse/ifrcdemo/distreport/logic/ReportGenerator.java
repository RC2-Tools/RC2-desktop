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

package edu.uw.cse.ifrcdemo.distreport.logic;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import edu.uw.cse.ifrcdemo.distreport.consts.ReportConsts;
import edu.uw.cse.ifrcdemo.distreport.consts.RequiredCsvs;
import edu.uw.cse.ifrcdemo.distreport.model.ReportFilter;
import edu.uw.cse.ifrcdemo.distreport.model.ReportMetadata;
import edu.uw.cse.ifrcdemo.distreport.model.ReportUntypedSyncRow;
import edu.uw.cse.ifrcdemo.distreport.util.TableUtil;
import edu.uw.cse.ifrcdemo.mustachetopdf.PdfGenerator;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.ModuleConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvAuthorization;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.HasCustomTable;
import edu.uw.cse.ifrcdemo.sharedlib.util.CsvMapperUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.DialogUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

import javax.swing.JPanel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReportGenerator {

    private static final MustacheFactory mustacheFactory;
    private static final Logger logger;


    static {
        logger = LogManager.getLogger(ReportGenerator.class);
        mustacheFactory = new DefaultMustacheFactory();
    }


    public static void generateReport(Path inputPath, Path outputPath, JPanel viewPanel) {
        ResourceBundle translations = TranslationUtil.getTranslations();
        try {
            outputPath = FileUtil.createDatedDirectory(outputPath, ModuleConsts.REPORT);
        } catch (IOException e) {
            logger.error(LogStr.LOG_FAILED_TO_CREATE_DATED_DIRECTORY, e);
            DialogUtil.showErrorDialog(viewPanel, translations.getString(TranslationConsts.CANNOT_WRITE_DIR_ERROR));
            return;
        }

        Map<String, byte[]> fileBytes;
        try {
            fileBytes = readAllFiles(inputPath);
        } catch (IOException e) {
            logger.error(LogStr.LOG_FAILED_TO_READ_FILES, e);
            DialogUtil.showErrorDialog(viewPanel, translations.getString(TranslationConsts.CANNOT_READ_DIR_ERROR));
            return;
        }

        CsvMapper mapper = CsvMapperUtil.getCsvMapper(true);

        Map<Class<? extends BaseSyncRow>, List<BaseSyncRow>> baseTables;
        try {
            baseTables = parseBaseTables(mapper, fileBytes);
        } catch (RuntimeException e) {
            logger.error(LogStr.LOG_FAILED_TO_PARSE_BASE_TABLES, e);
            DialogUtil.showErrorDialog(viewPanel, e.getMessage());
            return;
        }

        Map<String, List<UntypedSyncRow>> allTables = parseAllTables(mapper, fileBytes);

        Map<Class<? extends BaseSyncRow>, Map<String, BaseSyncRow>> baseTablesIndexed =
                TableUtil.indexTables(baseTables);
        Map<String, Map<String, UntypedSyncRow>> allTablesIndexed =
                TableUtil.indexTables(allTables);

        copyBaseTableToCustomTable(baseTables, allTablesIndexed);

        Set<String> validAuthId = baseTablesIndexed.get(CsvAuthorization.class).keySet();
        groupRowsByAuth(allTables, validAuthId)
                .forEach(reportPrinter(baseTablesIndexed, allTablesIndexed, inputPath, outputPath));

        DialogUtil.showConfirmDialog(viewPanel, translations.getString(TranslationConsts.DONE_LABEL), translations.getString(TranslationConsts.REPORT_GENERATED_MSG));
    }

    public static void generateCustomReport(String templatePath, Path outputPath) {
        Mustache template = mustacheFactory.compile(templatePath);
        try {
            PdfGenerator.generatePdf(template, null, outputPath);
            logger.info(LogStr.LOG_SUCCESSFULLY_WROTE_REPORT_FOR_TO, "custom", "");
        } catch (Exception e) {
            logger.error(new FormattedMessage(LogStr.LOG_FAILED_TO_GENERATE_PDF_FOR, "custom"), e);
        }
    }

    private static void copyBaseTableToCustomTable(Map<Class<? extends BaseSyncRow>, List<BaseSyncRow>> baseTables,
                                            Map<String, Map<String, UntypedSyncRow>> allTablesIndexed) {
        for (Map.Entry<Class<? extends BaseSyncRow>, List<BaseSyncRow>> entry : baseTables.entrySet()) {
            if (entry.getValue().size() <= 0 || !HasCustomTable.class.isInstance(entry.getValue().get(0))) {
                // skip
                continue;
            }

            String tableName = FileUtil.getTableName(entry.getKey());

            for (BaseSyncRow row : entry.getValue()) {
                HasCustomTable awareRow = (HasCustomTable) row;

                Map<String, UntypedSyncRow> indexedCustomTable = allTablesIndexed.get(awareRow.getCustomTableFormId());

                if (indexedCustomTable != null) {
                    UntypedSyncRow customTableRow = indexedCustomTable.get(awareRow.getCustomTableRowId());

                    if (customTableRow != null) {
                        Map<String, UntypedSyncRow> indexedBaseTable = allTablesIndexed.get(tableName);
                        UntypedSyncRow baseTableRow = indexedBaseTable.get(row.getRowId());

                        baseTableRow.getColumns().forEach(customTableRow.getColumns()::putIfAbsent);
                    } else {
                        logger.warn(LogStr.LOG_BASE_TABLE_ROW_REFERENCES_A_NON_EXISTING_CUSTOM_TABLE_ROW_OF,
                                tableName, row.getRowId(), awareRow.getCustomTableRowId(), awareRow.getCustomTableFormId());
                    }
                } else {
                    logger.warn(LogStr.LOG_BASE_TABLE_ROW_REFERENCES_A_NON_EXISTING_CUSTOM_TABLE,
                            tableName, row.getRowId(), awareRow.getCustomTableFormId());
                }
            }
        }
    }

    public static Map<String, List<ReportUntypedSyncRow>> buildWrappedTables(Map<String, List<UntypedSyncRow>> tables, Path inputPath) {
        return tables
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> ReportConsts.WRAPPED_SCOPE_NAME + GenConsts.PERIOD + entry.getKey(),
                        entry -> entry
                                .getValue()
                                .stream()
                                .map(row -> new ReportUntypedSyncRow(row, entry.getKey(), inputPath))
                                .collect(Collectors.toList())
                ));
    }

    private static Map<String, byte[]> readAllFiles(Path dataPath) throws IOException {
        try (Stream<Path> files = Files.list(dataPath)) {
            Map<String, byte[]> map = files
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .collect(Collectors.toMap(
                            path -> path.getFileName().toString(),
                            path -> {
                                try {
                                    return Files.readAllBytes(path);
                                } catch (IOException e) {
                                    // ignore this file
                                    logger.warn(new FormattedMessage(LogStr.LOG_FAILED_TO_READ_IGNORED, path.toAbsolutePath().toString()), e);
                                    return new byte[0];
                                }
                            }
                    ));

            // remove ignored entries from the map
            map.values().removeIf(arr -> arr.length == 0);

            return map;
        }
    }

    @SuppressWarnings("unchecked")
    private static <V extends Map> BiConsumer<String, V> reportPrinter(Map<Class<? extends BaseSyncRow>, Map<String, BaseSyncRow>> baseTablesIndexed,
                                                                Map<String, Map<String, UntypedSyncRow>> allTablesIndexed, Path inputPath,
                                                                Path outputPath) {
        return (String authId, V tables) -> {
            CsvAuthorization auth = ((CsvAuthorization) baseTablesIndexed.get(CsvAuthorization.class).get(authId));
            String authName = auth.getDistributionName()+ GenConsts.UNDERSCORE +  auth.getItemName();
            String authTableName = MobileDbConsts.TableNames.AUTHORIZATION_TABLE_NAME;

            Path reportPath = outputPath.resolve(getReportFilename(authName));


            Map<String, Object> scopes = new HashMap<>(tables);
            scopes.putAll(buildWrappedTables(tables, inputPath));
            scopes.put(authTableName, allTablesIndexed.get(authTableName).get(authId));
            scopes.put(ReportConsts.METADATA_SCOPE_NAME, new ReportMetadata());
            scopes.put(ReportConsts.FILTER_FUNCTION_SCOPE_NAME, new ReportFilter(tables));
            // add either map of functions or function object to scope. Define this in another class, see ReportMetadata as a example.
            // if you cannot pass in a list from mustache then make the function have access to it

            Mustache template = mustacheFactory.compile(ReportConsts.TEMPLATE_PATH);
            try {
                PdfGenerator.generatePdf(template, scopes, reportPath);
                logger.info(LogStr.LOG_SUCCESSFULLY_WROTE_REPORT_FOR_TO, authName, reportPath);
            } catch (Exception e) {
                logger.error(new FormattedMessage(LogStr.LOG_FAILED_TO_GENERATE_PDF_FOR, authName), e);
            }
        };
    }

    private static Map<Class<? extends BaseSyncRow>, List<BaseSyncRow>> parseBaseTables(CsvMapper mapper,
                                                                                 Map<String, byte[]> fileBytes) {
        return RequiredCsvs
                .REQUIRED_CSVS
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        clazz -> {
                            String filename = FileUtil.getFileName(clazz);
                            ResourceBundle translations = TranslationUtil.getTranslations();
                            if (!fileBytes.containsKey(filename)) {
                                throw new RuntimeException(translations.getString(TranslationConsts.MISSING_BASE_TABLE_ERROR) + GenConsts.SPACE + filename);
                            }

                            try {
                                return CsvMapperUtil
                                        .getReader(clazz, mapper, true)
                                        .<BaseSyncRow>readValues(fileBytes.get(filename))
                                        .readAll();
                            } catch (IOException e) {
                                throw new RuntimeException(translations.getString(TranslationConsts.FAILED_PARSE_ERROR)+ GenConsts.SPACE + filename, e);
                            }
                        }
                ));
    }

    private static Map<String, List<UntypedSyncRow>> parseAllTables(CsvMapper mapper, Map<String, byte[]> fileBytes) {
        ObjectReader untypedReader = CsvMapperUtil.getReader(mapper, true);

        Map<String, List<UntypedSyncRow>> allTables = fileBytes
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry
                                .getKey()
                                // use filename without extension as key
                                // assumes that file extension is .csv
                                .substring(0, entry.getKey().length() - GenConsts.CSV_FILE_EXTENSION.length()),
                        entry -> {
                            try {
                                return untypedReader.<UntypedSyncRow>readValues(entry.getValue()).readAll();
                            } catch (IOException e) {
                                // ignore this table
                                logger.warn(new FormattedMessage(LogStr.LOG_FAILED_TO_PARSE_IGNORED, entry.getKey()), e);
                                return Collections.emptyList();
                            }
                        }
                ));

        allTables.values().removeIf(List::isEmpty);
        return allTables;
    }

    private static Map<String, Map<String, List<UntypedSyncRow>>> groupRowsByAuth(Map<String, List<UntypedSyncRow>> allTables,
                                                                           Set<String> validAuthId) {
        Map<String, Map<String, List<UntypedSyncRow>>> groupedTables = validAuthId
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        __ -> new HashMap<>()
                ));

        for (Map.Entry<String, List<UntypedSyncRow>> entry : allTables.entrySet()) {
            String tableId = entry.getKey();

            for (UntypedSyncRow row : entry.getValue()) {
                String authId = row.getColumns().get(MobileDbConsts.AUTHORIZATION_ID_COLUMN);

                if (StringUtil.isNullOrEmpty(authId)) {
                    continue;
                }

                Map<String, List<UntypedSyncRow>> indexedTable = groupedTables.get(authId);

                if (indexedTable == null) {
                    logger.warn(LogStr.LOG_ROW_OF_TABLE_HAS_AN_INVALID_AUTHORIZATION_ID, row.getRowId(), tableId);
                    continue;
                }

                indexedTable
                        .computeIfAbsent(tableId, __ -> new ArrayList<>())
                        .add(row);
            }
        }

        return groupedTables;
    }

    private static String getReportFilename(String authName) {
        return ReportConsts.REPORT_FILENAME_PREFIX + authName + GenConsts.PDF_FILE_EXTENSION;
    }
}
