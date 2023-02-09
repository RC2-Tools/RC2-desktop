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

package edu.uw.cse.ifrcdemo.distplan.model.visitprogram;

import edu.uw.cse.ifrcdemo.distplan.entity.VisitProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import edu.uw.cse.ifrcdemo.sharedlib.model.datattype.DistVisitProgStatus;
import org.hibernate.annotations.QueryHints;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class VisitProgramRepository extends AbstractOdkRepository<VisitProgram> {

  public VisitProgramRepository(EntityManagerFactory emf) {
    super(emf);
  }

  public VisitProgram saveVisitProgram(VisitProgram program) {
      return saveEntity(program);
  }

  public CompletableFuture<List<VisitProgram>> getVisitPrograms(boolean includeRemoved) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      List<VisitProgram> entitlements;
      try {
        TypedQuery<VisitProgram> query;
        if (includeRemoved) {
          query = em.createQuery("SELECT p FROM VisitProgram p", VisitProgram.class);
        } else {
          query = em
              .createQuery("SELECT p FROM VisitProgram p WHERE p.status <> :status", VisitProgram.class)
              .setParameter("status", DistVisitProgStatus.REMOVED);
        }

        entitlements = query
            .setHint(QueryHints.READ_ONLY, Boolean.TRUE.toString())
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

  public List<VisitProgram> getVisitProgramsWithStatus(DistVisitProgStatus status) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<VisitProgram> entitlements;
    try {
      entitlements = em
          .createQuery("SELECT p FROM VisitProgram p WHERE p.status = :status", VisitProgram.class)
          .setParameter("status", status)
          .setHint(QueryHints.READ_ONLY, Boolean.TRUE.toString())
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
  }

  public List<VisitProgramListDto> getVisitProgramListDto() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<VisitProgramListDto> list;
    try {
      list = em
          .createNamedQuery("vpListDto", VisitProgramListDto.class)
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

  public VisitProgram getVisitProgramByRowId(String rowId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    VisitProgram visitProgram;
    try {
      visitProgram = em
          .createQuery("SELECT vp FROM VisitProgram vp WHERE vp.rowId = :rowId", VisitProgram.class)
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

    return visitProgram;
  }

  public List<XlsxForm> getAllForms() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<XlsxForm> list;
    try {
      list = em
          .createQuery("SELECT DISTINCT vp.customVisitFormStr FROM VisitProgram vp " +
              "WHERE vp.customVisitFormStr IS NOT NULL", String.class)
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

  public void updateVisitProgramStatus(String rowId, DistVisitProgStatus status) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    try {
      em
          .createQuery("UPDATE VisitProgram vp SET vp.status = :status WHERE vp.rowId = :rowId")
          .setParameter("rowId", rowId)
          .setParameter("status", status)
          .executeUpdate();

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
      }
      throw e;
    } finally {
      em.close();
    }
  }
}
