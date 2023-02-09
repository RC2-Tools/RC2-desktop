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

package edu.uw.cse.ifrcdemo.healthplan.model;

import edu.uw.cse.ifrcdemo.healthplan.entity.Donor;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class DonorRepository extends AbstractOdkRepository<Donor> {
    public static final String DONOR_ADD_EVT = "AddDonor";

    private final PropertyChangeSupport pcs;

    public PropertyChangeSupport getPcs() {
        return pcs;
    }

    public DonorRepository(EntityManagerFactory emf) {
        super(emf);
        this.pcs = new PropertyChangeSupport(this);
    }

    public CompletableFuture<Donor> saveDonor(Donor donor) {
        return CompletableFuture.supplyAsync(() -> {
            getPcs().firePropertyChange(DONOR_ADD_EVT, null, donor);
            return saveEntity(donor);
        });
    }

    public CompletableFuture<Donor> getDonor(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            EntityManager em = getEmf().createEntityManager();
            EntityTransaction tx = null;
            Donor donor = null;

            try {
                tx = em.getTransaction();
                tx.begin();

                donor = em.find(Donor.class, id);

                em.getTransaction().commit();

            } catch (RuntimeException e) {
                if (tx != null && tx.isActive()) { tx.rollback(); }
                throw (e);
            } finally {
                em.close();
            }

            return donor;
        });
    }

    public Donor getDonorByRowId(String rowId) {
        EntityManager em = getEmf().createEntityManager();
        EntityTransaction tx = null;
        Donor donor = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            donor = em.createQuery("SELECT i FROM Donor i WHERE i.rowId = :rowId", Donor.class)
                    .setParameter("rowId", rowId).getSingleResult();

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) { tx.rollback(); }
            throw (e);
        } finally {
            em.close();
        }

        return donor;
    }

    public List<Donor> getDonorList() {
        EntityManager em = getEmf().createEntityManager();
        EntityTransaction tx = null;
        List<Donor> donors = null;
        try {
            em = getEmf().createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            donors = em
                    .createQuery("SELECT d FROM Donor d ORDER BY d.name ASC", Donor.class).getResultList();

            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) { tx.rollback(); }
            throw (e);
        } finally {
            em.close();
        }


        return donors;
    }

    public void editDonor(String rowId, String name, String description) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        try {
            em
                    .createQuery("UPDATE Donor d " +
                            "SET d.name = :name, d.description = :desc " +
                            "WHERE d.rowId = :rowId"
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
