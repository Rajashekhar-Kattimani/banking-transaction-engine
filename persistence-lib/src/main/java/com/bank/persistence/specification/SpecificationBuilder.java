package com.bank.persistence.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {

    private final List<SearchCriteria> criteriaList = new ArrayList<>();

    public SpecificationBuilder<T> with(SearchCriteria criteria) {
        criteriaList.add(criteria);
        return this;
    }

    public SpecificationBuilder<T> with(
            String field,
            SearchOperation operation,
            Object value) {

        criteriaList.add(
                new SearchCriteria(field, operation, value));

        return this;
    }

    public Specification<T> build() {

        if (criteriaList.isEmpty()) {
            return null;
        }

        Specification<T> specification =
                new GenericSpecification<>(criteriaList.getFirst());

        for (int i = 1; i < criteriaList.size(); i++) {
            specification = specification.and(
                    new GenericSpecification<>(criteriaList.get(i)));
        }

        return specification;
    }

}