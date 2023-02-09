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

import edu.uw.cse.ifrcdemo.healthplan.entity.ServicesForProgram;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

public class HealthServicesForTaskRespository extends AbstractOdkRepository<ServicesForProgram> {


    public HealthServicesForTaskRespository(EntityManagerFactory emf) {
        super(emf);
    }

    public CompletableFuture<Void> saveHealthServiceForTask(ServicesForProgram hsft) {
        return CompletableFuture.runAsync(() -> {
            saveEntity(hsft);
        });
    }

    public CompletableFuture<ServicesForProgram> getHealthServiceForTask(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            EntityManager em = getEmf().createEntityManager();
            EntityTransaction tx = null;
            ServicesForProgram hsft = null;

            try {
                tx = em.getTransaction();
                tx.begin();

                hsft = em.find(ServicesForProgram.class, id);

                em.getTransaction().commit();

            } catch (RuntimeException e) {
                if (tx != null && tx.isActive()) { tx.rollback(); }
                throw (e);
            } finally {
                em.close();
            }

            return hsft;
        });
    }

    public ServicesForProgram getHealthServicesForTaskByRowId(String rowId) {
        EntityManager em = getEmf().createEntityManager();
        EntityTransaction tx = null;
        ServicesForProgram hsft = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            hsft = em.createQuery("SELECT i FROM ServicesForProgram i WHERE i.rowId = :rowId", ServicesForProgram.class)
                    .setParameter("rowId", rowId).getSingleResult();

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) { tx.rollback(); }
            throw (e);
        } finally {
            em.close();
        }

        return hsft;
    }

    public List<ServicesForProgram> getHealthServicesForTask(String taskId) {
        EntityManager em = getEmf().createEntityManager();
        EntityTransaction tx = null;
        List<ServicesForProgram> hsft = null;
        try {
            em = getEmf().createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            hsft = em
                    .createQuery("SELECT d FROM ServicesForProgram d WHERE d.programId = :programId ORDER BY d.serviceId ASC", ServicesForProgram.class)
                    .setParameter("programId", taskId)
                    .getResultList();

            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) { tx.rollback(); }
            throw (e);
        } finally {
            em.close();
        }


        return hsft;
    }
}