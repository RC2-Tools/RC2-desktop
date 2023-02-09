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

package edu.uw.cse.ifrcdemo.sharedlib.logic;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.LogConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.db.MobileDbModel;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.BaseSyncRow;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.util.FileUtil;
import edu.uw.cse.ifrcdemo.sharedlib.util.SwingUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.CharEncoding;

public class Validator {

    MobileDbModel db;

    public Validator(MobileDbModel db) {
        this.db = db;
    }

    public void resolveBeneficiaryEntityIdConflicts() {
        // Get conflicts for beneficiary entities
        Map<String, List<CsvBeneficiaryEntity>> beneficiaryEntitiesConflicts = db.getTableConflicts(
                CsvBeneficiaryEntity.class, CsvBeneficiaryEntity::getBeneficiaryEntityId);
        if (beneficiaryEntitiesConflicts != null) {
            System.out.println(LogStr.LOG_BENEFICIARY_ENTITY_CONFLICTS + beneficiaryEntitiesConflicts.size());
            showConflictDialogAndLog(MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME, beneficiaryEntitiesConflicts.keySet());
        } else {
            System.out.println(LogStr.LOG_NO_BENEFICIARY_ENTITY_CONFLICTS);
        }

        // TODO: Create User Interface and logic to resolve found conflicts
    }

    // TODO: Confirm with Ori has this works
//    public void resolveIndividualIdConflicts() {
//        // Get conflicts for beneficiary entities
//        Map<String, List<CsvIndividual>> individualConflicts = db.getTableConflicts(
//                CsvIndividual.class, CsvIndividual::getMemberId);
//        if (individualConflicts != null) {
//            System.out.println("individual conflicts: " + individualConflicts.size());
//            showConflictDialogAndLog(TableNames.INDIVIDUAL_TABLE_NAME, individualConflicts.keySet());
//        } else {
//            System.out.println("No individual conflicts");
//        }
//
//        // TODO: Create User Interface and logic to resolve found conflicts
//    }


    public void resolveAllRowIdConflicts() {

        // Iterate through each predefined table and try to download it
        for (Class<? extends BaseSyncRow> tableType : db.getPresentTableTypes()) {
            resolveRowIdConflicts(tableType);
        }

    }

    public <T extends BaseSyncRow> void resolveRowIdConflicts(Class<T> type) {
        // Get conflicts for beneficiary entities
        Map<String, List<T>> tableConflicts = db.getTableConflicts(type, BaseSyncRow::getRowId);
        if (tableConflicts != null) {
            System.out.println(FileUtil.getTableName(type) + LogStr.LOG_ROW_ID_CONFLICTS + tableConflicts.size());
            showConflictDialogAndLog(MobileDbConsts.TableNames.BENEFICIARY_ENTITY_TABLE_NAME, tableConflicts.keySet());
        } else {
            System.out.println(LogStr.LOG_NO_ROW_ID_CONFLICTS + FileUtil.getTableName(type));
        }
    }


    public void showConflictDialogAndLog(String tableId, Set<String> conflictKeys) {
        if (conflictKeys.size() <= 0) {
            return;
        }
        // Get the current date and time
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat(GenConsts.DATE_FORMAT);

        // Create string to display to user
        StringBuilder conflictsDetected = new StringBuilder();
        conflictsDetected.append(dateFormat.format(date)).append(GenConsts.COLON).append(GenConsts.SPACE);
        conflictsDetected.append(TranslationUtil.getTranslations().getString(TranslationConsts.CONFLICTS_DETECTED_MSG));
        conflictsDetected.append(tableId).append(GenConsts.NEW_LINE);

        for (String key : conflictKeys) {
            conflictsDetected.append(key);
            conflictsDetected.append('\n');
        }

        File conflictsLogFile = null;
        FileOutputStream out = null;
        OutputStreamWriter output = null;
        try {
            conflictsLogFile = new File(LogConsts.CONFLICT_LOG_FILE_PATH);

            // Create parent directories if necessary
            File conflictsLogDirs = conflictsLogFile.getParentFile();
            if (!conflictsLogDirs.exists() && !conflictsLogDirs.mkdirs()) {
                String error = TranslationUtil.getTranslations().getString(TranslationConsts.CREATE_DIR_ERROR);
                throw new IllegalStateException(error + conflictsLogDirs);
            }

            out = new FileOutputStream(conflictsLogFile, true);
            output = new OutputStreamWriter(out, CharEncoding.UTF_8);
            output.write(conflictsDetected.toString());

        } catch (IOException e) {
            System.out.println(LogStr.LOG_VALIDATOR_EXCEPTION_WHILE_TRYING_TO_WRITE_TO_LOG_FILE);
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ioe) {
                System.out.println(LogStr.LOG_VALIDATOR_EXCEPTION_WHILE_TRYING_TO_CLOSE_LOG_FILE);
                ioe.printStackTrace();
            }
        }

        if (conflictsLogFile != null) {
            conflictsDetected.append(TranslationUtil.getTranslations().getString(TranslationConsts.MORE_INFO_MSG))
                    .append(conflictsLogFile.getAbsoluteFile().toString())
                    .append(GenConsts.NEW_LINE);
        }

        JOptionPane.showMessageDialog(SwingUtil.getActiveFrame(), conflictsDetected.toString());
    }
}
