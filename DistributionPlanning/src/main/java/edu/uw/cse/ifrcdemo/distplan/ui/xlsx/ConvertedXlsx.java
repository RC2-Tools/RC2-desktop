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

package edu.uw.cse.ifrcdemo.distplan.ui.xlsx;

import java.util.List;

public class ConvertedXlsx {
  private String tableId;
  private String formId;
  private String formDef;
  private String definition;
  private String properties;
  private String tableSpecificDefinitions;
  private List<String> warnings;

  private String xlsxPath;

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

  public String getFormDef() {
    return formDef;
  }

  public void setFormDef(String formDef) {
    this.formDef = formDef;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public String getProperties() {
    return properties;
  }

  public void setProperties(String properties) {
    this.properties = properties;
  }

  public String getTableSpecificDefinitions() {
    return tableSpecificDefinitions;
  }

  public void setTableSpecificDefinitions(String tableSpecificDefinitions) {
    this.tableSpecificDefinitions = tableSpecificDefinitions;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings;
  }

  public String getXlsxPath() {
    return xlsxPath;
  }

  public void setXlsxPath(String xlsxPath) {
    this.xlsxPath = xlsxPath;
  }

  @Override
  public String toString() {
    return "ConvertedXlsx{" +
        "tableId='" + tableId + '\'' +
        ", formId='" + formId + '\'' +
        ", formDef='" + formDef + '\'' +
        ", definition='" + definition + '\'' +
        ", properties='" + properties + '\'' +
        ", tableSpecificDefinitions='" + tableSpecificDefinitions + '\'' +
        ", warnings=" + warnings +
        ", xlsxPath='" + xlsxPath + '\'' +
        '}';
  }
}
