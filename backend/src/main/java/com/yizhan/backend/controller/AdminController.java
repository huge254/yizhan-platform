package com.yizhan.backend.controller;

import com.yizhan.backend.common.BusinessException;
import com.yizhan.backend.common.Result;
import com.yizhan.backend.common.UserContext;
import com.yizhan.backend.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理接口：所有方法先校验管理员角色。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private void checkAdmin() {
        if (!UserContext.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作");
        }
    }

    @GetMapping("/goods")
    public Result<Map<String, Object>> goodsList() {
        checkAdmin();
        return Result.success(Map.of("list", adminService.goodsList()));
    }

    @PostMapping("/goods/status")
    public Result<Void> updateGoodsStatus(@RequestBody Map<String, Object> body) {
        checkAdmin();
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        adminService.updateGoodsStatus(id, status);
        return Result.success();
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> userList() {
        checkAdmin();
        return Result.success(Map.of("list", adminService.userList()));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        checkAdmin();
        return Result.success(adminService.stats());
    }
}
