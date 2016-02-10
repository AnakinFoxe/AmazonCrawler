package edu.cpp.iipl.crawlers.amazon.model;

import java.util.Date;

/**
 * Created by xing on 12/22/15.
 */
public class Review {

    //@Id
    //@GeneratedValue
    private Long id;

    //@NotNull
    //@Column(unique = true)
    //@Size(min=1, max=256)
    private String name;

    private Integer rate;

    //@Size(min=1, max=2048)
    private String title;

    private Date date;

    //@Size(min=1, max=2048)
    private String permalink;

    private Float helpRatio;

    private String modelNum;    // from product

    private String text;     // review text

    //@JsonIgnore
    private Integer crawledTimes;   // how many times crawled by ContentCrawler

    //@JsonIgnore
    //@ManyToOne
    private Product product;    // owner of OneToMany

    //@JsonIgnore
    //@ManyToOne
    private Brand brand;    // owner of OneToMany

    public Review() {
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

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public Float getHelpRatio() {
        return helpRatio;
    }

    public void setHelpRatio(Float helpRatio) {
        this.helpRatio = helpRatio;
    }

    public String getModelNum() {
        return modelNum;
    }

    public void setModelNum(String modelNum) {
        this.modelNum = modelNum;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getCrawledTimes() {
        return crawledTimes;
    }

    public void setCrawledTimes(Integer crawledTimes) {
        this.crawledTimes = crawledTimes;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
