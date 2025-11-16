package com.smart.edilek.core.service;

import java.util.List;

import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.models.LazyEvent;

public interface GenericService<T> {
	void add(T t);
	void remove(T t);
	T modify(T t);
	T get(Class<T> tClass, long id);

	List<T> find(Class<T> tClass, LazyEvent lazyEvent);
	long count(Class<T> tClass, LazyEvent lazyEvent);

	List<T> find(Class<T> tClass, String key, String value, MatchMode matchMode, int rows);
	long count(Class<T> tClass, String key, String value, MatchMode matchMode, int rows);
}
