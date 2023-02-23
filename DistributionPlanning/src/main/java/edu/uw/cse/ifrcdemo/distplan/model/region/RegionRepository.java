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

package edu.uw.cse.ifrcdemo.distplan.model.region;

import edu.uw.cse.ifrcdemo.distplan.entity.Region;
import edu.uw.cse.ifrcdemo.distplan.model.AbstractOdkRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RegionRepository extends AbstractOdkRepository<Region> {

  public RegionRepository(EntityManagerFactory emf) {
    super(emf);
  }

  public CompletableFuture<Void> saveRegion(Region region) {
    return CompletableFuture.runAsync(() -> {
        saveEntity(region);
    });
  }

  public CompletableFuture<Region> getRegion(Long id) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      Region region;
      try {
        region = em.find(Region.class, id);

        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return region;
    });
  }

  public List<Region> getAllRegion() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<Region> regions;
    try {
      regions = em
          .createQuery("SELECT r FROM Region r ORDER BY r.name ASC", Region.class)
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

    return regions;
  }

  public Region getRegionByRowId(String rowId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    Region region;
    try {
      region = em
          .createQuery("SELECT r FROM Region r WHERE r.rowId = :rowId", Region.class)
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

    return region;
  }

  public void editRegion(String rowId, String name, String description) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    try {
      em
          .createQuery("UPDATE Region region " +
              "SET region.name = :name, region.description = :desc " +
              "WHERE region.rowId = :rowId"
          )
          .setParameter("name", name)
          .setParameter("desc", description)
          .setParameter("rowId", rowId)
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
