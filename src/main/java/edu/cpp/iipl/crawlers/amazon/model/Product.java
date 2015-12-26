package edu.cpp.iipl.crawlers.amazon.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by xing on 12/22/15.
 */
public class Product {

    //@Id
    //@GeneratedValue
    private Long id;

    //@NotNull
    //@Column(unique = true)
    //@Size(min=1, max=256)
    private String asin;    // unique in Amazon, as an identifier for product

    //@NotNull
    //@Size(min=1, max=2048)
    private String name;

    //@Size(min=1, max=256)
    private String modelNum;

    // this field is used to determine whether needs to renew reviews
    private Integer numOfReviewsOnPage;

    // this field is used to determine whether needs to renew product info
    private Date updateDate;

    // url to the product page
    private String pageUrl;

    private String imgUrlHiRes; // url to the high resolution cover image
    private String imgUrlLarge; // url to the large (main) cover image

    //@JsonIgnore
    //@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private transient List<Review> reviews = new ArrayList<>();

    //@JsonIgnore
    //@NotNull
    //@ManyToOne
    private Brand brand;    // owner of OneToMany

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelNum() {
        return modelNum;
    }

    public void setModelNum(String modelNum) {
        this.modelNum = modelNum;
    }

    public Integer getNumOfReviewsOnPage() {
        return numOfReviewsOnPage;
    }

    public void setNumOfReviewsOnPage(Integer numOfReviewsOnPage) {
        this.numOfReviewsOnPage = numOfReviewsOnPage;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getImgUrlHiRes() {
        return imgUrlHiRes;
    }

    public void setImgUrlHiRes(String imgUrlHiRes) {
        this.imgUrlHiRes = imgUrlHiRes;
    }

    public String getImgUrlLarge() {
        return imgUrlLarge;
    }

    public void setImgUrlLarge(String imgUrlLarge) {
        this.imgUrlLarge = imgUrlLarge;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
