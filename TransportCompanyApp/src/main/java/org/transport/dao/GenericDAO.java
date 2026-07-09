package org.transport.dao;

import org.hibernate.Session;
import java.util.List;

public interface GenericDAO<T, ID> {
    T findById(Session session, ID id);
    List<T> findAll(Session session);
    void save(Session session, T entity);
    void update(Session session, T entity);
    void delete(Session session, T entity);
    void deleteById(Session session, ID id);
}