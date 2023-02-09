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

package edu.uw.cse.ifrcdemo.planningsharedlib.model.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class XlsxForm {
  @JsonProperty(required = true)
  private String tableId;

  @JsonProperty(required = true)
  private String formId;

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

  @JsonCreator
  public XlsxForm(@JsonProperty(value = "tableId", required = true) String tableId,
                  @JsonProperty(value = "formId", required = true) String formId) {
    this.tableId = tableId;
    this.formId = formId;
  }

  @JsonIgnore
  public static XlsxForm convertJsonStringToXlsxForm(String dbData) {
    if(dbData != null) {
      try {
        return new ObjectMapper().readerFor(obtainTypeReference()).readValue(dbData);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @JsonIgnore
  public String processToDBString() {
    try {
      return new ObjectMapper().writerFor(obtainTypeReference()).writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  @JsonIgnore
  private static TypeReference<?> obtainTypeReference() {
    return new TypeReference<XlsxForm>() {};
  }

  @Override
  public String toString() {
    return "XlsxForm{" +
        "tableId='" + tableId + '\'' +
        ", formId='" + formId + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    XlsxForm xlsxForm = (XlsxForm) o;

    return (getTableId() != null ? getTableId().equals(xlsxForm.getTableId()) : xlsxForm.getTableId() == null) &&
            (getFormId() != null ? getFormId().equals(xlsxForm.getFormId()) : xlsxForm.getFormId() == null);

  }

  @Override
  public int hashCode() {
    int result = getTableId() != null ? getTableId().hashCode() : 0;
    result = 31 * result + (getFormId() != null ? getFormId().hashCode() : 0);
    return result;
  }
}
