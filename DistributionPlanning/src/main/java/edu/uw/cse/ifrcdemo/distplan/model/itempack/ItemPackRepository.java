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

package edu.uw.cse.ifrcdemo.distplan.model.itempack;

import edu.uw.cse.ifrcdemo.distplan.consts.PropertyChangeConsts;
import edu.uw.cse.ifrcdemo.distplan.entity.ItemPack;
import edu.uw.cse.ifrcdemo.distplan.model.AbstractOdkRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ItemPackRepository extends AbstractOdkRepository<ItemPack> {
  private final PropertyChangeSupport pcs;

  public PropertyChangeSupport getPcs() {
    return pcs;
  }

  public ItemPackRepository(EntityManagerFactory emf) {
    super(emf);
    this.pcs = new PropertyChangeSupport(this);
  }

  public CompletableFuture<ItemPack> saveItemPack(ItemPack item) {
    return CompletableFuture.supplyAsync(() -> {
      getPcs().firePropertyChange(PropertyChangeConsts.ITEM_PACK_ADD_ITEM_EVT, null, item);
      return saveEntity(item);
    });
  }

  public CompletableFuture<ItemPack> getItemPack(Long id) {
    return CompletableFuture.supplyAsync(() -> {
      EntityManager em = getEmf().createEntityManager();
      EntityTransaction tx = null;
      ItemPack itemPack = null;

      try {
        tx = em.getTransaction();
        tx.begin();

        itemPack = em.find(ItemPack.class, id);

        em.getTransaction().commit();

      } catch (RuntimeException e) {
        if (tx != null && tx.isActive()) { tx.rollback(); }
        throw (e);
      } finally {
        em.close();
      }

      return itemPack;
    });
  }

  public ItemPack getItemPackByRowId(String rowId) {
    EntityManager em = getEmf().createEntityManager();
    EntityTransaction tx = null;
    ItemPack itemPack = null;

    try {
      tx = em.getTransaction();
      tx.begin();

      itemPack = em.createQuery("SELECT i FROM ItemPack i WHERE i.rowId = :rowId", ItemPack.class)
          .setParameter("rowId", rowId).getSingleResult();

      em.getTransaction().commit();
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive()) { tx.rollback(); }
      throw (e);
    } finally {
      em.close();
    }

    return itemPack;
  }

  public List<ItemPack> getItemPackList() {
    EntityManager em = getEmf().createEntityManager();
    EntityTransaction tx = null;
    List<ItemPack> items = null;
    try {
      em = getEmf().createEntityManager();
      tx = em.getTransaction();
      tx.begin();

      items = em
          .createQuery("SELECT item FROM ItemPack item ORDER BY item.name ASC", ItemPack.class).getResultList();

      tx.commit();
    } catch (RuntimeException e) {
      if (tx != null && tx.isActive()) { tx.rollback(); }
      throw (e);
    } finally {
      em.close();
    }


    return items;
  }

  public void editItemPack(String rowId, String name, String description) {
    EntityManager em = getEmf().createEntityManager();
    em.getTransaction().begin();

    try {
      em
          .createQuery("UPDATE ItemPack item " +
              "SET item.name = :name, item.description = :desc " +
              "WHERE item.rowId = :rowId"
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
