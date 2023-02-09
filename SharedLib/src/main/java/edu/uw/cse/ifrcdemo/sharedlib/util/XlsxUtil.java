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

package edu.uw.cse.ifrcdemo.sharedlib.util;

import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import edu.uw.cse.ifrcdemo.translations.TranslationUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class XlsxUtil {
  private static final int UNINITIALIZED = -12345;
  private static final String SETTING_NAME_CELL_VALUE = "setting_name";
  private static final String VALUE_CELL_VALUE = "value";
  private static final String FORM_ID_CELL_VALUE = "form_id";
  private static final String TABLE_ID_CELL_VALUE = "table_id";
  private static final String SETTINGS_SHEET_NAME = "settings";

  private static final String[] FORM_LEVEL_FILE_COPY_BLACKLIST = new String[]{"formDef.json"};

  public static Path organizeFormLevelFiles(Path xlsxPath, String tableId, String formId, Path outputPath) throws IOException {
    Path odkFormPath = OdkPathUtil.getFormPath(OdkPathUtil.getTablePath(outputPath, tableId), formId);

    if (!Files.isSameFile(xlsxPath.getParent(), odkFormPath)) {
      FileUtils.copyDirectory(
          xlsxPath.getParent().toFile(),
          odkFormPath.toFile(),
          new NotFileFilter(new NameFileFilter(FORM_LEVEL_FILE_COPY_BLACKLIST))
      );
    }

    return odkFormPath;
  }

  public static String formatFormDefWarnings(String filename, List<String> warnings) {
    ResourceBundle translations = TranslationUtil.getTranslations();
    StringBuilder builder = new StringBuilder()
        .append(translations.getString(TranslationConsts.XLSX_CONVERSION_WARNING))
        .append(GenConsts.COLON)
        .append(GenConsts.SPACE)
        .append(translations.getString(TranslationConsts.FILENAME_LABEL))
        .append(GenConsts.COLON)
        .append(GenConsts.SPACE)
        .append(filename)
        .append(GenConsts.NEW_LINE);

    for (String warning : warnings) {
      builder
          .append(warning)
          .append(GenConsts.NEW_LINE);
    }

    return builder.toString();
  }

  public static String getFormId(String path) throws IOException {
    return getSettingValue(path, FORM_ID_CELL_VALUE);
  }

  public static String getTableId(String path) throws IOException {
    return getSettingValue(path, TABLE_ID_CELL_VALUE);
  }

  public static String getSettingValue(String path, String settingName) throws IOException {
    int settingNameColumnIndex = UNINITIALIZED;
    int formIdColumnIndex = UNINITIALIZED;
    int settingRowIndex = UNINITIALIZED;

    String errMsg = TranslationUtil.getTranslations().getString(TranslationConsts.FORM_ID_PARSE_ERROR) + GenConsts.COLON + path;

    FileInputStream fis = null;
    Workbook workbook = null;
    try {
      fis = new FileInputStream(new File(path));
      workbook = new XSSFWorkbook(fis);
    } catch(IOException e1) {
      throw new IOException(errMsg, e1);
    } finally {
      try {
        if (fis != null)
          fis.close();
      } catch (IOException ioe) {
        // move on  as clean up has failed.
      }
    }

    if(workbook == null) {
      throw new IOException(errMsg);
    }

    Sheet settingsSheet = workbook.getSheet(SETTINGS_SHEET_NAME);
    if(settingsSheet == null) {
      throw new IOException(errMsg);
    }

    int topRow = settingsSheet.getTopRow();
    Row header = settingsSheet.getRow(topRow);
    Iterator<Cell> cellIterator = header.cellIterator();
    while(cellIterator.hasNext()) {
      Cell cell = cellIterator.next();
      String contents = cell.getStringCellValue();
      if(SETTING_NAME_CELL_VALUE.equals(contents)) {
        settingNameColumnIndex = cell.getColumnIndex();
      } else if(VALUE_CELL_VALUE.equals(contents)) {
        formIdColumnIndex = cell.getColumnIndex();
      }
      if(settingNameColumnIndex != UNINITIALIZED && formIdColumnIndex != UNINITIALIZED) {
        break;
      }
    }

    if(settingNameColumnIndex == UNINITIALIZED || formIdColumnIndex == UNINITIALIZED) {
      throw new IOException(errMsg);
    }

    for(int i = topRow + 1; i <= settingsSheet.getLastRowNum(); i++) {
      Row row = settingsSheet.getRow(i);
      Cell cell = row.getCell(settingNameColumnIndex);
      String contents = cell.getStringCellValue();
      if(settingName.equals(contents)) {
        settingRowIndex = i;
        break;
      }
    }

    if(settingRowIndex == UNINITIALIZED) {
      throw new IOException(errMsg);
    }

    Row settingRow = settingsSheet.getRow(settingRowIndex);
    Cell settingCell = settingRow.getCell(formIdColumnIndex);
    return settingCell.getStringCellValue();
  }
}
