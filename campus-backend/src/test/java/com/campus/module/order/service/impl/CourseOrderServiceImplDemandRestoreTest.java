package com.campus.module.order.service.impl;

import com.campus.common.exception.BusinessException;
import com.campus.module.booking.mapper.BookingRequestMapper;
import com.campus.module.demand.entity.DemandPost;
import com.campus.module.demand.mapper.DemandPostMapper;
import com.campus.module.demand.mapper.TutorApplicationMapper;
import com.campus.module.demand.service.GeoService;
import com.campus.module.order.entity.CourseOrder;
import com.campus.module.order.mapper.CourseOrderMapper;
import com.campus.module.teaching.mapper.TeachingRecordMapper;
import com.campus.module.tutor.mapper.TutorProfileMapper;
import com.campus.module.wallet.service.SysTransactionFlowService;
import com.campus.module.wallet.service.SysWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单取消后需求重新上架测试")
class CourseOrderServiceImplDemandRestoreTest {

    @Mock
    private TutorProfileMapper tutorProfileMapper;
    @Mock
    private SysWalletService walletService;
    @Mock
    private TeachingRecordMapper teachingRecordMapper;
    @Mock
    private DemandPostMapper demandPostMapper;
    @Mock
    private SysTransactionFlowService transactionFlowService;
    @Mock
    private GeoService geoService;
    @Mock
    private BookingRequestMapper bookingRequestMapper;
    @Mock
    private TutorApplicationMapper tutorApplicationMapper;
    @Mock
    private CourseOrderMapper courseOrderMapper;

    private CourseOrderServiceImpl orderService;

    private static final Long PARENT_ID = 100L;
    private static final Long TUTOR_ID = 200L;
    private static final Long DEMAND_ID = 300L;
    private static final Long ORDER_ID = 400L;
    private static final Long OTHER_TUTOR_ID = 999L;

    @BeforeEach
    void setUp() {
        orderService = spy(new CourseOrderServiceImpl(
                tutorProfileMapper, walletService, teachingRecordMapper,
                demandPostMapper, transactionFlowService, geoService, bookingRequestMapper,
                tutorApplicationMapper));
        ReflectionTestUtils.setField(orderService, "baseMapper", courseOrderMapper);
        lenient().doReturn(true).when(orderService).updateById(any());
    }

    private CourseOrder buildOrder(Integer status, Long demandId, Long tutorId) {
        CourseOrder order = new CourseOrder();
        order.setId(ORDER_ID);
        order.setParentId(PARENT_ID);
        order.setTutorId(tutorId);
        order.setDemandId(demandId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("1000"));
        order.setTutorAmount(new BigDecimal("900"));
        order.setUnitPrice(new BigDecimal("100"));
        order.setTotalHours(10);
        return order;
    }

    private DemandPost buildDemand(Integer status, Long matchedTutorId) {
        DemandPost demand = new DemandPost();
        demand.setId(DEMAND_ID);
        demand.setPublisherId(PARENT_ID);
        demand.setStatus(status);
        demand.setMatchedTutorId(matchedTutorId);
        demand.setLongitude(new BigDecimal("116.397428"));
        demand.setLatitude(new BigDecimal("39.90923"));
        demand.setSubject("数学");
        demand.setGrade("高一");
        demand.setExpectPrice(new BigDecimal("100"));
        demand.setTeachMode(1);
        return demand;
    }

    @Nested
    @DisplayName("教师取消订单 → 需求恢复")
    class CancelOrderRestoreDemand {

        @Test
        @DisplayName("教师取消待确认订单，需求应恢复为上架状态")
        void cancelPendingOrder_shouldRestoreDemand() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "不想接了");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            DemandPost updated = captor.getValue();
            assertEquals(1, updated.getStatus());
            assertNull(updated.getMatchedTutorId());

            verify(geoService).addDemandLocation(eq(DEMAND_ID), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("教师取消已支付订单，需求应恢复且退款")
        void cancelPaidOrder_shouldRestoreDemandAndRefund() {
            CourseOrder order = buildOrder(1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);
            when(walletService.unfreeze(anyLong(), any())).thenReturn(true);
            when(walletService.recharge(anyLong(), any())).thenReturn(true);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "临时有事");

            verify(walletService).unfreeze(TUTOR_ID, order.getTutorAmount());
            verify(walletService).recharge(PARENT_ID, order.getTotalAmount());

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
            assertNull(captor.getValue().getMatchedTutorId());

            verify(geoService).addDemandLocation(eq(DEMAND_ID), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("订单无关联需求，取消不应报错")
        void cancelOrder_noDemand_shouldNotThrow() {
            CourseOrder order = buildOrder(-1, null, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);

            assertDoesNotThrow(() -> orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消"));
            verify(demandPostMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("需求已被其他教师匹配，不应恢复")
        void cancelOrder_demandMatchedOtherTutor_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, OTHER_TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            verify(demandPostMapper, never()).updateById(any());
            verify(geoService, never()).addDemandLocation(anyLong(), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("需求已完成，不应恢复")
        void cancelOrder_demandCompleted_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(3, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            verify(demandPostMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("需求已处于上架未匹配状态，不应重复恢复")
        void cancelOrder_demandAlreadyActive_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(1, null);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            verify(demandPostMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("需求matchedTutorId为null但status=2（家长发起订单场景），应恢复")
        void cancelOrder_matchedTutorIdNullButStatusMatched_shouldRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, null);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
            assertNull(captor.getValue().getMatchedTutorId());
        }

        @Test
        @DisplayName("需求无经纬度，应跳过GEO索引但仍然恢复状态")
        void cancelOrder_demandNoCoordinates_shouldRestoreSkipGeo() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);
            demand.setLongitude(null);
            demand.setLatitude(null);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());

            verify(geoService, never()).addDemandLocation(anyLong(), anyDouble(), anyDouble());
        }
    }

    @Nested
    @DisplayName("家长拒绝接单 → 需求恢复")
    class ParentRejectRestoreDemand {

        @Test
        @DisplayName("家长拒绝待确认订单，需求应恢复")
        void parentReject_shouldRestoreDemand() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.parentRejectOrder(PARENT_ID, ORDER_ID, "不合适");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
            assertNull(captor.getValue().getMatchedTutorId());

            verify(geoService).addDemandLocation(eq(DEMAND_ID), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("家长拒绝时需求matchedTutorId为null，仍应恢复")
        void parentReject_matchedTutorIdNull_shouldRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, null);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.parentRejectOrder(PARENT_ID, ORDER_ID, "不合适");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("家长拒绝时需求已被其他教师匹配，不应恢复")
        void parentReject_demandMatchedOtherTutor_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, OTHER_TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.parentRejectOrder(PARENT_ID, ORDER_ID, "不合适");

            verify(demandPostMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("教师拒绝接单 → 需求恢复")
    class TutorRejectRestoreDemand {

        @Test
        @DisplayName("教师拒绝待确认订单，需求应恢复")
        void tutorReject_shouldRestoreDemand() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.tutorRejectOrder(TUTOR_ID, ORDER_ID, "时间冲突");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
            assertNull(captor.getValue().getMatchedTutorId());

            verify(geoService).addDemandLocation(eq(DEMAND_ID), anyDouble(), anyDouble());
        }

        @Test
        @DisplayName("教师拒绝时需求matchedTutorId为null，仍应恢复")
        void tutorReject_matchedTutorIdNull_shouldRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, null);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.tutorRejectOrder(TUTOR_ID, ORDER_ID, "时间冲突");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("教师拒绝时需求已被其他教师匹配，不应恢复")
        void tutorReject_demandMatchedOtherTutor_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, OTHER_TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.tutorRejectOrder(TUTOR_ID, ORDER_ID, "时间冲突");

            verify(demandPostMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCases {

        @Test
        @DisplayName("需求不存在，取消订单不应报错")
        void cancelOrder_demandNotFound_shouldNotThrow() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(null);

            assertDoesNotThrow(() -> orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消"));
            verify(demandPostMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("GEO索引添加失败，需求状态仍应恢复")
        void cancelOrder_geoIndexFails_demandStillRestored() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);
            doThrow(new RuntimeException("Redis连接失败"))
                    .when(geoService).addDemandLocation(anyLong(), anyDouble(), anyDouble());

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            assertEquals(1, captor.getValue().getStatus());
            assertNull(captor.getValue().getMatchedTutorId());
        }

        @Test
        @DisplayName("非订单参与方取消订单应抛出异常")
        void cancelOrder_notOwner_shouldThrow() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);

            assertThrows(BusinessException.class,
                    () -> orderService.cancelOrder(OTHER_TUTOR_ID, ORDER_ID, "取消"));
        }

        @Test
        @DisplayName("订单状态不允许取消应抛出异常")
        void cancelOrder_invalidStatus_shouldThrow() {
            CourseOrder order = buildOrder(2, DEMAND_ID, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);

            assertThrows(BusinessException.class,
                    () -> orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消"));
        }

        @Test
        @DisplayName("需求状态为0（下架），取消订单不应恢复为上架")
        void cancelOrder_demandOffline_shouldNotRestore() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(0, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            verify(demandPostMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("恢复后需求status=1且matchedTutorId=null，与正常上架需求查询条件一致")
        void restoredDemand_shouldMatchNormalDemandQueryCriteria() {
            CourseOrder order = buildOrder(-1, DEMAND_ID, TUTOR_ID);
            DemandPost demand = buildDemand(2, TUTOR_ID);

            doReturn(order).when(orderService).getById(ORDER_ID);
            when(demandPostMapper.selectById(DEMAND_ID)).thenReturn(demand);

            orderService.cancelOrder(TUTOR_ID, ORDER_ID, "取消");

            ArgumentCaptor<DemandPost> captor = ArgumentCaptor.forClass(DemandPost.class);
            verify(demandPostMapper).updateById(captor.capture());
            DemandPost restored = captor.getValue();
            assertEquals(1, restored.getStatus(), "恢复后status应为1(上架)");
            assertNull(restored.getMatchedTutorId(), "恢复后matchedTutorId应为null");
        }
    }
}
