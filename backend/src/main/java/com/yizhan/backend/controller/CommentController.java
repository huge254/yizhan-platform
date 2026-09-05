package com.yizhan.backend.controller;

import com.yizhan.backend.common.Result;
import com.yizhan.backend.common.UserContext;
import com.yizhan.backend.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** 发表评论 */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        Long goodsId = Long.valueOf(body.get("goodsId").toString());
        String content = (String) body.get("content");
        commentService.add(UserContext.getUserId(), goodsId, content);
        return Result.success();
    }

    /** 商品评论列表 */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam Long goodsId) {
        List<Map<String, Object>> list = commentService.listByGoods(goodsId);
        return Result.success(Map.of("list", list));
    }
}
