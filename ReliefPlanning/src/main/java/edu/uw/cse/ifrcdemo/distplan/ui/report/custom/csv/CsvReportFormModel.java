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

package edu.uw.cse.ifrcdemo.distplan.ui.report.custom.csv;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;

import java.util.List;
import java.util.Map;

public class CsvReportFormModel {
  private String primaryFilter;
  private boolean includeUnrealized;
  private List<List<AuthorizationCriterion>> criteria;

  private Map<String, String> distributionSet;
  private Map<String, String> visitProgramSet;

  public String getPrimaryFilter() {
    return primaryFilter;
  }

  public void setPrimaryFilter(String primaryFilter) {
    this.primaryFilter = primaryFilter;
  }

  public boolean isIncludeUnrealized() {
    return includeUnrealized;
  }

  public void setIncludeUnrealized(boolean includeUnrealized) {
    this.includeUnrealized = includeUnrealized;
  }

  public List<List<AuthorizationCriterion>> getCriteria() {
    return criteria;
  }

  public void setCriteria(List<List<AuthorizationCriterion>> criteria) {
    this.criteria = criteria;
  }

  public Map<String, String> getDistributionSet() {
    return distributionSet;
  }

  public void setDistributionSet(Map<String, String> distributionSet) {
    this.distributionSet = distributionSet;
  }

  public Map<String, String> getVisitProgramSet() {
    return visitProgramSet;
  }

  public void setVisitProgramSet(Map<String, String> visitProgramSet) {
    this.visitProgramSet = visitProgramSet;
  }
}
