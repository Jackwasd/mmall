package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CartProductVo {
    //结合了产品和购物车的抽象对象
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private String productName;
    private String productSubtitle;
    private String productMainImage;
    private BigDecimal productPrice;
    private Integer status;
    private BigDecimal productTotalPrice;  //产品的总价
    private Integer productStock;
    private Integer productChecked;        //产品在购物车中是否勾选

    private String limitQuantity;          //购物车中超过库存的返回结果
}
