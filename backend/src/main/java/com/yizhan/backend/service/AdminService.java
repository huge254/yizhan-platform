package com.yizhan.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.entity.Goods;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.CommentMapper;
import com.yizhan.backend.mapper.GoodsMapper;
import com.yizhan.backend.mapper.OrderMapper;
import com.yizhan.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理：仅管理员可用（Controller 层已校验角色）。
 */
@Service
public class AdminService {

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final CommentMapper commentMapper;

    public AdminService(GoodsMapper goodsMapper, UserMapper userMapper,
                        OrderMapper orderMapper, CommentMapper commentMapper) {
        this.goodsMapper = goodsMapper;
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.commentMapper = commentMapper;
    }

    /** 全部商品（所有状态） */
    public List<Map<String, Object>> goodsList() {
        List<Goods> goodsList = goodsMapper.selectList(new QueryWrapper<Goods>().orderByDesc("created_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Goods g : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("title", g.getTitle());
            item.put("price", g.getPrice());
            item.put("status", g.getStatus());
            item.put("createdAt", g.getCreatedAt() == null ? null : g.getCreatedAt().toString());
            User seller = userMapper.selectById(g.getUserId());
            item.put("seller", seller == null ? "未知" : seller.getUsername());
            result.add(item);
        }
        return result;
    }

    /** 审核/下架商品 */
    public void updateGoodsStatus(Long goodsId, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException("状态值非法");
        }
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            throw new BusinessException("商品不存在");
        }
        g.setStatus(status);
        goodsMapper.updateById(g);
    }

    /** 用户列表 */
    public List<Map<String, Object>> userList() {
        List<User> users = userMapper.selectList(new QueryWrapper<User>().orderByDesc("created_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname());
            item.put("role", u.getRole());
            item.put("createdAt", u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
            result.add(item);
        }
        return result;
    }

    /** 数据统计 */
    public Map<String, Object> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("goodsCount", goodsMapper.selectCount(null));
        data.put("pendingGoods", goodsMapper.selectCount(new QueryWrapper<Goods>().eq("status", 0)));
        data.put("orderCount", orderMapper.selectCount(null));
        data.put("commentCount", commentMapper.selectCount(null));
        return data;
    }
}
