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

import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.InstantConverter;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.EntitlementStatus;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Entitlement extends ODKEntity {

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private Authorization authorization;

  @Basic(optional = false)
  @Column(nullable = false)
  @Convert(converter = InstantConverter.class)
  private Instant dateCreated;

  @Column
  private String beneficiaryEntityId;

  @Column
  private String individualId;

  @Column
  private String beneficiaryUnitRcId;

  @Column
  private String memberRcId;

  @Basic(optional = false)
  @Column(nullable = false)
  private boolean isOverride;

  @Basic(optional = false)
  @Column(nullable = false)
  private EntitlementStatus status;

  @Column
  private String statusReason;

  @Column
  private String assignedItemCode;

  @Column
  private String createUser;

  @Column
  private String groupReadOnly;


  public String getGroupReadOnly() {  return groupReadOnly; }

  public void setGroupReadOnly(String groupReadOnly) { this.groupReadOnly = groupReadOnly; }

  public String getCreateUser() { return createUser; }

  public void setCreateUser(String createUser) { this.createUser = createUser; }

  public Authorization getAuthorization() {
    return authorization;
  }

  public void setAuthorization(Authorization authorization) {
    this.authorization = authorization;
  }

  public Instant getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Instant dateCreated) {
    this.dateCreated = dateCreated;
  }

  public String getBeneficiaryEntityId() {
    return beneficiaryEntityId;
  }

  public void setBeneficiaryEntityId(String beneficiaryEntityId) {
    this.beneficiaryEntityId = beneficiaryEntityId;
  }

  public String getIndividualId() {
    return individualId;
  }

  public void setIndividualId(String individualId) {
    this.individualId = individualId;
  }

  public String getBeneficiaryUnitRcId() {
    return beneficiaryUnitRcId;
  }

  public void setBeneficiaryUnitRcId(String beneficiaryUnitRcId) {
    this.beneficiaryUnitRcId = beneficiaryUnitRcId;
  }

  public String getMemberRcId() {
    return memberRcId;
  }

  public void setMemberRcId(String memberRcId) {
    this.memberRcId = memberRcId;
  }

  public boolean isOverride() {
    return isOverride;
  }

  public void setOverride(boolean override) {
    isOverride = override;
  }

  public EntitlementStatus getStatus() {
    return status;
  }

  public void setStatus(EntitlementStatus status) {
    this.status = status;
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
  }

  public String getAssignedItemCode() {
    return assignedItemCode;
  }

  public void setAssignedItemCode(String assignedItemCode) {
    this.assignedItemCode = assignedItemCode;
  }

  @Override
  @PrePersist
  public void prePersist() {
    super.prePersist();
    if (getDateCreated() == null) {
      setDateCreated(Instant.now());
    }
  }

  @Override
  public String toString() {
    return "Entitlement{" +
        "id=" + id +
        ", rowId='" + rowId + '\'' +
        ", authorization=" + authorization +
        ", dateCreated=" + dateCreated +
        ", beneficiaryEntityId='" + beneficiaryEntityId + '\'' +
        ", individualId='" + individualId + '\'' +
        ", beneficiaryUnitRcId='" + beneficiaryUnitRcId + '\'' +
        ", memberRcId='" + memberRcId + '\'' +
        ", isOverride=" + isOverride +
        ", status=" + status +
        ", statusReason='" + statusReason + '\'' +
        ", assignedItemCode='" + assignedItemCode + '\'' +
        ", createUser='" + createUser + '\'' +
        ", groupReadOnly='" + groupReadOnly + '\'' +
        '}';
  }
}
