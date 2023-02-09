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

package edu.uw.cse.ifrcdemo.planningsharedlib.model.authorization;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Authorization_;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Item;
import edu.uw.cse.ifrcdemo.planningsharedlib.entity.ODKEntity;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationStatus;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.AuthorizationType;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class DbAuthorizationRepository extends AbstractOdkRepository<Authorization> implements AuthorizationRepository  {

  private final PropertyChangeSupport pcs;

  public PropertyChangeSupport getPcs() {
    return pcs;
  }

  public DbAuthorizationRepository(EntityManagerFactory emf) {
    super(emf);
    this.pcs = new PropertyChangeSupport(this);
  }

  @Override
  public CompletableFuture<Authorization> getAuthorization(Long id) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      Authorization authorization;
      try {
        authorization = em.find(Authorization.class, id);
        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return authorization;
    });
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationStatus status) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Authorization> result;
      try {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Authorization> criteriaQuery = builder.createQuery(Authorization.class);
        Root<Authorization> authorizationRoot = criteriaQuery.from(Authorization.class);

        result = em
            .createQuery(criteriaQuery
                .where(builder.equal(
                    authorizationRoot.get(Authorization_.status),
                    status
                ))
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

      return result;
    });
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationType type) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Authorization> result;
      try {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Authorization> criteriaQuery = builder.createQuery(Authorization.class);
        Root<Authorization> authorizationRoot = criteriaQuery.from(Authorization.class);

        result = em
            .createQuery(criteriaQuery
                .where(builder.equal(
                    authorizationRoot.get(Authorization_.type),
                    type
                ))
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

      return result;
    });
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(AuthorizationStatus status,
      AuthorizationType type) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Authorization> result;
      try {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Authorization> criteriaQuery = builder.createQuery(Authorization.class);
        Root<Authorization> authorizationRoot = criteriaQuery.from(Authorization.class);

        result = em
            .createQuery(criteriaQuery
                .where(
                    builder.equal(
                        authorizationRoot.get(Authorization_.status),
                        status
                    ),
                    builder.equal(
                        authorizationRoot.get(Authorization_.type),
                        type
                    )
                )
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

      return result;
    });
  }

  @Override
  public CompletableFuture<List<Authorization>> getAuthorizations(Item item) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<Authorization> result;
      try {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Authorization> criteriaQuery = builder.createQuery(Authorization.class);
        Root<Authorization> authorizationRoot = criteriaQuery.from(Authorization.class);

        result = em
            .createQuery(criteriaQuery
                .where(builder.equal(
                    authorizationRoot.get(Authorization_.item),
                        item
                ))
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

      return result;
    });
  }

  @Override
  public CompletableFuture<Authorization> saveAuthorization(Authorization authorization) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      try {
        if (authorization.getItemRanges() != null) {
          authorization.setItemRanges(new ArrayList<>(authorization.getItemRanges()));
        }

        if (authorization.getVoucherRanges() != null) {
          authorization.setVoucherRanges(new ArrayList<>(authorization.getVoucherRanges()));
        }

        ODKEntity entity = authorization.getPersistenceEntity(em);
        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return authorization;
    });
  }


  @Override
  public CompletableFuture<Authorization> updateAuthorizationStatus(Long id, AuthorizationStatus status) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      Authorization authorization;
      try {
        authorization = em.find(Authorization.class, id);
        authorization.setStatus(status);
        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return authorization;
    });
  }

  @Override
  public List<XlsxForm> getAllForms() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<XlsxForm> list;
    try {
      list = em
          .createQuery("SELECT DISTINCT a.customDeliveryFormStr FROM Authorization a " +
              "WHERE a.status <> :exclude " +
              "AND a.customDeliveryFormStr IS NOT NULL", String.class)
          .setParameter("exclude", AuthorizationStatus.REMOVED)
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
