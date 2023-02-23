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

package edu.uw.cse.ifrcdemo.distplan.model.rctemplate;

import edu.uw.cse.ifrcdemo.distplan.data.DataInstance;
import edu.uw.cse.ifrcdemo.distplan.data.DataRepos;
import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Distribution;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.entity.RcTemplate;
import edu.uw.cse.ifrcdemo.distplan.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.util.InternalFileStoreUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.util.ErrorUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.wink.json4j.JSONException;

import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class RcTemplateUtil {

    public static final String TEMPLATE_PREFIX = "RC2-";
    public static final String FILE_EXTENSION_ZIP = ".zip";
    public static final String TEMPLATE_DIR = "template";
    public static final String TABLES_DIR = "tables";
    public static final Path DEST_TABLES_PATH = Paths.get(InternalFileStoreUtil.XLSX_STORAGE_PATH, TABLES_DIR);

    public static final String ZIP_ILLEGAL_ARGS_EXCEPTION_MSG = "Did not specify either the template to zip or the zip output location";
    public static final String ZIP_NOT_SPECIFIED_EXCEPTION_MSG = "Need to specify a zip";
    public static final String ZIP_NOT_TEMPLATE_EXCEPTION_MSG = "The zip file does not contain a recognized template";

    static void copyFormForTemplateIfNotNull(File file, XlsxForm fieldSummaryForm) throws IOException {
        if(fieldSummaryForm != null) {
            copyFormsToTemplate(file, fieldSummaryForm.getTableId());
        }
    }

    static void copyFormForTemplateIfNotNull(File file, String tableId) throws IOException {
        if(tableId != null) {
            copyFormsToTemplate(file, tableId);
        }
    }

    private static void copyFormsToTemplate(File file, String tableId) throws IOException {
        if(MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME.equals(tableId)
                || MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME.equals(tableId)) {
            return; // inside in database so no associated form
        }
        Path tableIdDirPath = InternalFileStoreUtil.getXlsxFormStoragePath().resolve(TABLES_DIR).resolve(tableId);
        File srcXlsxDir = tableIdDirPath.toFile();
        if (srcXlsxDir.exists() && file != null) {
            Path destXlsxPath = file.toPath().resolve(DEST_TABLES_PATH).resolve(tableId);
            FileUtils.copyDirectory(srcXlsxDir, destXlsxPath.toFile());
        }
    }

    private static void copyTemplateFormsToSystem(Path templateLocation) throws IOException {
        File tablesDir = new File(InternalFileStoreUtil.getXlsxFormStoragePath().toFile(), TABLES_DIR);
        if (templateLocation != null) {
            File srcXlsxDir = templateLocation.resolve(DEST_TABLES_PATH).toFile();
            if(srcXlsxDir.exists()) {
                FileUtils.copyDirectory(srcXlsxDir, tablesDir);
            }
        }
    }


    private static String sanitizeFilename(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    public static String createTemplateFilename(RcTemplate template) {
        return TEMPLATE_PREFIX + RcTemplateUtil.sanitizeFilename(template.getName()) + FILE_EXTENSION_ZIP;
    }

    public static void generateTemplateZip(RcTemplate template, Path outputZipDir) throws IOException, JSONException, IllegalArgumentException {
        if(template == null || outputZipDir == null) {
            throw new IllegalArgumentException(ZIP_ILLEGAL_ARGS_EXCEPTION_MSG);
        }
        InternalFileStoreUtil.setCurrentTempPath();
        Path tmpDirectory = InternalFileStoreUtil.getCurrentTempStoragePath().resolve(TEMPLATE_DIR);
        template.writeEncodinDirectory(tmpDirectory);

        String fileName = createTemplateFilename(template);
        ZipUtil.zipDirectory(tmpDirectory, outputZipDir.resolve(fileName));
    }

    public static void importTemplate(File templateZip) throws IOException, IllegalArgumentException, JSONException {
        if(templateZip == null || !templateZip.exists()) {
            // TODO: verify it's a zip file
            throw new IllegalArgumentException(ZIP_NOT_SPECIFIED_EXCEPTION_MSG);
        }

        DataRepos dataRepos = DataInstance.getDataRepos();
        // create a tmp directory
        InternalFileStoreUtil.setCurrentTempPath();
        Path tmpExtractDirPath = InternalFileStoreUtil.getCurrentTempStoragePath();
        File tmpExtractDir = tmpExtractDirPath.toFile();
        tmpExtractDir.mkdir();

        Path templateExtractPath = tmpExtractDirPath.resolve(TEMPLATE_DIR);

        // extract the files and import template
        ZipUtil.extractZip(templateZip.toPath(),tmpExtractDirPath);
        Path distributionTemplateJson = templateExtractPath.resolve(RcDistributionTemplate.FILENAME);
        Path visitTemplateJson = templateExtractPath.resolve(RcVisitProgramTemplate.FILENAME);

        RcTemplate newTemplate = null;
        if(distributionTemplateJson.toFile().exists()) {
            RcDistributionTemplate distributionTemplate = new RcDistributionTemplate(distributionTemplateJson.toFile());
            Distribution dist =  distributionTemplate.getDistribution();
            copyTemplateFormsToSystem(templateExtractPath);
            ItemPackRepository itemPackRepository = dataRepos.getItemPackRepository();
            for(Authorization auth : dist.getAuthorizations()) {
                ItemPack itemPack = auth.getItemPack();
                try {
                    itemPackRepository.getItemPackByRowId(itemPack.getRowId());
                } catch (NoResultException e) {
                    CompletableFuture dbItemPack = itemPackRepository.saveItemPack(itemPack);
                    try {
                        dbItemPack.get();
                    } catch (Exception e1) {
                        ErrorUtil.handleException(e1);
                    }
                }
            }
            newTemplate = distributionTemplate.getTemplate();
        } else if(visitTemplateJson.toFile().exists()) {
            RcVisitProgramTemplate distributionTemplate = new RcVisitProgramTemplate(visitTemplateJson.toFile());
            copyTemplateFormsToSystem(templateExtractPath);
            newTemplate = distributionTemplate.getTemplate();
        } else {
            throw new IllegalArgumentException(ZIP_NOT_TEMPLATE_EXCEPTION_MSG);
        }

        // insert template into database
        if(newTemplate != null) {
            RcTemplateRepository templateRepository = dataRepos.getRcTemplateRepository();
            templateRepository.saveRcTemplate(newTemplate);
        }

        FileUtils.deleteDirectory(tmpExtractDir);
    }


    public static void outputJsonFile(File jsonFile, String jsonObj) throws IOException {

        PrintWriter outputStream = null;
        try {
            outputStream = new PrintWriter(jsonFile);
            outputStream.print(jsonObj);
        } catch (IOException e) {
            throw e;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
