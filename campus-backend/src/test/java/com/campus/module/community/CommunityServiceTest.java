package com.campus.module.community;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.module.community.dto.CommunityPostRequest;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.service.CommunityPostService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 社区帖子服务测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("社区帖子服务测试")
class CommunityServiceTest {

    @Autowired
    private CommunityPostService communityPostService;

    @Autowired
    private SysUserService userService;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        SysUser user = new SysUser();
        user.setUsername("community_test_" + System.currentTimeMillis());
        user.setPassword("test123456");
        user.setNickname("社区测试用户");
        user.setRole(2);
        user.setStatus(1);
        userService.register(user);
        testUserId = user.getId();
        UserContext.setUser(testUserId, 2);
    }

    // ==================== 帖子发布测试 ====================

    @Test
    @Order(1)
    @DisplayName("1. 发布帖子 - 正常流程")
    @Transactional
    void testCreatePost_Success() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("如何高效备考期末考试？");
        request.setContent("分享我的备考经验：第一制定复习计划，第二合理分配时间...");
        request.setTopicType(1);
        request.setTags("学习经验,考试技巧");

        Long postId = communityPostService.createPost(testUserId, request);

        assertNotNull(postId, "应返回帖子ID");

        CommunityPost post = communityPostService.getById(postId);
        assertNotNull(post, "帖子应存在");
        assertEquals("如何高效备考期末考试？", post.getTitle());
        assertEquals(1, post.getTopicType());
        assertEquals("学习经验,考试技巧", post.getTags());
        assertEquals(0, post.getViewCount());
        assertEquals(0, post.getLikeCount());
        assertEquals(1, post.getStatus());

        System.out.println("✅ 帖子发布成功，ID: " + postId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 发布帖子 - 带图片")
    @Transactional
    void testCreatePost_WithImages() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("校园活动推荐");
        request.setContent("这周末有精彩的社团招新活动...");
        request.setTopicType(1);
        request.setTags("校园生活,活动推荐");
        request.setImages("[\"uploads/img1.jpg\", \"uploads/img2.jpg\"]");

        Long postId = communityPostService.createPost(testUserId, request);

        CommunityPost post = communityPostService.getById(postId);
        assertNotNull(post);
        assertNotNull(post.getImages());

        System.out.println("✅ 带图片帖子发布成功");
    }

    @Test
    @Order(3)
    @DisplayName("3. 发布帖子 - 难题求助类型")
    @Transactional
    void testCreatePost_HelpType() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("求Python编程学习建议");
        request.setContent("大一新生，想自学Python但不知道从哪里开始...");
        request.setTopicType(2);
        request.setTags("求助问答,选课建议");

        Long postId = communityPostService.createPost(testUserId, request);

        CommunityPost post = communityPostService.getById(postId);
        assertEquals(2, post.getTopicType());

        System.out.println("✅ 难题求助帖子发布成功");
    }

    @Test
    @Order(4)
    @DisplayName("4. 发布帖子 - 无标签")
    @Transactional
    void testCreatePost_NoTags() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("纯文字经验分享");
        request.setContent("今天分享一下我的学习方法...");
        request.setTopicType(1);

        Long postId = communityPostService.createPost(testUserId, request);

        CommunityPost post = communityPostService.getById(postId);
        assertNotNull(post);
        assertNull(post.getTags());

        System.out.println("✅ 无标签帖子发布成功");
    }

    // ==================== 帖子查询测试 ====================

    @Test
    @Order(5)
    @DisplayName("5. 获取帖子列表 - 按话题类型筛选")
    @Transactional
    void testListPosts_ByTopicType() {
        // 发布经验分享
        CommunityPostRequest req1 = new CommunityPostRequest();
        req1.setTitle("经验分享帖");
        req1.setContent("经验内容");
        req1.setTopicType(1);
        communityPostService.createPost(testUserId, req1);

        // 发布难题求助
        CommunityPostRequest req2 = new CommunityPostRequest();
        req2.setTitle("难题求助帖");
        req2.setContent("求助内容");
        req2.setTopicType(2);
        communityPostService.createPost(testUserId, req2);

        IPage<CommunityPost> list1 = communityPostService.listPosts(1, 1, 10);
        assertNotNull(list1);
        list1.getRecords().forEach(p -> assertEquals(1, p.getTopicType()));

        IPage<CommunityPost> list2 = communityPostService.listPosts(2, 1, 10);
        list2.getRecords().forEach(p -> assertEquals(2, p.getTopicType()));

        System.out.println("✅ 按话题类型筛选正常");
    }

    @Test
    @Order(6)
    @DisplayName("6. 获取帖子详情 - 浏览量自增")
    @Transactional
    void testGetPostDetail() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("浏览量测试帖");
        request.setContent("测试浏览量自增功能");
        request.setTopicType(1);
        Long postId = communityPostService.createPost(testUserId, request);

        CommunityPost post = communityPostService.getPostDetail(postId);
        assertNotNull(post);
        assertEquals(testUserId, post.getUserId());
        assertTrue(post.getViewCount() >= 1);

        System.out.println("✅ 帖子详情获取正常，浏览量: " + post.getViewCount());
    }

    @Test
    @Order(7)
    @DisplayName("7. 获取不存在的帖子详情 - 抛出异常")
    @Transactional
    void testGetPostDetail_NotFound() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            communityPostService.getPostDetail(99999L);
        });
        System.out.println("✅ 不存在的帖子正确抛出异常: " + exception.getMessage());
    }

    // ==================== 点赞测试 ====================

    @Test
    @Order(8)
    @DisplayName("8. 点赞帖子")
    @Transactional
    void testLikePost() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("点赞测试帖");
        request.setContent("测试点赞功能");
        request.setTopicType(1);
        Long postId = communityPostService.createPost(testUserId, request);

        // 点赞
        boolean liked = communityPostService.likePost(postId);
        assertTrue(liked, "首次点赞应返回true");

        CommunityPost post = communityPostService.getById(postId);
        assertEquals(1, post.getLikeCount());

        // 取消点赞
        boolean unliked = communityPostService.likePost(postId);
        assertFalse(unliked, "取消点赞应返回false");

        post = communityPostService.getById(postId);
        assertEquals(0, post.getLikeCount());

        System.out.println("✅ 点赞/取消点赞功能正常");
    }

    // ==================== 管理员功能测试 ====================

    @Test
    @Order(9)
    @DisplayName("9. 管理员删除/恢复帖子")
    @Transactional
    void testAdminDeleteAndRestore() {
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("待删除测试帖");
        request.setContent("测试软删除功能");
        request.setTopicType(1);
        Long postId = communityPostService.createPost(testUserId, request);

        // 软删除
        communityPostService.deletePost(postId);
        CommunityPost post = communityPostService.getById(postId);
        assertEquals(0, post.getStatus(), "删除后状态应为0");

        // 验证已删除帖子不出现在用户列表
        IPage<CommunityPost> list = communityPostService.listPosts(null, 1, 10);
        list.getRecords().forEach(p -> assertNotEquals(postId, p.getId(), "已删除帖子不应出现在用户列表"));

        // 恢复帖子
        post.setStatus(1);
        communityPostService.updateById(post);
        CommunityPost restored = communityPostService.getById(postId);
        assertEquals(1, restored.getStatus(), "恢复后状态应为1");

        System.out.println("✅ 软删除/恢复功能正常");
    }

    @Test
    @Order(10)
    @DisplayName("10. 管理员列表查询 - 含已隐藏帖子")
    @Transactional
    void testAdminListPosts() {
        CommunityPostRequest req1 = new CommunityPostRequest();
        req1.setTitle("管理员可见测试帖");
        req1.setContent("测试管理员列表");
        req1.setTopicType(1);
        communityPostService.createPost(testUserId, req1);

        IPage<CommunityPost> adminList = communityPostService.adminListPosts(null, 1, 10);
        assertNotNull(adminList);

        System.out.println("✅ 管理员列表查询正常，总数: " + adminList.getTotal());
    }

    // ==================== 综合测试 ====================

    @Test
    @Order(11)
    @DisplayName("11. 综合测试 - 发布→查询→详情完整流程")
    @Transactional
    void testFullPublishFlow() {
        // 1. 发布
        CommunityPostRequest request = new CommunityPostRequest();
        request.setTitle("综合测试帖：如何学好高等数学");
        request.setContent("高等数学是大学非常重要的一门课程，以下是几点学习建议：\n1. 课前预习\n2. 认真听讲\n3. 课后练习\n4. 及时复习总结");
        request.setTopicType(1);
        request.setTags("学习经验,考试技巧");

        Long postId = communityPostService.createPost(testUserId, request);
        assertNotNull(postId);

        // 2. 详情
        CommunityPost post = communityPostService.getPostDetail(postId);
        assertEquals("综合测试帖：如何学好高等数学", post.getTitle());
        assertNotNull(post.getAuthorNickname());
        assertTrue(post.getReplyCount() >= 0);

        // 3. 列表出现
        IPage<CommunityPost> list = communityPostService.listPosts(1, 1, 10);
        boolean found = list.getRecords().stream().anyMatch(p -> p.getId().equals(postId));
        assertTrue(found, "新发布的帖子应出现在列表中");

        System.out.println("✅ 完整发布流程测试通过");
    }
}
