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

package edu.uw.cse.ifrcdemo.distplan.entity;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.AuthorizationRuleConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.RangeListConverter;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.ODKEntity;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.InstantConverter;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import java.time.Instant;
import java.util.List;

@Entity
@NamedQuery(
    name = "vpListDto",
    query = "SELECT NEW " +
        "edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramListDto(vp.id, vp.rowId, vp.name, vp.status) " +
        "FROM VisitProgram vp " +
        "ORDER BY vp.name ASC"
)
public class VisitProgram  extends ODKEntity {

  @Basic(optional = false)
  @Column(nullable = false)
  private String customVisitFormStr;

  @Basic(optional = false)
  @Column(nullable = false)
  @Convert(converter = InstantConverter.class)
  private Instant dateCreated;

  @Column
  private String description;

  @Basic(optional = false)
  @Column(nullable = false)
  private boolean forMember;

  @Basic(optional = false)
  @Column(nullable = false)
  private String name;

  @Column(columnDefinition="TEXT")
  @Convert(converter = AuthorizationRuleConverter.class)
  private List<List<AuthorizationCriterion>> rules;

  @Column(columnDefinition="TEXT")
  @Convert(converter = RangeListConverter.class)
  private List<Range> beneficiaryRanges;

  @ManyToOne(fetch = FetchType.EAGER)
  private Location location;

  @Basic(optional = false)
  @Column(nullable = false)
  private DistVisitProgStatus status;

  @Column
  private String statusReason;

  @Override
  public ODKEntity getPersistenceEntity(EntityManager em) {
    ODKEntity tmp = location.getPersistenceEntity(em);
    if(tmp instanceof Location) {
      location = (Location) tmp;
    } else {
      throw new IllegalArgumentException("Unable to cast persistence entity for Location");
    }
    return super.getPersistenceEntity(em);
  }

  public XlsxForm getCustomVisitForm() {
    return XlsxForm.convertJsonStringToXlsxForm(customVisitFormStr);
  }

  public void setCustomVisitForm(XlsxForm customVisitForm) {
    if(customVisitForm == null) {
      this.customVisitFormStr = null;
    } else {
      this.customVisitFormStr = customVisitForm.processToDBString();
    }
  }

  public Instant getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Instant dateCreated) {
    this.dateCreated = dateCreated;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isForMember() {
    return forMember;
  }

  public void setForMember(boolean forMember) {
    this.forMember = forMember;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public DistVisitProgStatus getStatus() {
    return status;
  }

  public void setStatus(DistVisitProgStatus status) {
    this.status = status;
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
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
