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

package edu.uw.cse.ifrcdemo.distplan.data;

import edu.uw.cse.ifrcdemo.distplan.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.itempack.ItemPackRepository;
import edu.uw.cse.ifrcdemo.distplan.model.preference.PreferencesStore;
import edu.uw.cse.ifrcdemo.distplan.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.distplan.model.region.RegionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;

import javax.persistence.EntityManagerFactory;
import java.util.UUID;

public class DataRepos {

    private final EntityManagerFactory emf;
    private final AuthorizationRepository authorizationRepository;
    private final AuxiliaryProperty auxiliaryProperty;
    private final CsvRepository csvRepository;
    private final DistributionRepository distributionRepository;
    private final EntitlementRepository entitlementRepository;
    private final ItemPackRepository itemPackRepository;
    private final PreferencesStore preferencesStore;
    private final RcTemplateRepository rcTemplateRepository;
    private final RegionRepository regionRepository;
    private final VisitProgramRepository visitProgramRepository;
    private final String currentProfile;
    private final UUID version;

    public DataRepos(String profile, EntityManagerFactory emf,
                     AuthorizationRepository authorizationRepository,
                     AuxiliaryProperty auxiliaryProperty,
                     CsvRepository csvRepository,
                     DistributionRepository distributionRepository,
                     EntitlementRepository entitlementRepository,
                     ItemPackRepository itemPackRepository,
                     RcTemplateRepository rcTemplateRepository,
                     RegionRepository regionRepository,
                     VisitProgramRepository visitProgramRepository,
                     PreferencesStore preferencesStore) {
        this.currentProfile = profile;
        this.emf = emf;
        this.authorizationRepository = authorizationRepository;
        this.auxiliaryProperty = auxiliaryProperty;
        this.csvRepository = csvRepository;
        this.distributionRepository = distributionRepository;
        this.entitlementRepository = entitlementRepository;
        this.itemPackRepository = itemPackRepository;
        this.rcTemplateRepository = rcTemplateRepository;
        this.regionRepository = regionRepository;
        this.visitProgramRepository = visitProgramRepository;
        this.preferencesStore = preferencesStore;
        this.version = UUID.randomUUID();
    }

    public String getCurrentProfile() {
        return currentProfile;
    }

    public AuthorizationRepository getAuthorizationRepository() {
        return authorizationRepository;
    }

    public AuxiliaryProperty getAuxiliaryProperty() {
        return auxiliaryProperty;
    }

    public CsvRepository getCsvRepository() {
        return csvRepository;
    }

    public DistributionRepository getDistributionRepository() {
        return distributionRepository;
    }

    public EntitlementRepository getEntitlementRepository() {
        return entitlementRepository;
    }

    public ItemPackRepository getItemPackRepository() {
        return itemPackRepository;
    }

    public PreferencesStore getPreferencesStore() { return preferencesStore; }

    public RcTemplateRepository getRcTemplateRepository() {
        return rcTemplateRepository;
    }

    public RegionRepository getRegionRepository() {
        return regionRepository;
    }

    public VisitProgramRepository getVisitProgramRepository() {
        return visitProgramRepository;
    }

    public UUID getVersion() {return version;}

    public void close() {
        if(emf.isOpen()) {
            emf.close();
        }
    }
}
