package com.smart.edilek.core.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.smart.edilek.core.dao.GenericDao;
import com.smart.edilek.core.models.Constraint;
import com.smart.edilek.core.models.FilterMeta;
import com.smart.edilek.core.models.LazyEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.TypedQuery;

@Repository
public class GenericDaoImp<T> implements GenericDao<T> {

	@PersistenceContext
	private EntityManager entityManager;

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void addEntity(T entity) {
		entityManager.persist(entity);
	}
	
	@Override
	public void removeEntity(T entity) {
		entityManager.remove(entity);
	}
	
	@Override
	public T modifyEntity(T entity) {
		return entityManager.merge(entity);
	}

	@Override
	public T findEntity(Class<T> tClass, long id) {	
		return entityManager.find(tClass, id);
	}

	@Override
	public T findEntity(Class<T> tClass, String id) {	
		return entityManager.find(tClass, id);
	}

	/*
	 * Datatable & Primereact
	 * 
	 * pager, sorth, search(with filter type) 
	 * Main or subentity
	 * 
	 */
	@Override
	public List<T> find(Class<T> tClass, LazyEvent lazyEvent) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(tClass);
        Root<T> root = query.from(tClass);

		// apply filters
        Predicate predicate = builder.conjunction();
        if (lazyEvent.getFilters() != null) {
            for (Map.Entry<String, FilterMeta> entry : lazyEvent.getFilters().entrySet()) {
                String field = entry.getKey();
                FilterMeta filter = entry.getValue();
                if (filter != null && filter.getConstraints() != null) {
					Predicate fieldPredicate = builder.conjunction(); // Default to AND
					for (Constraint constraint : filter.getConstraints()) {
						if (constraint.getValue() != null && !constraint.getValue().toString().isEmpty()) {
							Path<String> path = getPath(root, field, filter);
							Predicate constraintPredicate = getPredicate(builder, builder.conjunction(), constraint, path);
							fieldPredicate = applyOperator(builder, fieldPredicate, constraintPredicate, filter.getOperator());
						}
					}
					predicate = builder.and(predicate, fieldPredicate);
				}
            }
        }
        query.where(predicate);

		// apply sorting
		Path<?> sortPath = null;
        if (lazyEvent.getSortField() != null && lazyEvent.getSortOrder() != null) {
			sortPath = getSorthPath(root, lazyEvent.getSortField());
            Order order = lazyEvent.getSortOrder().equals("1") ? builder.asc(sortPath) : builder.desc(sortPath);
            query.orderBy(order);
        }

		// execute the query and return the results
        int offset = lazyEvent.getPage() * lazyEvent.getRows();
        TypedQuery<T> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(lazyEvent.getRows());

		return typedQuery.getResultList(); 
	}

	/*
	 * Datatable & Primreact
	 * 
	 * count, search(with filter type) 
	 * Main or subentity
	 * 
	 */
	@Override
	public long count(Class<T> tClass, LazyEvent lazyEvent) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<T> root = countQuery.from(tClass);

		countQuery.select(builder.count(root));

		// apply filters
        Predicate predicate = builder.conjunction();
        if (lazyEvent.getFilters() != null) {
            for (Map.Entry<String, FilterMeta> entry : lazyEvent.getFilters().entrySet()) {
                String field = entry.getKey();
                FilterMeta filter = entry.getValue();
                if (filter != null && filter.getConstraints() != null) {
					Predicate fieldPredicate = builder.conjunction(); // Default to AND
					for (Constraint constraint : filter.getConstraints()) {
						if (constraint.getValue() != null && !constraint.getValue().toString().isEmpty()) {
							Path<String> path = getPath(root, field, filter);
							Predicate constraintPredicate = getPredicate(builder, builder.conjunction(), constraint, path);
							fieldPredicate = applyOperator(builder, fieldPredicate, constraintPredicate, filter.getOperator());
						}
					}
					predicate = builder.and(predicate, fieldPredicate);
				}
            }
        }
        countQuery.where(predicate);

		TypedQuery<Long> typedCountQuery = entityManager.createQuery(countQuery);
		return (long) typedCountQuery.getSingleResult();
	}

	private Path<String> getPath(Root<T> root, String field, FilterMeta filter) {
		Path<String> path = null;
		if(field.contains(".")){
			String[] fieldNames = field.split("\\."); // split the string by "." to get nested field names
			Join<Object, Object> join = null;
			for (int i = 0; i < fieldNames.length; i++) {
				if (join == null) {
					join = root.join(fieldNames[i], JoinType.INNER);
					path = join.get(fieldNames[i + 1]);
					i++;
				} else {
					join = join.join(fieldNames[i], JoinType.INNER);
					if (i == fieldNames.length - 1) {
						path = join.get(fieldNames[i]);
					}
				}
			}
		} else {
			path = root.get(field);
		}
		return path;
	}
	

	private Path<?> getSorthPath(Root<T> root, String sorthField) {
		Path<?> sortPath;
		if(sorthField.contains(".")){
			String[] fieldNames = sorthField.split("\\."); // split the string by "." to get nested field names
			Join<Object, Object> join = null;
			for (int i = 0; i < fieldNames.length - 1; i++) {
				if (join == null) {
					join = root.join(fieldNames[i], JoinType.INNER);
				} else {
					join = join.join(fieldNames[i], JoinType.INNER);
				}
			}
			sortPath = join.get(fieldNames[fieldNames.length - 1]);
		}else{
			sortPath = root.get(sorthField);
		}
		return sortPath;
	}

	private Predicate getPredicate(CriteriaBuilder builder, Predicate predicate, Constraint constraint, Path<String> path) {
		switch (constraint.getMatchMode()) {
			case "startsWith":
				predicate = builder.and(predicate, builder.like(path, constraint.getValue() + "%"));
				break;
			case "contains":
				predicate = builder.and(predicate, builder.like(path, "%" + constraint.getValue() + "%"));
				break;
			case "notContains":
				predicate = builder.and(predicate, builder.notLike(path, "%" + constraint.getValue() + "%"));
				break;
			case "endsWith":
				predicate = builder.and(predicate, builder.like(path, "%" + constraint.getValue()));
				break;
			case "equals":
				predicate = builder.and(predicate, builder.equal(path, constraint.getValue()));
				break;
			case "notEquals":
				predicate = builder.and(predicate, builder.notEqual(path, constraint.getValue()));
				break;
			case "noFilter":
			default:
				predicate = builder.conjunction();
				break;
		}
		return predicate;
	}

	private Predicate applyOperator(CriteriaBuilder builder, Predicate currentPredicate, Predicate newPredicate, String operator) {
		if ("or".equalsIgnoreCase(operator)) {
			return builder.or(currentPredicate, newPredicate);
		} else {
			// Default to "and" if the operator is not specified or is invalid
			return builder.and(currentPredicate, newPredicate);
		}
	}
	
}
