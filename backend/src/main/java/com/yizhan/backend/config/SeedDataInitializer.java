package com.yizhan.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yizhan.backend.entity.Comment;
import com.yizhan.backend.entity.Goods;
import com.yizhan.backend.entity.Order;
import com.yizhan.backend.entity.User;
import com.yizhan.backend.mapper.CommentMapper;
import com.yizhan.backend.mapper.GoodsMapper;
import com.yizhan.backend.mapper.OrderMapper;
import com.yizhan.backend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 演示数据种子：仅在商品表为空时执行一次（幂等）。
 * 商品图片使用内置的 /static/img/goods-NN.png 渐变占位图（Nginx 直接提供，不依赖外网）。
 */
@Slf4j
@org.springframework.core.annotation.Order(2) // 在 DataInitializer(@Order(1)) 创建管理员之后执行
@Component
public class SeedDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;

    public SeedDataInitializer(UserMapper userMapper, GoodsMapper goodsMapper,
                               CommentMapper commentMapper, OrderMapper orderMapper) {
        this.userMapper = userMapper;
        this.goodsMapper = goodsMapper;
        this.commentMapper = commentMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public void run(String... args) {
        if (goodsMapper.selectCount(new QueryWrapper<>()) > 0) {
            return; // 已有数据，跳过
        }
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String pwd = enc.encode("root123456");
        LocalDateTime base = LocalDateTime.now();

        // 1) 演示用户（密码统一 root123456）
        Long zhangsan = addUser("zhangsan", pwd, "张三", base.minusDays(20));
        Long lisi = addUser("lisi", pwd, "李四", base.minusDays(15));
        Long wangwu = addUser("wangwu", pwd, "王五", base.minusDays(10));

        // 2) 12 件上架商品（图片为内置渐变占位图）
        Long g1 = addGoods(zhangsan, "95新 HHKB Pro 2 静电容键盘", "用了不到一年，键帽无打油，手感依旧。原装盒线齐全，诚心转让。", "1280.00", 1, base.minusDays(9));
        Long g2 = addGoods(zhangsan, "AirPods Pro 2（Type-C 版）", "国行在保，降噪拉满，耳塞套全新未拆。换头戴耳机了所以出。", "1150.00", 2, base.minusDays(9));
        Long g3 = addGoods(lisi, "小米手环 8 NFC 版", "用了两个月，表带全新，充电线齐全。屏幕无划痕，电池健康。", "135.00", 3, base.minusDays(8));
        Long g4 = addGoods(lisi, "罗技 G502 X 游戏鼠标", "25K HERO 传感器，手感极佳，微动无双击。换了无线版，低价出。", "289.00", 4, base.minusDays(7));
        Long g5 = addGoods(lisi, "红米 AX6S 路由器", "全屋覆盖无压力，支持 Mesh 组网。搬家换了光纤路由器。", "158.00", 5, base.minusDays(7));
        Long g6 = addGoods(wangwu, "雷蛇毒蝰终极版鼠标", "无线 20K 传感器，充电底座齐全，脚垫无磨损。", "350.00", 6, base.minusDays(6));
        Long g7 = addGoods(wangwu, "明基 ScreenBar 屏幕挂灯", "护眼神器，无频闪无蓝光危害。升级了旗舰款，出闲置。", "420.00", 7, base.minusDays(6));
        Long g8 = addGoods(zhangsan, "绿联 Type-C 扩展坞", "10 合 1 带千兆网口和 HDMI 4K60，兼容性好。全新仅拆封。", "168.00", 8, base.minusDays(5));
        Long g9 = addGoods(lisi, "海康威视 2T 移动固态硬盘", "读取 1000MB/s，带原装保护套。数据已清空，可当面验货。", "399.00", 9, base.minusDays(4));
        Long g10 = addGoods(wangwu, "京造 75% 三模机械键盘", "Gasket 结构，手感软弹，支持热插拔。用了一个月。", "239.00", 10, base.minusDays(3));
        Long g11 = addGoods(zhangsan, "酷态科 10 号电能棒充电宝", "100W 双向快充，能充笔记本。出差少用，九成新。", "129.00", 11, base.minusDays(2));
        Long g12 = addGoods(lisi, "爱格升显示器支架", "铝合金臂杆顺滑，承重 2-9kg。搬家闲置，自提优先。", "320.00", 12, base.minusDays(2));

        // 3) 评论
        addComment(g1, lisi, "成色确实很新，卖家靠谱，交易顺畅！", base.minusDays(8));
        addComment(g1, wangwu, "HHKB 的手感真的绝了，这个价超值。", base.minusDays(7));
        addComment(g2, wangwu, "降噪效果杠杠的，验过正品，推荐入手。", base.minusDays(7));
        addComment(g4, zhangsan, "鼠标握感很好，卖家发货很快。", base.minusDays(6));
        addComment(g5, wangwu, "信号覆盖比我家旧路由强多了。", base.minusDays(5));
        addComment(g7, lisi, "屏幕党护眼必备，挂上去桌面无光斑。", base.minusDays(4));
        addComment(g9, wangwu, "速度实测达标，包装严实，好评。", base.minusDays(3));
        addComment(g10, zhangsan, "热插拔很爽，已经换了新轴体。", base.minusDays(2));

        // 4) 订单（演示闭环：一单已完成、一单待支付）
        addOrder(g2, wangwu, zhangsan, 2, base.minusDays(6));
        addOrder(g5, wangwu, lisi, 0, base.minusDays(1));

        log.info("已写入演示数据：3 个用户（密码均 root123456）/ 12 件商品 / 8 条评论 / 2 笔订单");
    }

    private Long addUser(String username, String pwd, String nickname, LocalDateTime at) {
        User exists = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (exists != null) {
            return exists.getId(); // 用户已存在则复用，避免重复插入导致启动失败
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(pwd);
        u.setNickname(nickname);
        u.setRole("USER");
        u.setCreatedAt(at);
        userMapper.insert(u);
        return u.getId();
    }

    private Long addGoods(Long userId, String title, String desc, String price, int imgNo, LocalDateTime at) {
        Goods g = new Goods();
        g.setUserId(userId);
        g.setTitle(title);
        g.setDescription(desc);
        g.setPrice(new BigDecimal(price));
        g.setImageUrl("/static/img/goods-" + String.format("%02d", imgNo) + ".svg");
        g.setStatus(1);
        g.setCreatedAt(at);
        goodsMapper.insert(g);
        return g.getId();
    }

    private void addComment(Long goodsId, Long userId, String content, LocalDateTime at) {
        Comment c = new Comment();
        c.setGoodsId(goodsId);
        c.setUserId(userId);
        c.setContent(content);
        c.setCreatedAt(at);
        commentMapper.insert(c);
    }

    private void addOrder(Long goodsId, Long buyerId, Long sellerId, Integer status, LocalDateTime at) {
        Order o = new Order();
        o.setGoodsId(goodsId);
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setStatus(status);
        o.setCreatedAt(at);
        orderMapper.insert(o);
    }
}
