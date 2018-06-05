package com.mmall.vo;

import java.math.BigDecimal;

/**
 * 相当于是一个商品以列表的形式展现出来，但是此时的话是不需要全部展示的，所以写一个ListVo
 */

public class ProductListVo {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;    //副标题
    private String mainImage;   //主图
    private BigDecimal price;   //价格
    private Integer status;     //上下架状态
    private String imageHost;   //图片服务器的前缀

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
