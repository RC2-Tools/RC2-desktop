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

import edu.uw.cse.ifrcdemo.healthplan.entity.HealthTask;
import edu.uw.cse.ifrcdemo.healthplan.logic.HealthTaskStatus;
import edu.uw.cse.ifrcdemo.healthplan.ui.heathtask.EditHealthTaskFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class HealthTaskRepository extends AbstractOdkRepository<HealthTask> {

    public HealthTaskRepository(EntityManagerFactory emf) {
        super(emf);
    }

    public CompletableFuture<Void> saveHealthTask(HealthTask healthTask) {
        return CompletableFuture.runAsync(() -> {
            saveEntity(healthTask);
        });
    }

    public CompletableFuture<HealthTask> getHealthTask(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            EntityManager em = getEmf().createEntityManager();
            em.getTransaction().begin();

            HealthTask healthTask;
            try {
                healthTask = em.find(HealthTask.class, id);

                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }

            return healthTask;
        });
    }

    public List<HealthTask> getAllHealthTasks() {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        List<HealthTask> healthTasks;
        try {
            healthTasks = em
                    .createQuery("SELECT s FROM HealthTask s ORDER BY s.name ASC", HealthTask.class)
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

        return healthTasks;
    }

    public HealthTask getHealthTaskByRowId(String rowId) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        HealthTask healthTask;
        try {
            healthTask = em
                    .createQuery("SELECT s FROM HealthTask s WHERE s.rowId = :rowId", HealthTask.class)
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

        return healthTask;
    }

    public void editHealthTask(String rowId, EditHealthTaskFormModel ehtfm) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        HealthTaskStatus status = ehtfm.getStatus();

        try {
            em.createQuery("UPDATE HealthTask task " +
                 "SET task.name = :name, task.description = :desc, task.status = :status " +
                 "WHERE task.rowId = :rowId"
                 )
                 .setParameter("name", ehtfm.getName())
                 .setParameter("desc", ehtfm.getDescription())
                 .setParameter("status", status.name())
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
