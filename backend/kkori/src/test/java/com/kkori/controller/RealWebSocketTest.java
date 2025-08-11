package com.kkori.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkori.dto.interview.request.CommonRoomRequest;
import com.kkori.dto.interview.request.RoomCreateRequest;
import com.kkori.dto.interview.response.RoomCreateResponse;
import com.kkori.dto.interview.response.RoomStatusResponse;
import com.kkori.entity.User;
import com.kkori.jwt.Token;
import com.kkori.jwt.TokenProvider;
import com.kkori.service.InterviewSessionService;
import com.kkori.test.helper.WebSocketTestHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("실제 JWT 쿠키 기반 WebSocket 테스트")
class RealWebSocketTest {

    @LocalServerPort
    private int port;

    @MockBean
    private InterviewSessionService interviewSessionService;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private WebSocketTestHelper testHelper;

    private StompSession stompSession;
    private ObjectMapper objectMapper;
    private WebSocketTestHelper.MessageSubscriber personalSubscriber;

    private final Long TEST_USER_ID = 123L;
    private final String TEST_ROOM_ID = "REAL_ROOM_456";
    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();

        // 테스트용 User 객체 생성
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .sub("test-kakao-123456")
                .nickname("테스트사용자")
                .deleted(false)
                .build();

        // User 객체로 JWT 토큰 생성
        Token tokenObject = tokenProvider.generateAccessToken(testUser);
        jwtToken = tokenObject.getToken();
        // WebSocket 연결
        stompSession = testHelper.createRealTestSession(port, jwtToken, TEST_USER_ID);
        personalSubscriber = testHelper.subscribeToRealPersonalQueue(stompSession, TEST_USER_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (personalSubscriber != null) {
            personalSubscriber.unsubscribe();
        }

        Thread.sleep(1000);

        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
    }

    @Test
    @DisplayName("실제 JWT 쿠키로 방 생성 테스트 (혼자 연습하기)")
    void realJWTRoomCreateTest() throws Exception {
        // given
        String expectedRoomId = "REAL_CREATED_ROOM_789";
        given(interviewSessionService.createSoloRoom(1L, TEST_USER_ID))
                .willReturn(expectedRoomId);

        // RoomCreateRequest 직접 전송
        RoomCreateRequest request = new RoomCreateRequest("SOLO_PRACTICE", 1L);

        // when
        stompSession.send("/app/room-create", request);

        try {
            Map<String, Object> response = personalSubscriber.waitForMessage("room-created", 10);

            assertThat(response).isNotNull();
            assertThat(response.get("type")).isEqualTo("room-created");

            // 응답 데이터에서 방 정보 확인
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
            RoomCreateResponse roomCreateResponse = objectMapper.convertValue(dataMap, RoomCreateResponse.class);
            assertThat(roomCreateResponse.getRoomId()).isEqualTo(expectedRoomId);
        } catch (AssertionError e) {
            // 대기 중인 모든 메시지 확인
            printQueuedMessages();
            throw e;
        }
    }

    @Test
    @DisplayName("실제 JWT 쿠키로 방 상태 조회 테스트")
    void realJWTRoomStatusTest() throws Exception {
        // given
        CommonRoomRequest request = new CommonRoomRequest(TEST_ROOM_ID);

        // when
        stompSession.send("/app/room-status", request);
        Thread.sleep(500);

        try {
            Map<String, Object> response = personalSubscriber.waitForMessage("room-status", 10);

            assertThat(response).isNotNull();
            assertThat(response.get("type")).isEqualTo("room-status");

            // 응답 데이터에서 방 상태 정보 확인
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
            RoomStatusResponse roomStatusResponse = objectMapper.convertValue(dataMap, RoomStatusResponse.class);
            assertThat(roomStatusResponse.getStatus()).isEqualTo("WAITING");
            assertThat(roomStatusResponse.getUserCount()).isEqualTo(1);
            assertThat(roomStatusResponse.getMaxUsers()).isEqualTo(2);


        } catch (AssertionError e) {
            printQueuedMessages();
            throw e;
        }
    }

    @Test
    @DisplayName("실제 인터셉터 동작 확인 - 인증 실패 시뮬레이션")
    void realInterceptorAuthFailureTest() throws Exception {
        // 잘못된 JWT로 연결 시도 테스트

        String invalidToken = "invalid.jwt.token";

        try {
            StompSession failSession = testHelper.createRealTestSession(port, invalidToken, TEST_USER_ID);
            failSession.disconnect();
        } catch (Exception e) {
            // 예상대로 연결 실패
        }
    }

    @Test
    @DisplayName("JWT 토큰 정보 확인")
    void jwtTokenInfoTest() throws Exception {
        assertThat(tokenProvider.validateToken(jwtToken)).isTrue();
        assertThat(tokenProvider.getUserIdFromToken(jwtToken)).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("실제 JWT 쿠키로 2명 사용자 방 입장 테스트")
    void realJWTTwoUsersRoomTest() throws Exception {
        // given - 두 번째 사용자 설정
        final Long TEST_USER2_ID = 456L;
        User testUser2 = User.builder()
                .userId(TEST_USER2_ID)
                .sub("test-kakao-456789")
                .nickname("테스트사용자2")
                .deleted(false)
                .build();

        Token tokenObject2 = tokenProvider.generateAccessToken(testUser2);
        String jwtToken2 = tokenObject2.getToken();

        // 두 번째 사용자 WebSocket 연결 및 구독
        StompSession stompSession2 = testHelper.createRealTestSession(port, jwtToken2, TEST_USER2_ID);
        WebSocketTestHelper.MessageSubscriber personalSubscriber2 =
                testHelper.subscribeToRealPersonalQueue(stompSession2, TEST_USER2_ID);

        try {
            // 방 생성 모킹
            String testRoomId = "TWO_USER_ROOM_123";
            given(interviewSessionService.createPairRoom(1L, TEST_USER_ID))
                    .willReturn(testRoomId);

            // 1: 첫 번째 사용자가 함께 연습하기 방 생성
            RoomCreateRequest createRequest = new RoomCreateRequest("PAIR_INTERVIEW", 1L);
            stompSession.send("/app/room-create", createRequest);

            // 방 생성 응답 확인
            Map<String, Object> createResponse = personalSubscriber.waitForMessage("room-created", 10);

            @SuppressWarnings("unchecked")
            Map<String, Object> createDataMap = (Map<String, Object>) createResponse.get("data");
            RoomCreateResponse roomCreateResponse = objectMapper.convertValue(createDataMap, RoomCreateResponse.class);
            String createdRoomId = roomCreateResponse.getRoomId();

            // 2: 방 토픽 구독 (두 사용자 모두)
            List<StompSession> sessions = Arrays.asList(stompSession, stompSession2);
            List<WebSocketTestHelper.MessageSubscriber> roomSubscribers = subscribeUsersToRoom(sessions, createdRoomId);
            WebSocketTestHelper.MessageSubscriber roomSubscriber1 = roomSubscribers.get(0);
            WebSocketTestHelper.MessageSubscriber roomSubscriber2 = roomSubscribers.get(1);

            // 3: 두 번째 사용자가 방 입장
            CommonRoomRequest joinRequest = new CommonRoomRequest(createdRoomId);
            stompSession2.send("/app/room-join", joinRequest);

            // 4: 입장 관련 알림들 확인
            Map<String, Object> user1Notification = roomSubscriber1.waitForMessage("user-joined", 10);
            Map<String, Object> user2Notification = roomSubscriber2.waitForMessage("user-joined", 10);

            // 5: 검증
            assertThat(user1Notification.get("type")).isEqualTo("user-joined");
            assertThat(user2Notification.get("type")).isEqualTo("user-joined");

            // SuccessResponse 구조 확인
            @SuppressWarnings("unchecked")
            Map<String, Object> joinDataMap = (Map<String, Object>) user1Notification.get("data");
            assertThat(joinDataMap.get("message")).isEqualTo("새 사용자가 참여했습니다");

            // 정리
            unsubscribeSafe(roomSubscriber1);
            unsubscribeSafe(roomSubscriber2);

        } finally {
            // 두 번째 사용자 연결 정리
            unsubscribeSafe(personalSubscriber2);
            disconnectSafe(stompSession2);
        }
    }

    @Test
    @DisplayName("에러 응답 처리 테스트")
    void errorResponseTest() throws Exception {
        // given - 존재하지 않는 모드로 방 생성 시도
        RoomCreateRequest invalidRequest = new RoomCreateRequest("INVALID_MODE", 1L);


        // when
        stompSession.send("/app/room-create", invalidRequest);

        // then
        try {
            Map<String, Object> response = personalSubscriber.waitForMessage("error", 10);

            assertThat(response).isNotNull();
            assertThat(response.get("type")).isEqualTo("error");

        } catch (AssertionError e) {
            printQueuedMessages();
            throw e;
        }
    }

    @Test
    @DisplayName("역할 바꾸기 WebSocket API 테스트")
    void rolesSwapTest() throws Exception {
        // given - 테스트 방 ID 설정 및 서비스 모킹
        String testRoomId = "TEST_ROLE_SWAP_ROOM_789";
        doNothing().when(interviewSessionService).swapRoles(testRoomId);
        
        // 방 토픽 구독 (역할 바꾸기 알림을 받기 위해)
        WebSocketTestHelper.MessageSubscriber roomSubscriber = 
                testHelper.subscribeToRealRoomTopic(stompSession, testRoomId);
        
        try {
            // when - 역할 바꾸기 요청 전송
            CommonRoomRequest swapRequest = new CommonRoomRequest(testRoomId);
            stompSession.send("/app/roles-swap", swapRequest);
            
            // then - 역할 바뀜 알림 수신 확인
            Map<String, Object> response = roomSubscriber.waitForMessage("roles-swapped", 10);
            
            assertThat(response).isNotNull();
            assertThat(response.get("type")).isEqualTo("roles-swapped");
            
            // SuccessResponse 구조 확인
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
            assertThat(dataMap.get("message")).isEqualTo("면접관과 면접자 역할이 변경되었습니다");
            assertThat(dataMap.get("timestamp")).isNotNull();
            
        } catch (AssertionError e) {
            printQueuedMessages();
            throw e;
        } finally {
            // 정리
            unsubscribeSafe(roomSubscriber);
        }
    }

    @Test
    @DisplayName("역할 바꾸기 실패 시 에러 응답 테스트") 
    void rolesSwapErrorTest() throws Exception {
        // given - 서비스에서 예외 발생하도록 설정
        String testRoomId = "INVALID_ROOM_FOR_ROLE_SWAP";
        doThrow(new RuntimeException("역할 변경 실패"))
                .when(interviewSessionService).swapRoles(testRoomId);
        
        try {
            // when - 역할 바꾸기 요청 전송
            CommonRoomRequest swapRequest = new CommonRoomRequest(testRoomId);
            stompSession.send("/app/roles-swap", swapRequest);
            
            // then - 에러 응답 수신 확인
            Map<String, Object> response = personalSubscriber.waitForMessage("error", 10);
            
            assertThat(response).isNotNull();
            assertThat(response.get("type")).isEqualTo("error");
            
            // ErrorResponse 구조 확인
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
            assertThat(dataMap.get("error")).isEqualTo("역할 변경에 실패했습니다.");
            assertThat(dataMap.get("message")).isEqualTo("역할 변경 실패");
            assertThat(dataMap.get("timestamp")).isNotNull();
            
        } catch (AssertionError e) {
            printQueuedMessages();
            throw e;
        }
    }

    // ==================== 헬퍼 메서드들 ====================
    
    private List<WebSocketTestHelper.MessageSubscriber> subscribeUsersToRoom(List<StompSession> sessions, String roomId) {
        return sessions.stream()
                .map(session -> {
                    try {
                        return testHelper.subscribeToRealRoomTopic(session, roomId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    private void unsubscribeSafe(WebSocketTestHelper.MessageSubscriber subscriber) {
        if (subscriber != null) {
            subscriber.unsubscribe();
        }
    }

    private void disconnectSafe(StompSession session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private void printQueuedMessages() {
        Map<String, Object> anyMessage;
        int count = 0;
        while ((anyMessage = personalSubscriber.getMessages().poll()) != null && count < 10) {
            count++;
        }
    }
}