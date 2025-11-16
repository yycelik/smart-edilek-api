package com.smart.edilek.core.dao;

import java.util.List;

import com.smart.edilek.core.models.LazyEvent;

import jakarta.persistence.EntityManager;

public interface GenericDao<T> {

	void addEntity(T entity);
	void removeEntity(T entity);
	T modifyEntity(T entity);
	T findEntity(Class<T> tClass, long id);

	List<T> find(Class<T> tClass, LazyEvent lazyEvent);
	long count(Class<T> tClass, LazyEvent lazyEvent);

	EntityManager getEntityManager();
}
