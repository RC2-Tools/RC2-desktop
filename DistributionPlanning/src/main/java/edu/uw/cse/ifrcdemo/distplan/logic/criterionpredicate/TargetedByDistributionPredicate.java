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

package edu.uw.cse.ifrcdemo.distplan.logic.criterionpredicate;

import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.AuthorizationCriterion;
import edu.uw.cse.ifrcdemo.distplan.model.criteria.CriterionOperator;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.distplan.model.distribution.DistributionRepository;
import edu.uw.cse.ifrcdemo.distplan.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.distplan.util.BeneficiaryUtil;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvDelivery;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.UntypedSyncRow;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TargetedByDistributionPredicate implements Predicate<UntypedSyncRow> {
  private final DistributionRepository distributionRepository;
  private final EntitlementRepository entitlementRepository;

  private final AuthorizationCriterion authorizationCriterion;
  private final boolean includeUnrealized;

  private final Set<String> deliveredEntitlementSet;

  public TargetedByDistributionPredicate(DistributionRepository distributionRepository,
                                         EntitlementRepository entitlementRepository,
                                         CsvRepository csvRepository,
                                         AuthorizationCriterion authorizationCriterion,
                                         boolean includeUnrealized) {
    this.distributionRepository = distributionRepository;
    this.entitlementRepository = entitlementRepository;
    this.authorizationCriterion = authorizationCriterion;
    this.includeUnrealized = includeUnrealized;

    this.deliveredEntitlementSet = csvRepository
        .readTypedCsv(CsvDelivery.class)
        .orElseThrow(IllegalStateException::new)
        .stream()
        .map(CsvDelivery::getEntitlementId)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean test(UntypedSyncRow untypedSyncRow) {
    if (untypedSyncRow == null || authorizationCriterion.getField() == null) {
      return false;
    }

    // only the EQ operator is supported
    if (authorizationCriterion.getOperator() != CriterionOperator.EQ) {
      return false;
    }

    String beneficiaryEntityRowId = BeneficiaryUtil.getFromCombinedRow(
        untypedSyncRow,
        MobileDbConsts.TableNames.INDIVIDUAL_TABLE_NAME,
        MobileDbConsts.BENEFICIARY_ENTITY_ROW_ID
    );

    String memberRowId = untypedSyncRow.getRowId();

    String distId = authorizationCriterion.getValue();
    List<Authorization> authorizations = distributionRepository.getDistributionByRowId(distId).getAuthorizations();
    for (Authorization authorization : authorizations) {
      Optional<Entitlement> entitlement = entitlementRepository.findEntitlement(authorization, memberRowId, beneficiaryEntityRowId);

      if (entitlement.isPresent()) {
        if (!includeUnrealized) {
          // check delivery to make sure the entitlement was realized
          if (deliveredEntitlementSet.contains(entitlement.get().getRowId())) {
            return true;
          }
        }

        return true;
      }
    }

    return false;
  }
}
