package com.smart.edilek.core.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smart.edilek.core.annotation.LogExecutionTime;
import com.smart.edilek.core.dao.GenericDao;
import com.smart.edilek.core.enumObject.MatchMode;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.LazyEvent;

@Service
public class GenericServiceImp<T> implements GenericService<T> {

    @Autowired
    private GenericDao<T> genericDao;

    @Transactional
    @LogExecutionTime
    public void add(T t) {
        genericDao.addEntity(t);
    }

    @Transactional
    @LogExecutionTime
    public void remove(T t) {
        genericDao.removeEntity(t);
    }

    @Transactional
    @LogExecutionTime
    public T modify(T t) {
        return genericDao.modifyEntity(t);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public T get(Class<T> tClass, long id) {
        return genericDao.findEntity(tClass, id);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public List<T> find(Class<T> tClass, LazyEvent lazyEvent) {
        return genericDao.find(tClass, lazyEvent);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public long count(Class<T> tClass, LazyEvent lazyEvent) {
        return genericDao.count(tClass, lazyEvent);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public List<T> find(Class<T> tClass, String key, String value, MatchMode matchMode, int rows) {
        // create LazyEvent
        LazyEvent lazyEvent = new LazyEvent();

        // create a constraint with value and matchMode
        Constraint constraint = new Constraint();
        constraint.setValue(value);
        constraint.setMatchMode(matchMode.toString());

        // set operator and constraints for FilterMeta
        FilterMeta filterMeta = new FilterMeta();
        filterMeta.setOperator("and"); // default operator
        filterMeta.setConstraints(List.of(constraint)); // create a list of constraints

        // set filter using map
        Map<String, FilterMeta> filters = new HashMap<>();
        filters.put(key, filterMeta);
        lazyEvent.setFilters(filters);

        // set max rows, first element, and page
        lazyEvent.setRows(rows);
        lazyEvent.setFirst(0);
        lazyEvent.setPage(1);

        return genericDao.find(tClass, lazyEvent);
    }

    @Transactional(readOnly = true)
    @LogExecutionTime
    public long count(Class<T> tClass, String key, String value, MatchMode matchMode, int rows) {
        // create LazyEvent
        LazyEvent lazyEvent = new LazyEvent();

        // create a constraint with value and matchMode
        Constraint constraint = new Constraint();
        constraint.setValue(value);
        constraint.setMatchMode(matchMode.toString());

        // set operator and constraints for FilterMeta
        FilterMeta filterMeta = new FilterMeta();
        filterMeta.setOperator("and"); // default operator
        filterMeta.setConstraints(List.of(constraint)); // create a list of constraints

        // set filter using map
        Map<String, FilterMeta> filters = new HashMap<>();
        filters.put(key, filterMeta);
        lazyEvent.setFilters(filters);

        // set max rows, first element, and page
        lazyEvent.setRows(rows);
        lazyEvent.setFirst(0);
        lazyEvent.setPage(1);

        return genericDao.count(tClass, lazyEvent);
    }

}
