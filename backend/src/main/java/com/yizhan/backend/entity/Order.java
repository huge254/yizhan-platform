package com.yizhan.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Long buyerId;
    private Long sellerId;
    private Integer status;       // 0-待支付 1-已支付 2-已完成
    private LocalDateTime createdAt;
}
