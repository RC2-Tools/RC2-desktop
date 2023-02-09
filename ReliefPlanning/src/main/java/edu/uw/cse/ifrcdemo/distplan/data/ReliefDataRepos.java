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

package edu.uw.cse.ifrcdemo.distplan.data;

import edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization.AuthorizationRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.model.visitprogram.VisitProgramRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.data.AbsDataRepos;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.item.ItemRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.preference.PreferencesStore;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.rctemplate.RcTemplateRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.location.LocationRepository;
import edu.uw.cse.ifrcdemo.sharedlib.model.config.AuxiliaryProperty;

import javax.persistence.EntityManagerFactory;

public class ReliefDataRepos extends AbsDataRepos {

//    private final AuthorizationRepository authorizationRepository;
//    private final DistributionRepository distributionRepository;
//    private final EntitlementRepository entitlementRepository;
    private final VisitProgramRepository visitProgramRepository;

    public ReliefDataRepos(String profile, EntityManagerFactory emf,
                           AuthorizationRepository authorizationRepository,
                           AuxiliaryProperty auxiliaryProperty,
                           CsvRepository csvRepository,
                           DistributionRepository distributionRepository,
                           EntitlementRepository entitlementRepository,
                           ItemRepository itemRepository,
                           RcTemplateRepository rcTemplateRepository,
                           LocationRepository locationRepository,
                           VisitProgramRepository visitProgramRepository,
                           PreferencesStore preferencesStore) {
        super(profile, emf, csvRepository, rcTemplateRepository, locationRepository, itemRepository, authorizationRepository, distributionRepository, entitlementRepository,
                preferencesStore, auxiliaryProperty);
//        this.authorizationRepository = authorizationRepository;
//        this.distributionRepository = distributionRepository;
//        this.entitlementRepository = entitlementRepository;
        this.visitProgramRepository = visitProgramRepository;
    }

//    public AuthorizationRepository getAuthorizationRepository() {
//        return authorizationRepository;
//    }
//
//    public DistributionRepository getDistributionRepository() {
//        return distributionRepository;
//    }
//
//    public EntitlementRepository getEntitlementRepository() {
//        return entitlementRepository;
//    }

    public VisitProgramRepository getVisitProgramRepository() {
        return visitProgramRepository;
    }

}
