package com.bank.persistence.specification;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class GenericSpecification<T> implements Specification<T> {

    private static final long serialVersionUID = 1L;

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(
            Root<T> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {

        Path<?> path = root.get(criteria.field());

        return switch (criteria.operation()) {

            case EQUAL ->
                    cb.equal(path, criteria.value());

            case NOT_EQUAL ->
                    cb.notEqual(path, criteria.value());

            case GREATER_THAN ->
                    cb.greaterThan(
                            path.as(Comparable.class),
                            (Comparable) criteria.value());

            case GREATER_THAN_EQUAL ->
                    cb.greaterThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) criteria.value());

            case LESS_THAN ->
                    cb.lessThan(
                            path.as(Comparable.class),
                            (Comparable) criteria.value());

            case LESS_THAN_EQUAL ->
                    cb.lessThanOrEqualTo(
                            path.as(Comparable.class),
                            (Comparable) criteria.value());

            case LIKE ->
                    cb.like(
                            cb.lower(path.as(String.class)),
                            criteria.value().toString().toLowerCase());

            case STARTS_WITH ->
                    cb.like(
                            cb.lower(path.as(String.class)),
                            criteria.value().toString().toLowerCase() + "%");

            case ENDS_WITH ->
                    cb.like(
                            cb.lower(path.as(String.class)),
                            "%" + criteria.value().toString().toLowerCase());

            case CONTAINS ->
                    cb.like(
                            cb.lower(path.as(String.class)),
                            "%" + criteria.value().toString().toLowerCase() + "%");

            case IN ->
                    path.in((Collection<?>) criteria.value());

            case NOT_IN ->
                    cb.not(path.in((Collection<?>) criteria.value()));

            case IS_NULL ->
                    cb.isNull(path);

            case IS_NOT_NULL ->
                    cb.isNotNull(path);

            case TRUE ->
                    cb.isTrue(path.as(Boolean.class));

            case FALSE ->
                    cb.isFalse(path.as(Boolean.class));

            case BETWEEN -> {
                if (!(criteria.value() instanceof java.util.List<?> values)
                        || values.size() != 2) {
                    throw new IllegalArgumentException(
                            "BETWEEN requires a List with exactly 2 values.");
                }

                yield cb.between(
                        path.as(Comparable.class),
                        (Comparable) values.get(0),
                        (Comparable) values.get(1));
            }
        };
    }
}