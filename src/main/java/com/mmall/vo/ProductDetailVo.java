package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ProductDetailVo {
    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;    //副标题
    private String mainImage;   //主图
    private String subImages;   //子图
    private String detail;      //详情描述
    private BigDecimal price;   //价格
    private Integer stock;      //库存
    private Integer status;     //上下架状态
    private String createTime;  //创建时间
    private String updateTime;  //更新时间
    private String imageHost;   //图片服务器的前缀
    private Integer parentCategoryId; //父分类
}
