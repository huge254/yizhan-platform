package com.yizhan.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.entity.Goods;
import com.yizhan.backend.entity.Order;
import com.yizhan.backend.mapper.GoodsMapper;
import com.yizhan.backend.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;

    public OrderService(OrderMapper orderMapper, GoodsMapper goodsMapper) {
        this.orderMapper = orderMapper;
        this.goodsMapper = goodsMapper;
    }

    /** 创建订单：校验商品状态，不能买自己的商品 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long buyerId, Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (goods.getStatus() != 1) {
            throw new BusinessException("商品当前不可购买");
        }
        if (goods.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        Order order = new Order();
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setStatus(0);
        orderMapper.insert(order);

        // 下单后商品转为已售
        goods.setStatus(2);
        goodsMapper.updateById(goods);
        return order.getId();
    }

    /** 我的订单（买家视角） */
    public List<Map<String, Object>> myList(Long buyerId) {
        List<Order> orders = orderMapper.selectList(new QueryWrapper<Order>()
                .eq("buyer_id", buyerId)
                .orderByDesc("created_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", o.getId());
            item.put("goodsId", o.getGoodsId());
            Goods g = goodsMapper.selectById(o.getGoodsId());
            item.put("goodsTitle", g == null ? "（商品已删除）" : g.getTitle());
            item.put("amount", g == null ? null : g.getPrice());
            item.put("status", o.getStatus());
            item.put("createdAt", o.getCreatedAt() == null ? null : o.getCreatedAt().toString());
            result.add(item);
        }
        return result;
    }

    /** 确认收货（模拟支付完成）：状态 0→2 */
    public void confirm(Long buyerId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许此操作");
        }
        order.setStatus(2);
        orderMapper.updateById(order);
    }
}
