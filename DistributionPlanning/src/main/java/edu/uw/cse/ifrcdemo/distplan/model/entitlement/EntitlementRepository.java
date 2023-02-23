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

package edu.uw.cse.ifrcdemo.distplan.model.entitlement;

import edu.uw.cse.ifrcdemo.distplan.entity.Authorization;
import edu.uw.cse.ifrcdemo.distplan.entity.DisabledEntitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.EnabledEntitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement;
import edu.uw.cse.ifrcdemo.distplan.entity.Entitlement_;
import edu.uw.cse.ifrcdemo.distplan.model.AbstractOdkRepository;
import edu.uw.cse.ifrcdemo.distplan.model.csv.CsvRepository;
import edu.uw.cse.ifrcdemo.sharedlib.consts.GenConsts;
import edu.uw.cse.ifrcdemo.sharedlib.consts.csv.MobileDbConsts;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.EntitlementStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvBeneficiaryEntity;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvEntitlement;
import edu.uw.cse.ifrcdemo.sharedlib.model.row.CsvIndividual;
import edu.uw.cse.ifrcdemo.translations.PrintOutStr;
import org.hibernate.annotations.QueryHints;
import org.opendatakit.aggregate.odktables.rest.TableConstants;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.beans.PropertyChangeSupport;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public class EntitlementRepository extends AbstractOdkRepository<Entitlement> {
  // TODO: make this more flexible
  private static final int BULK_INSERT_BATCH_SIZE = 20;

  private final PropertyChangeSupport pcs;

  public PropertyChangeSupport getPcs() {
    return pcs;
  }

  public EntitlementRepository(EntityManagerFactory emf) {
    super(emf);
    this.pcs = new PropertyChangeSupport(this);
  }

  public CompletableFuture<Void> bulkSaveEntitlement(List<? extends Entitlement> entitlements) {
    return CompletableFuture.runAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      try {
        for (int i = 0; i < entitlements.size(); i++) {
        entitlements.get(i).getPersistenceEntity(em);

        // flush the batch to release memory
        if (i > 0 && i % BULK_INSERT_BATCH_SIZE == 0) {
          em.flush();
          em.clear();
        }
      }

      em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }
    });
  }

  // TODO: clean up
  public CompletableFuture<Void> updateOrInsertFromCsv(CsvRepository csvRepository) {
    return CompletableFuture.runAsync(() -> {
      List<CsvEntitlement> entitlements = csvRepository
          .readTypedCsv(CsvEntitlement.class)
          .orElseThrow(IllegalStateException::new);

      List<CsvBeneficiaryEntity> csvBeneficiaryEntities = csvRepository
          .readTypedCsv(CsvBeneficiaryEntity.class)
          .orElseThrow(IllegalStateException::new);

      List<CsvIndividual> csvIndividuals = csvRepository
          .readTypedCsv(CsvIndividual.class)
          .orElseThrow(IllegalStateException::new);

      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      try {
        for (int i = 0; i < entitlements.size(); i++) {
          CsvEntitlement csvEntitlement = entitlements.get(i);

          Entitlement entitlement = csvEntitlement.getStatus() == EntitlementStatus.ENABLED ?
              new EnabledEntitlement() :
              new DisabledEntitlement();

          entitlement.setRowId(csvEntitlement.getRowId());
          entitlement.setDateCreated(Instant.ofEpochMilli(TableConstants.milliSecondsFromNanos(
              csvEntitlement.getDateCreated(), Locale.ROOT)));
          entitlement.setOverride(Boolean.parseBoolean(csvEntitlement.getIsOverride()));
          entitlement.setAssignedItemPackCode(csvEntitlement.getAssignedItemPackCode());

          try {
            entitlement.setAuthorization(em
                .createQuery("SELECT a FROM Authorization a WHERE a.rowId = :rowId", Authorization.class)
                .setParameter(MobileDbConsts.ROW_ID, csvEntitlement.getAuthorizationId())
                .getSingleResult()
            );
          } catch (Exception e) {
            // this entitlement is invalid
            continue;
          }

          entitlement.setBeneficiaryUnitRcId(csvEntitlement.getBeneficiaryEntityId());
          entitlement.setMemberRcId(csvEntitlement.getMemberId());

          entitlement.setStatus(csvEntitlement.getStatus());
          entitlement.setStatusReason(csvEntitlement.getStatusReason());

          entitlement.setCreateUser(csvEntitlement.getCreateUser());
          entitlement.setGroupReadOnly(csvEntitlement.getRowFilterScope().getGroupReadOnly());

          try {
            Long id = em
                .createQuery("SELECT e.id FROM Entitlement e WHERE e.rowId = :rowId", Long.class)
                .setParameter(MobileDbConsts.ROW_ID, csvEntitlement.getRowId())
                .getSingleResult();

            // if status is changed, move from one table to another
            // is there a better way to do this?
            Entitlement dbEntitlement = em.find(Entitlement.class, id);

            entitlement.setBeneficiaryEntityId(dbEntitlement.getBeneficiaryEntityId());
            entitlement.setIndividualId(dbEntitlement.getIndividualId());

            if (dbEntitlement.getStatus() != csvEntitlement.getStatus()) {
              em.remove(dbEntitlement);
              em.flush();
            } else {
              entitlement.setId(id);
            }

            if (entitlement.getCreateUser() != null &&
                !entitlement.getCreateUser().equals(dbEntitlement.getCreateUser())) {
              System.out.println(PrintOutStr.UPDATING_ENTITLEMENT_CREATE_USER_FROM_CSV_IS_DIFFERENT_FROM_CREATE_USER_IN_THE_DATABASE);
            }
          } catch (NoResultException e) {
            // this is a new entitlement, it has to be an override
            entitlement.setOverride(true);

            // lookup these 2 ids from the CSV
            // this should only be done for new entitlements (overrides)

            String beneficiaryEntityRowId = csvBeneficiaryEntities
                .stream()
                .filter(x -> x
                    .getBeneficiaryEntityId()
                    .equals(csvEntitlement.getBeneficiaryEntityId())
                )
                .findAny()
                .map(CsvBeneficiaryEntity::getRowId)
                .orElse(GenConsts.EMPTY_STRING);

            String individualId = csvIndividuals
                .stream()
                .filter(x -> x.getBeneficiaryEntityRowId().equals(beneficiaryEntityRowId) &&
                    x.getMemberId().equals(csvEntitlement.getMemberId())
                )
                .findAny()
                .map(CsvIndividual::getRowId)
                .orElse(GenConsts.EMPTY_STRING);

            entitlement.setBeneficiaryEntityId(beneficiaryEntityRowId);
            entitlement.setIndividualId(individualId);
          }

          entitlement.getPersistenceEntity(em);

          // flush the batch to release memory
          if (i > 0 && i % BULK_INSERT_BATCH_SIZE == 0) {
            em.flush();
            em.clear();
          }
        }

        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }
    });
  }

  public CompletableFuture<List<Entitlement>> getEntitlements() {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Entitlement> entitlements;
      try {
        entitlements = em
            .createQuery("SELECT e FROM Entitlement e", Entitlement.class)
            .setHint(QueryHints.READ_ONLY, true)
            .getResultList();

        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return entitlements;
    });
  }

  public CompletableFuture<List<Entitlement>> getEntitlements(Authorization authorization) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Entitlement> entitlements;
      try {
        entitlements = em
            .createQuery("SELECT e FROM Entitlement e WHERE e.authorization = :auth", Entitlement.class)
            .setParameter("auth", authorization)
            .setHint(QueryHints.READ_ONLY, true)
            .getResultList();

        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return entitlements;
    });
  }

  public Optional<Entitlement> findEntitlement(Authorization authorization, String memberId, String beneficiaryUnitId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<Entitlement> resultList;
    try {
      resultList = em
          .createQuery("SELECT e FROM Entitlement e " +
              "WHERE e.authorization = :auth " +
              "AND e.individualId = :memberId " +
              "AND e.beneficiaryEntityId = :beneficiaryUnitId", Entitlement.class)
          .setParameter("auth", authorization)
          .setParameter("memberId", memberId)
          .setParameter("beneficiaryUnitId", beneficiaryUnitId)
          .setMaxResults(1)
          .getResultList();

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }

    return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
  }

  public Optional<Entitlement> findEntitlement(Authorization authorization, String rcId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<Entitlement> resultList;
    try {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<Entitlement> query = builder.createQuery(Entitlement.class);
      Root<Entitlement> root = query.from(Entitlement.class);

      SingularAttribute<Entitlement, String> attribute = authorization.getForIndividual() ?
          Entitlement_.memberRcId :
          Entitlement_.beneficiaryUnitRcId;

      resultList = em
          .createQuery(query
              .where(builder.equal(root.get(attribute), rcId))
              .where(builder.equal(root.get(Entitlement_.authorization), authorization))
          )
          .getResultList();

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }

    return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
  }
}
