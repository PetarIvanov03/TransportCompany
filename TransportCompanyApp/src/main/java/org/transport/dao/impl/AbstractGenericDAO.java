package org.transport.dao.impl;

import org.hibernate.Session;
import org.transport.dao.GenericDAO;

import java.util.List;

public abstract class AbstractGenericDAO<T, ID> implements GenericDAO<T, ID> {

    private final Class<T> entityClass;

    protected AbstractGenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T findById(Session session, ID id) {
        return session.get(entityClass, id);
    }

    @Override
    public List<T> findAll(Session session) {
        return session.createQuery(
                        "FROM " + entityClass.getSimpleName(), entityClass)
                .list();
    }

    @Override
    public void save(Session session, T entity) {
        session.persist(entity);
    }

    @Override
    public void update(Session session, T entity) {
        session.merge(entity);
    }

    @Override
    public void delete(Session session, T entity) {
        session.remove(entity);
    }

    @Override
    public void deleteById(Session session, ID id) {
        T entity = findById(session, id);
        if (entity != null) {
            delete(session, entity);
        }
    }
}