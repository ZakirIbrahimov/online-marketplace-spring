package com.marketplace.repository;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.Product;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> catalogVisible(String q) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("listingStatus"), ListingStatus.ACTIVE));
            p.add(cb.greaterThan(root.get("stock"), 0));
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(
                        cb.or(
                                cb.like(cb.lower(root.get("title")), like),
                                cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> adminAll() {
        return (root, query, cb) -> cb.conjunction();
    }
}
