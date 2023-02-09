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

import edu.uw.cse.ifrcdemo.healthplan.entity.HealthService;
import edu.uw.cse.ifrcdemo.healthplan.ui.healthservice.EditHealthServiceFormModel;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.AbstractOdkRepository;
import edu.uw.cse.ifrcdemo.planningsharedlib.model.form.XlsxForm;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class HealthServiceRepository extends AbstractOdkRepository<HealthService> {

    public HealthServiceRepository(EntityManagerFactory emf) {
        super(emf);
    }

    public CompletableFuture<Void> saveHealthService(HealthService healthService) {
        return CompletableFuture.runAsync(() -> {
            saveEntity(healthService);
        });
    }

    public CompletableFuture<HealthService> getHealthService(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            EntityManager em = getEmf().createEntityManager();
            em.getTransaction().begin();

            HealthService healthService;
            try {
                healthService = em.find(HealthService.class, id);

                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }

            return healthService;
        });
    }

    public List<HealthService> getAllHealthServices() {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        List<HealthService> healthServices;
        try {
            healthServices = em
                    .createQuery("SELECT s FROM HealthService s ORDER BY s.name ASC", HealthService.class)
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

        return healthServices;
    }

    public HealthService getHealthServiceByRowId(String rowId) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        HealthService healthService;
        try {
            healthService = em
                    .createQuery("SELECT s FROM HealthService s WHERE s.rowId = :rowId", HealthService.class)
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

        return healthService;
    }

    public void editHealthService(String rowId, EditHealthServiceFormModel ehsfm) {
        EntityManager em = getEmf().createEntityManager();
        em.getTransaction().begin();

        try {
            em
                    .createQuery("UPDATE HealthService service " +
                            "SET service.name = :name, service.description = :desc, " +
                            "service.endWithReferrals = :ewr, service.requiresReferral = :reqref " +
                            "WHERE service.rowId = :rowId"
                    )
                    .setParameter("name", ehsfm.getName())
                    .setParameter("desc", ehsfm.getDescription())
                    .setParameter("ewr", ehsfm.getEndWithReferrals())
                    .setParameter("reqref", ehsfm.getRequiresReferral())
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

    public List<XlsxForm> getAllForms() {
        EntityManager em = getEmf().createEntityManager();

        List<HealthService> serviceFormList;
        List<HealthService> refferalFormList;

        em.getTransaction().begin();
        try {
            serviceFormList = em.createQuery("SELECT s FROM HealthService s WHERE s.serviceFormId IS NOT NULL",
                    HealthService.class)
                    .getResultList();

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }

        em.getTransaction().begin();
        try {
            refferalFormList = em.createQuery("SELECT s FROM HealthService s WHERE s.serviceFormId IS NOT NULL",
                    HealthService.class)
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

        List<XlsxForm> formsList = new ArrayList<XlsxForm>();
        if(serviceFormList != null && !serviceFormList.isEmpty()) {
            for(HealthService service : serviceFormList) {
                XlsxForm form = new XlsxForm(service.getServiceTableId(), service.getServiceFormId());
                formsList.add(form);
            }
        }
        if(refferalFormList != null && !refferalFormList.isEmpty()) {
            for(HealthService service : refferalFormList) {
                XlsxForm form = new XlsxForm(service.getServiceTableId(), service.getServiceFormId());
                formsList.add(form);
            }
        }

        return formsList;
    }
}
