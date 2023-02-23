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
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;

public class AuthorizationCriterion {

  private final CriterionField field;
  private final CriterionOperator operator;
  private final String value;

  public CriterionField getField() {
    return field;
  }

  public CriterionOperator getOperator() {
    return operator;
  }

  public String getValue() {
    return value;
  }

  @JsonCreator
  public AuthorizationCriterion(@JsonProperty(value = "field", required = true) CriterionField field,
                                @JsonProperty(value = "operator", required = true) CriterionOperator operator,
                                @JsonProperty(value = "value", required = true) String value) {
    this.field = field;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public String toString() {
    return String.join(GenConsts.SPACE,
        field != null ? field.toString() : GenConsts.EMPTY_STRING,
        operator != null ? operator.symbol : GenConsts.EMPTY_STRING,
        value
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AuthorizationCriterion that = (AuthorizationCriterion) o;

    if (getField() != null ? !getField().equals(that.getField()) : that.getField() != null) return false;
    if (getOperator() != that.getOperator()) return false;
    return getValue() != null ? getValue().equals(that.getValue()) : that.getValue() == null;

  }

  @Override
  public int hashCode() {
    int result = getField() != null ? getField().hashCode() : 0;
    result = 31 * result + (getOperator() != null ? getOperator().hashCode() : 0);
    result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
    return result;
  }
}
