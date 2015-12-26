package edu.cpp.iipl.crawlers.amazon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xing on 12/22/15.
 */
public class Brand {

    //@Id
    //@GeneratedValue
    private Long id;

    //@NotNull
    private String name;

    //@JsonIgnore
    //@OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    //@JsonIgnore
    //@OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    public Brand() {

    }

    public Brand(String name) {
        this.name = name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
