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

package edu.uw.cse.ifrcdemo.healthplan.entity;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.ODKEntity;
import edu.uw.cse.ifrcdemo.planningsharedlib.persistence.InstantConverter;
import java.time.Instant;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.PrePersist;

@Entity
public class HealthTask extends ODKEntity {
    @Column(nullable = false, unique = true)
    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false)
    @Convert(converter = InstantConverter.class)
    private Instant dateCreated;

    @Column
    private String description;

    @Column
    private String startDate;

    @Column
    private String endDate;

    @Column
    private String status;

    @Column
    private String statusReason;

    @Column
    private String locationId;

    @Column
    private String locationName;

    @Column
    private String summaryTableId;

    @Column
    private String summaryFormId;

    @Column
    private String summaryFormVersion;

    @Column
    private String distributionId;

    @Column
    private String funderId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getSummaryTableId() {
        return summaryTableId;
    }

    public void setSummaryTableId(String summaryTableId) {
        this.summaryTableId = summaryTableId;
    }

    public String getSummaryFormId() {
        return summaryFormId;
    }

    public void setSummaryFormId(String summaryFormId) {
        this.summaryFormId = summaryFormId;
    }

    public String getSummaryFormVersion() {
        return summaryFormVersion;
    }

    public void setSummaryFormVersion(String summaryFormVersion) {
        this.summaryFormVersion = summaryFormVersion;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
    }

    public String getFunderId() {
        return funderId;
    }

    public void setFunderId(String funderId) {
        this.funderId = funderId;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
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
