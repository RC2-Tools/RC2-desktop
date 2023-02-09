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

package edu.uw.cse.ifrcdemo.distplan.model.distribution;

import edu.uw.cse.ifrcdemo.distplan.data.ReliefDataInstance;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.EnabledEntitlement;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.logic.EntitlementFilter;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.entitlement.EntitlementRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.range.Range;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.util.RangeUtil;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.sharedlib.model.stub.Individual;
import edu.uw.cse.ifrcdemo.sharedlib.util.StringUtil;
import edu.uw.cse.ifrcdemo.translations.LogStr;
import edu.uw.cse.ifrcdemo.translations.TranslationConsts;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DistExporter {
  private final Logger logger;

  public DistExporter(Logger logger) {
    this.logger = logger;
  }

  public void exportDistribution(@NonNull Distribution distribution) {
    if (DistVisitProgStatus.ACTIVE != distribution.getStatus()) {
      logger.warn("Distribution {} is not {}", distribution::getRowId, DistVisitProgStatus.ACTIVE::name);
      return;
    }
    EntitlementRepository entitlementRepository = ReliefDataInstance.getDataRepos().getEntitlementRepository();

    for (Authorization authorization : distribution.getAuthorizations()) {
      List<Entitlement> updateOrInsert;
      if (AuthorizationType.REQUIRED_REGISTRATION == authorization.getType()) {
        updateOrInsert = processCriteriaAuth(authorization);
      } else if (AuthorizationType.OPTIONAL_REGISTRATION == authorization.getType()) {
        updateOrInsert = processVoucherAuth(authorization);
      } else {
        continue;
      }

      entitlementRepository
          .bulkSaveEntitlement(updateOrInsert)
          .join();
    }
  }

  private List<Entitlement> processCriteriaAuth(@NonNull Authorization authorization) {
    CsvRepository csvRepository = ReliefDataInstance.getDataRepos().getCsvRepository();
    EntitlementRepository entitlementRepository = ReliefDataInstance.getDataRepos().getEntitlementRepository();

    List<String> entitledIds = EntitlementFilter
        .filter(csvRepository, authorization.getRules(), authorization.getForIndividual())
        .collect(Collectors.toList());

    if (!isCriteriaRangeValid(authorization, entitledIds.size())) {
      // TODO:
      throw new RuntimeException(TranslationConsts.TOO_FEW_DELIVERABLES_ERROR);
    }

    PrimitiveIterator.OfLong unusedCodesItr = null;
    if (authorization.isAssignItemCode()) {
      unusedCodesItr = getUnusedItemCodeItr(authorization);
    }

    Map<String, CsvIndividual> indexedCsvMember = csvRepository
        .readIndexedTypedCsv(CsvIndividual.class)
        .orElseThrow(IllegalStateException::new);

    Map<String, CsvBeneficiaryEntity> indexedCsvBeneficiaryUnit = csvRepository
        .readIndexedTypedCsv(CsvBeneficiaryEntity.class)
        .orElseThrow(IllegalStateException::new);

    LinkedList<Entitlement> newOrUpdatedEnt = new LinkedList<>();
    for (String entitledMemberId : entitledIds) {
      Individual entitledMember = indexedCsvMember.get(entitledMemberId);
      String entitledMemberBenUnitId = entitledMember.getBeneficiaryEntityRowId();
      CsvBeneficiaryEntity entitledBenUnit = indexedCsvBeneficiaryUnit.get(entitledMemberBenUnitId);

      // TODO: clean up

      Entitlement entitlement = entitlementRepository
          .findEntitlement(authorization, entitledMemberId, entitledMemberBenUnitId)
          .orElseGet(EnabledEntitlement::new);

      entitlement.setAuthorization(authorization);
      entitlement.setBeneficiaryEntityId(entitledMemberBenUnitId);
      entitlement.setIndividualId(entitledMemberId);
      entitlement.setOverride(false);
      entitlement.setMemberRcId(entitledMember.getMemberId());
      entitlement.setBeneficiaryUnitRcId(entitledBenUnit.getBeneficiaryEntityId());
      entitlement.setGroupReadOnly(entitledBenUnit.getRowFilterScope().getGroupModify());

      // TODO: disable entitlements that are not linked to a valid member/beneficiary id
      // TODO: and re-enable the now valid ones
      // TODO: should this take into account the beneficiary's enabled/disabled status?

      if (authorization.isAssignItemCode() &&
          StringUtil.isNullOrEmpty(entitlement.getAssignedItemCode())) {
        if (unusedCodesItr.hasNext()) {
          entitlement.setAssignedItemCode(unusedCodesItr.next().toString());
        } else {
          // TODO: how can this happen??
          logger.warn(LogStr.LOG_ITEM_PACK_CODE_EXHAUSTED_ENTITLEMENT_AUTHORIZATION,
              entitlement::getRowId, entitlement.getAuthorization()::getRowId);
        }
      }

      newOrUpdatedEnt.add(entitlement);
    }

    return newOrUpdatedEnt;
  }

  private List<Entitlement> processVoucherAuth(@NonNull Authorization authorization) {
    if (!isVoucherRangeValid(authorization)) {
      // TODO:
      throw new RuntimeException("Voucher range too narrow");
    }


    EntitlementRepository entitlementRepository = ReliefDataInstance.getDataRepos().getEntitlementRepository();

    PrimitiveIterator.OfLong unusedCodesItr = authorization.isAssignItemCode() ?
        getUnusedItemCodeItr(authorization) : null;

    return RangeUtil
        .toLongStream(authorization.getVoucherRanges())
        .mapToObj(Long::toString)
        .map(rcId -> {
          Entitlement entitlement = entitlementRepository
              .findEntitlement(authorization, rcId)
              .orElseGet(EnabledEntitlement::new);

          entitlement.setAuthorization(authorization);
          entitlement.setBeneficiaryEntityId(rcId);
          entitlement.setOverride(false);

          if (authorization.getForIndividual()) {
            entitlement.setMemberRcId(rcId);
          } else {
            entitlement.setBeneficiaryUnitRcId(rcId);
          }

          if (authorization.isAssignItemCode() &&
              StringUtil.isNullOrEmpty(entitlement.getAssignedItemCode())) {
            if (unusedCodesItr.hasNext()) {
              entitlement.setAssignedItemCode(unusedCodesItr.next().toString());
            } else {
              logger.warn(LogStr.LOG_ITEM_PACK_CODE_EXHAUSTED_ENTITLEMENT_AUTHORIZATION,
                  entitlement::getRowId, entitlement.getAuthorization()::getRowId);
            }
          }

          return entitlement;
        })
        .collect(Collectors.toCollection(LinkedList::new));
  }

  private boolean isCriteriaRangeValid(@NonNull Authorization authorization, int entitlementCount) {
    return !authorization.isAssignItemCode() ||
        RangeUtil.getTotalCount(authorization.getItemRanges()) >= entitlementCount;
  }

  private boolean isVoucherRangeValid(@NonNull Authorization authorization) {
    return !authorization.isAssignItemCode() ||
        RangeUtil.getTotalCount(authorization.getItemRanges()) >=
            RangeUtil.getTotalCount(authorization.getVoucherRanges());
  }

  private PrimitiveIterator.OfLong getUnusedItemCodeItr(Authorization authorization) {
    EntitlementRepository entitlementRepository = ReliefDataInstance.getDataRepos().getEntitlementRepository();
    return entitlementRepository
        .getEntitlements(authorization)
        .thenApplyAsync(list -> getUsedCodeSet(list.stream()))
        .thenApplyAsync(usedCodes -> filterCodesForUnused(usedCodes, authorization.getItemRanges()))
        .join();
  }

  private Set<Long> getUsedCodeSet(@NonNull Stream<Entitlement> stream) {
    return stream
        .map(Entitlement::getAssignedItemCode)
        .filter(StringUtil::isNotNullAndNotEmpty)
        .map(Long::valueOf)
        .collect(Collectors.toSet());
  }

  private PrimitiveIterator.OfLong filterCodesForUnused(Set<Long> usedCodes, List<Range> fullRange) {
    return RangeUtil
        .toLongStream(fullRange)
        .filter(x -> !usedCodes.contains(x))
        .sorted()
        .iterator();
  }
}
