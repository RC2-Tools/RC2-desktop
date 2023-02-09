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

package edu.uw.cse.ifrcdemo.planningsharedlib.model.location;

import edu.uw.cse.ifrcdemo.planningsharedlib.entity.Location;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocationRepository extends AbstractOdkRepository<Location> {

  public LocationRepository(EntityManagerFactory emf) {
    super(emf);
  }

  public CompletableFuture<Void> saveLocation(Location location) {
    return CompletableFuture.runAsync(() -> {
        saveEntity(location);
    });
  }

  public CompletableFuture<Location> getLocation(Long id) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      em.getTransaction().begin();

      Location location;
      try {
        location = em.find(Location.class, id);

        em.getTransaction().commit();
      } catch (RuntimeException e) {
        if (em.getTransaction().isActive()) {
          em.getTransaction().rollback();
        }
        throw e;
      } finally {
        em.close();
      }

      return location;
    });
  }

  public List<Location> getAllLocations() {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    List<Location> locations;
    try {
      locations = em
          .createQuery("SELECT r FROM Location r ORDER BY r.name ASC", Location.class)
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

    return locations;
  }

  public Location getLocationByRowId(String rowId) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    Location location;
    try {
      location = em
          .createQuery("SELECT r FROM Location r WHERE r.rowId = :rowId", Location.class)
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

    return location;
  }

  public void editLocation(String rowId, String name, String description) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    try {
      em
          .createQuery("UPDATE Location location " +
              "SET location.name = :name, location.description = :desc " +
              "WHERE location.rowId = :rowId"
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
