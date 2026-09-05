package com.yizhan.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.entity.Comment;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.CommentMapper;
import com.yizhan.backend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentService(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    public void add(Long userId, Long goodsId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException("评论不能超过 500 字");
        }
        Comment c = new Comment();
        c.setGoodsId(goodsId);
        c.setUserId(userId);
        c.setContent(content.trim());
        commentMapper.insert(c);
    }

    public List<Map<String, Object>> listByGoods(Long goodsId) {
        List<Comment> comments = commentMapper.selectList(new QueryWrapper<Comment>()
                .eq("goods_id", goodsId)
                .orderByDesc("created_at"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("content", c.getContent());
            item.put("createdAt", c.getCreatedAt() == null ? null : c.getCreatedAt().toString());
            User u = userMapper.selectById(c.getUserId());
            item.put("user", u == null ? "匿名" : (u.getNickname() != null ? u.getNickname() : u.getUsername()));
            result.add(item);
        }
        return result;
    }
}
