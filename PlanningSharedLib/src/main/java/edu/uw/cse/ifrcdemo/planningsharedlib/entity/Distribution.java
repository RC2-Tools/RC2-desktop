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

import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.InstantConverter;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.List;

@Entity
@NamedQuery(
    name = "distListDto",
    query = "SELECT NEW " +
        "edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistListDto(d.id, d.rowId, d.name, d.status) " +
        "FROM Distribution d " +
        "WHERE d.status != edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus.REMOVED " +
        "ORDER BY d.name ASC"
)
@NamedQuery(
    name = "distListDtoRemoved",
    query = "SELECT NEW " +
        "edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistListDto(d.id, d.rowId, d.name, d.status) " +
        "FROM Distribution d " +
        "WHERE d.status = edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus.REMOVED " +
        "ORDER BY d.name ASC"
)
public class Distribution extends ODKEntity {
  @Transient
  private final Object authPersistLock = new Object();

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "distribution", fetch = FetchType.EAGER)
  private List<Authorization> authorizations;

  @Basic(optional = false)
  @Column(nullable = false)
  @Convert(converter = InstantConverter.class)
  private Instant dateCreated;

  @Column
  private String description;

  @ManyToOne(optional = false)
  @JoinColumn(nullable = false)
  private Location location;

  @Basic(optional = false)
  @Column(nullable = false)
  private String name;

  @Basic(optional = false)
  @Column(nullable = false)
  private DistVisitProgStatus status;

  @Column
  private String statusReason;

  @Column
  private String summaryFormStr;

  @Column
  private String summaryVersion;

  public List<Authorization> getAuthorizations() {
    synchronized(authPersistLock) {
      return authorizations;
    }
  }

  public void setAuthorizations(List<Authorization> authorizations) {
    synchronized (authPersistLock) {
      this.authorizations = authorizations;
      this.authorizations.forEach(authorization -> {
        if (authorization.getDistribution() != this) {
          authorization.setDistribution(this);
        }
      });
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

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public XlsxForm getSummaryForm() {
    return XlsxForm.convertJsonStringToXlsxForm(summaryFormStr);
  }

  public void setSummaryForm(XlsxForm summaryForm) {
    if(summaryForm == null) {
      this.summaryFormStr = null;
    } else {
      this.summaryFormStr = summaryForm.processToDBString();
    }
  }

  public String getSummaryVersion() {
    return summaryVersion;
  }

  public void setSummaryVersion(String summaryVersion) {
    this.summaryVersion = summaryVersion;
  }

  @Override
  public ODKEntity getPersistenceEntity(EntityManager em) {

    synchronized (authPersistLock) {
      for (int i = 0; i < authorizations.size(); i++) {
        Authorization tmpAuth = authorizations.get(i);
        ODKEntity tmp = tmpAuth.getPersistenceEntity(em);
        if (tmp instanceof Authorization) {
          authorizations.set(i, (Authorization) tmp);
        } else {
          throw new IllegalArgumentException("Unable to cast persistence entity for Authorization");
        }
      }
    }

    ODKEntity tmp = location.getPersistenceEntity(em);
    if(tmp instanceof Location) {
      location = (Location) tmp;
    } else {
      throw new IllegalArgumentException("Unable to cast persistence entity for Location");
    }

    return super.getPersistenceEntity(em);
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
