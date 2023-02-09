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

package edu.uw.cse.ifrcdemo.planningsharedlib.entity;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.AuthorizationRuleConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.RangeListConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.InstantConverter;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.ExtraFieldEntitlements;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.time.Instant;
import java.util.List;

@Entity
public class Authorization extends ODKEntity {

  @Basic(optional = false)
  @Column(nullable = false)
  private AuthorizationType type;

  @Basic(optional = false)
  @Column(nullable = false)
  private boolean assignItemCode;

  @Column
  private String customDeliveryFormStr;

  @Basic(optional = false)
  @Column(nullable = false)
  @Convert(converter = InstantConverter.class)
  private Instant dateCreated;

  @Basic(optional = false)
  @Column(nullable = false)
  private boolean forIndividual;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn
  private Item item;

  @Column(columnDefinition="TEXT")
  @Convert(converter = RangeListConverter.class)
  private List<Range> itemRanges;

  @Column(columnDefinition="TEXT")
  @Convert(converter = RangeListConverter.class)
  private List<Range> voucherRanges;

  @Basic(optional = false)
  @Column(nullable = false)
  private ExtraFieldEntitlements extraFieldEntitlements;

  @Column(columnDefinition="TEXT")
  @Convert(converter = AuthorizationRuleConverter.class)
  private List<List<AuthorizationCriterion>> rules;

  @Column(columnDefinition="TEXT")
  @Convert(converter = RangeListConverter.class)
  private List<Range> beneficiaryRanges;

  @Basic(optional = false)
  @Column(nullable = false)
  private AuthorizationStatus status;

  @Column
  private String statusReason;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinColumn
  private Distribution distribution;

  public ODKEntity getPersistenceEntity(EntityManager em) {
    ODKEntity tmp = item.getPersistenceEntity(em);
    if(tmp instanceof Item) {
      item = (Item) tmp;
    } else {
      throw new IllegalArgumentException("Unable to cast persistence entity for Item");
    }
    return super.getPersistenceEntity(em);
  }

  public AuthorizationType getType() {
    return type;
  }

  public void setType(AuthorizationType type) {
    this.type = type;
  }

  public boolean isAssignItemCode() {
    return assignItemCode;
  }

  public void setAssignItemCode(boolean assignItemCode) {
    this.assignItemCode = assignItemCode;
  }

  public XlsxForm getCustomDeliveryForm() {
    return XlsxForm.convertJsonStringToXlsxForm(customDeliveryFormStr);
  }

  public void setCustomDeliveryForm(XlsxForm customDeliveryForm) {
    if(customDeliveryForm == null) {
      this.customDeliveryFormStr = null;
    } else {
      this.customDeliveryFormStr = customDeliveryForm.processToDBString();
    }
  }

  public Instant getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Instant dateCreated) {
    this.dateCreated = dateCreated;
  }

  public boolean getForIndividual() {
    return forIndividual;
  }

  public void setForIndividual(boolean forIndividual) {
    this.forIndividual = forIndividual;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public List<Range> getItemRanges() {
    return itemRanges;
  }

  public void setItemRanges(List<Range> itemRanges) {
    this.itemRanges = itemRanges;
  }

  public List<Range> getVoucherRanges() {
    return voucherRanges;
  }

  public void setVoucherRanges(List<Range> voucherRanges) {
    this.voucherRanges = voucherRanges;
  }

  public ExtraFieldEntitlements getExtraFieldEntitlements() {
    return extraFieldEntitlements;
  }

  public void setExtraFieldEntitlements(ExtraFieldEntitlements restrictOverrides) {
    this.extraFieldEntitlements = restrictOverrides;
  }

  public List<List<AuthorizationCriterion>> getRules() {
    return rules;
  }

  public void setRules(List<List<AuthorizationCriterion>> rules) {
    this.rules = rules;
  }

  public List<Range> getBeneficiaryRanges() {
    return beneficiaryRanges;
  }

  public void setBeneficiaryRanges(List<Range> beneficiaryRanges) {
    this.beneficiaryRanges = beneficiaryRanges;
  }

  public AuthorizationStatus getStatus() {
    return status;
  }

  public void setStatus(AuthorizationStatus status) {
    this.status = status;
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
  }

  public Distribution getDistribution() {
    return distribution;
  }

  public void setDistribution(Distribution distribution) {
    this.distribution = distribution;
  }

  @Override
  @PrePersist
  public void prePersist() {
    super.prePersist();
    if (getDateCreated() == null) {
      setDateCreated(Instant.now());
    }
  }
}
