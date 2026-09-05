package com.yizhan.backend.controller;

import com.yizhan.backend.common.Result;
import com.yizhan.backend.common.UserContext;
import com.yizhan.backend.service.GoodsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /** 发布商品 */
    @PostMapping("/add")
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String imageUrl = (String) body.get("imageUrl");
        BigDecimal price = body.get("price") == null ? null
                : new BigDecimal(body.get("price").toString());
        Long id = goodsService.add(UserContext.getUserId(), title, description, price, imageUrl);
        return Result.success(Map.of("id", id));
    }

    /** 商品列表（可带关键词） */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = goodsService.list(keyword);
        return Result.success(Map.of("list", list, "total", list.size()));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(goodsService.detail(id));
    }
}
