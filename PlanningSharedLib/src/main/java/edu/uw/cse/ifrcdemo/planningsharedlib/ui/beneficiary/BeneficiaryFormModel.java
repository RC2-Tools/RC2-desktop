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

package edu.uw.cse.ifrcdemo.planningsharedlib.ui.beneficiary;

import java.util.List;
import java.util.stream.Collectors;

public class BeneficiaryFormModel {

  private String searchName;
  private List<BeneficiaryFormData> beneficiaries;
  private Integer elementsPerPage;
  private Integer numOfPages;
  private Integer currPage;
  private Integer startElementIndex;
  private Integer endElementIndex;
  private Integer totalBeneficiarySetSize;

  public String getSearchName() {
    return searchName;
  }

  public void setSearchName(String searchName) {
    this.searchName = searchName;
  }

  public List<BeneficiaryFormData> getBeneficiaries() {
    return beneficiaries;
  }

  public void setBeneficiaries(List<BeneficiaryFormData> beneficiaries) {
    this.beneficiaries = beneficiaries;
  }

  public Integer getElementsPerPage() {
    return elementsPerPage;
  }

  public void setElementsPerPage(Integer elementsPerPage) {
    this.elementsPerPage = elementsPerPage;
  }

  public Integer getNumOfPages() {
    return numOfPages;
  }

  public void setNumOfPages(Integer numOfPages) {
    this.numOfPages = numOfPages;
  }

  public Integer getCurrPage() {
    return currPage;
  }

  public void setCurrPage(Integer currPage) {
    this.currPage = currPage;
  }

  public Integer getStartElementIndex() {
    return startElementIndex;
  }

  public void setStartElementIndex(Integer startElementIndex) {
    this.startElementIndex = startElementIndex;
  }

  public Integer getEndElementIndex() {
    return endElementIndex;
  }

  public void setEndElementIndex(Integer endElementIndex) {
    this.endElementIndex = endElementIndex;
  }

  public Integer getTotalBeneficiarySetSize() {
    return totalBeneficiarySetSize;
  }

  public void setTotalBeneficiarySetSize(Integer totalBeneficiarySetSize) {
    this.totalBeneficiarySetSize = totalBeneficiarySetSize;
  }

  public void searchForBeneficiaryByNameOrRcId() {
    if (searchName == null || searchName.length() == 0)
      return;

    if (beneficiaries == null || beneficiaries.size() <= 0)
      return;

    beneficiaries = beneficiaries
        .stream()
        .filter(this::beneficiarySearch)
        .collect(Collectors.toList());
  }

  private boolean beneficiarySearch(BeneficiaryFormData beneficiary) {
    String searchTerm = searchName.toLowerCase();

    return ((beneficiary.getFirstName() + " " + beneficiary.getLastName()).toLowerCase()
        .contains(searchTerm)) ||
        beneficiary.getRcId().contains(searchName);
  }

  @Override
  public String toString() {
    return "BeneficiaryFormModel{" + "searchName='" + searchName + '\'' + ", beneficiaries="
        + beneficiaries + ", elementsPerPage=" + elementsPerPage + ", numOfPages=" + numOfPages
        + ", currPage=" + currPage + ", startElementIndex=" + startElementIndex
        + ", endElementIndex=" + endElementIndex + ", totalBeneficiarySetSize="
        + totalBeneficiarySetSize + '}';
  }
}
