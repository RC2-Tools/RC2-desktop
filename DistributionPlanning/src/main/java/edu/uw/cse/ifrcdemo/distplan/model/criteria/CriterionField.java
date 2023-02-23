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

package edu.uw.cse.ifrcdemo.distplan.model.criteria;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CriterionField {
  private final String tableId;
  private final String column;

  public String getTableId() {
    return tableId;
  }

  public String getColumn() {
    return column;
  }

  @JsonCreator
  public CriterionField(@JsonProperty(value = "tableId", required = true) String tableId,
                        @JsonProperty(value = "column", required = true) String column) {
    this.tableId = tableId;
    this.column = column;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CriterionField criterionField = (CriterionField) o;

    if (getTableId() != null ? !getTableId().equals(criterionField.getTableId()) : criterionField.getTableId() != null)
      return false;
    return getColumn() != null ? getColumn().equals(criterionField.getColumn()) : criterionField.getColumn() == null;

  }

  @Override
  public int hashCode() {
    int result = getTableId() != null ? getTableId().hashCode() : 0;
    result = 31 * result + (getColumn() != null ? getColumn().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CriterionField{" +
        "tableId='" + tableId + '\'' +
        ", column='" + column + '\'' +
        '}';
  }
}
