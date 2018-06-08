package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 相当于是一个商品以列表的形式展现出来，但是此时的话是不需要全部展示的，所以写一个ListVo
 */

@Setter
@Getter
public class ProductListVo {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;    //副标题
    private String mainImage;   //主图
    private BigDecimal price;   //价格
    private Integer status;     //上下架状态
    private String imageHost;   //图片服务器的前缀
}
