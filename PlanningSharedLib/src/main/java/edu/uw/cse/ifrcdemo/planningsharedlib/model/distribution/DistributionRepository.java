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

package edu.uw.cse.ifrcdemo.planningsharedlib.model.distribution;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Distribution_;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DistributionRepository extends AbstractOdkRepository<Distribution> {

  public DistributionRepository(EntityManagerFactory emf) {
    super(emf);
  }

  public Distribution saveDistribution(Distribution distribution) {
    return saveEntity(distribution);
  }

  public List<DistListDto> getDistributionListDto(boolean removed) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<DistListDto> list;
    try {
      String queryName = removed ? "distListDtoRemoved" : "distListDto";

      list = em
          .createNamedQuery(queryName, DistListDto.class)
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

    return list;
  }

  public Distribution updateDistributionStatus(Long id, DistVisitProgStatus status) {
    EntityManager em = getEmf().createEntityManager();
    
    EntityTransaction entityTransaction = em.getTransaction();
    entityTransaction.begin();

    Distribution distribution;
    try {
      distribution = em.find(Distribution.class, id);
      distribution.setStatus(status);

      List<Authorization> auths = distribution.getAuthorizations();
      for (Authorization auth : auths) {
        if (status == DistVisitProgStatus.ACTIVE) {
          auth.setStatus(AuthorizationStatus.ACTIVE);
        } else if (status == DistVisitProgStatus.INACTIVE) {
          auth.setStatus(AuthorizationStatus.INACTIVE);
        } else if (status == DistVisitProgStatus.DISABLED) {
          auth.setStatus(AuthorizationStatus.DISABLED);
        } else if (status == DistVisitProgStatus.REMOVED) {
          auth.setStatus(AuthorizationStatus.REMOVED);
        } else {
          entityTransaction.rollback();
          throw new IllegalStateException("Somehow we got a DistributionStatus that does not exist");
        }
      }

      entityTransaction.commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }

    return distribution;
  }

  public CompletableFuture<List<Distribution>> getDistributions(DistVisitProgStatus status) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Distribution> result;
      try {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Distribution> criteriaQuery = builder.createQuery(Distribution.class);
        Root<Distribution> distributionRoot = criteriaQuery.from(Distribution.class);

        result = em.createQuery(
            criteriaQuery.where(builder.equal(distributionRoot.get(Distribution_.status), status)))
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

      return result;
    });
  }

  public Distribution getDistributionByRowId(String rowId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    Distribution distribution;
    try {
      distribution = em
          .createQuery("SELECT d FROM Distribution d WHERE d.rowId = :rowId", Distribution.class)
          .setParameter("rowId", rowId)
          .getSingleResult();

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }

    return distribution;
  }

  public List<Distribution> getAllDistributions() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<Distribution> list;
    try {
      list = em
          .createQuery("SELECT d FROM Distribution d", Distribution.class)
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

    return list;
  }

  public List<XlsxForm> getAllForms() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<XlsxForm> list;
    try {
      list = em
          .createQuery("SELECT DISTINCT d.summaryFormStr FROM Distribution d " +
              "WHERE d.status <> :exclude " +
              "AND d.status IS NOT NULL " +
              "AND d.summaryFormStr IS NOT NULL", String.class)
          .setParameter("exclude", DistVisitProgStatus.REMOVED)
          .getResultStream()
          .map(XlsxForm::convertJsonStringToXlsxForm)
          .collect(Collectors.toList());

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }

    return list;
  }
}
