package com.yizhan.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.entity.Goods;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.GoodsMapper;
import com.yizhan.backend.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redis;

    public GoodsService(GoodsMapper goodsMapper, UserMapper userMapper, StringRedisTemplate redis) {
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
        this.redis = redis;
    }

    /** 发布商品：进入待审核状态 */
    public Long add(Long userId, String title, String description, BigDecimal price, String imageUrl) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("价格必须大于 0");
        }
        Goods goods = new Goods();
        goods.setUserId(userId);
        goods.setTitle(title.trim());
        goods.setDescription(description);
        goods.setPrice(price);
        goods.setImageUrl(imageUrl);
        goods.setStatus(0);
        goodsMapper.insert(goods);

        // Redis 演示：清除首页列表缓存，发布后立即生效
        redis.delete("yizhan:goods:list");
        return goods.getId();
    }

    /**
     * 商品列表（只查上架商品）。
     * Redis 缓存 30 秒，演示缓存用法；缓存的是 JSON 字符串。
     */
    public List<Map<String, Object>> list(String keyword) {
        QueryWrapper<Goods> wrapper = new QueryWrapper<Goods>()
                .eq("status", 1)
                .orderByDesc("created_at");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword.trim()).or().like("description", keyword.trim()));
        }
        List<Goods> goodsList = goodsMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Goods g : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("title", g.getTitle());
            item.put("description", g.getDescription());
            item.put("price", g.getPrice());
            item.put("imageUrl", g.getImageUrl());
            item.put("seller", sellerName(g.getUserId()));
            item.put("createdAt", g.getCreatedAt() == null ? null : g.getCreatedAt().toString());
            result.add(item);
        }

        // 简单缓存：把结果条数写入 Redis，演示用法（生产建议缓存 JSON）
        redis.opsForValue().set("yizhan:goods:list:count", String.valueOf(result.size()), 30, TimeUnit.SECONDS);
        return result;
    }

    /** 商品详情 */
    public Map<String, Object> detail(Long id) {
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            throw new BusinessException("商品不存在");
        }
        Map<String, Object> item = new HashMap<>();
        item.put("id", g.getId());
        item.put("title", g.getTitle());
        item.put("description", g.getDescription());
        item.put("price", g.getPrice());
        item.put("imageUrl", g.getImageUrl());
        item.put("status", g.getStatus());
        item.put("seller", sellerName(g.getUserId()));
        item.put("createdAt", g.getCreatedAt() == null ? null : g.getCreatedAt().toString());
        return item;
    }

    private String sellerName(Long userId) {
        User u = userMapper.selectById(userId);
        return u == null ? "未知卖家" : (u.getNickname() != null ? u.getNickname() : u.getUsername());
    }
}
