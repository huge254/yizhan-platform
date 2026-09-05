package com.yizhan.backend.controller;

import com.yizhan.backend.common.Result;
import com.yizhan.backend.common.UserContext;
import com.yizhan.backend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 创建订单 */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Long> body) {
        Long goodsId = body.get("goodsId");
        Long orderId = orderService.create(UserContext.getUserId(), goodsId);
        return Result.success(Map.of("orderId", orderId));
    }

    /** 我的订单 */
    @GetMapping("/list")
    public Result<Map<String, Object>> list() {
        List<Map<String, Object>> list = orderService.myList(UserContext.getUserId());
        return Result.success(Map.of("list", list));
    }

    /** 确认收货 */
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody Map<String, Long> body) {
        orderService.confirm(UserContext.getUserId(), body.get("orderId"));
        return Result.success();
    }
}
