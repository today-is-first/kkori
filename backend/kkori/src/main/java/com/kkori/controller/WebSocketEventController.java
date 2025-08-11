package com.kkori.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkori.component.interview.QuestionForm;
import com.kkori.dto.interview.QuestionDto;
import com.kkori.dto.interview.request.AnswerSubmitRequest;
import com.kkori.dto.interview.request.CommonRoomRequest;
import com.kkori.dto.interview.request.CustomQuestionCreateRequest;
import com.kkori.dto.interview.request.NextQuestionSelectRequest;
import com.kkori.dto.interview.request.RoomCreateRequest;
import com.kkori.dto.interview.response.NextQuestionChoicesResponse;
import com.kkori.dto.interview.response.STTResultResponse;
import com.kkori.dto.interview.response.ErrorResponse;
import com.kkori.dto.interview.response.InterviewStartResponse;
import com.kkori.dto.interview.response.RoomCreateResponse;
import com.kkori.dto.interview.response.RoomStatusResponse;
import com.kkori.dto.interview.response.SuccessResponse;
import com.kkori.exception.ExceptionCode;
import com.kkori.exception.user.UserException;
import com.kkori.message.InterviewMessages;
import com.kkori.service.InterviewSessionService;
import com.kkori.component.interview.InterviewRoom;
import com.kkori.dto.websocket.WebSocketResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 면접 실시간 WebSocket 이벤트 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class WebSocketEventController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final InterviewSessionService interviewSessionService;

    private static final String ROOM_TOPIC_PREFIX = "/topic/interview/";
    private static final String USER_QUEUE = "/queue/interview";

    // ==================== 방 관리 ====================

    @MessageMapping("/room-create")
    public void handleRoomCreate(@Payload RoomCreateRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = createRoom(request, authenticatedUserId);
            RoomCreateResponse response = new RoomCreateResponse(roomId);
            sendPersonalMessage(authenticatedUserId, "room-created", response);
        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.ROOM_CREATE_FAILED, e.getMessage());
        }
    }

    @MessageMapping("/room-join")
    public void handleRoomJoin(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            interviewSessionService.joinRoom(roomId, authenticatedUserId);

            SuccessResponse broadcastResponse = new SuccessResponse(
                    InterviewMessages.USER_JOINED,
                    String.valueOf(System.currentTimeMillis())
            );
            broadcastToRoom(roomId, "user-joined", broadcastResponse);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.ROOM_JOIN_FAILED, e.getMessage());
        }
    }

    @MessageMapping("/room-exit")
    public void handleRoomExit(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            interviewSessionService.exitRoom(roomId, authenticatedUserId);

            SuccessResponse broadcastResponse = new SuccessResponse(
                    InterviewMessages.USER_EXITED,
                    String.valueOf(System.currentTimeMillis())
            );
            broadcastToRoom(roomId, "user-exited", broadcastResponse);

        } catch (Exception e) {
            // 방 나가기는 항상 성공으로 처리
        }
    }

    @MessageMapping("/room-status")
    public void handleRoomStatus(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();

            // TODO: 실제 방 상태 조회 로직 구현 (필요하다면)
            RoomStatusResponse response = new RoomStatusResponse("WAITING", 1, 2);

            sendPersonalMessage(authenticatedUserId, "room-status", response);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.ROOM_STATUS_FAILED, e.getMessage());
        }
    }

    @MessageMapping("/roles-swap")
    public void handleRolesSwap(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            interviewSessionService.swapRoles(roomId);

            SuccessResponse response = new SuccessResponse(
                    InterviewMessages.ROLES_SWAPPED,
                    String.valueOf(System.currentTimeMillis())
            );
            broadcastToRoom(roomId, "roles-swapped", response);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.ROLE_SWAP_FAILED, e.getMessage());
        }
    }

    // ==================== 면접 진행 ====================

    @MessageMapping("/interview-start")
    public void handleInterviewStart(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();

            // 면접 시작 및 저장
            interviewSessionService.startInterview(roomId, authenticatedUserId);

            // 첫 번째 질문 조회
            QuestionForm currentQuestion = interviewSessionService.getCurrentQuestion(roomId);
            QuestionDto questionDto = QuestionDto.from(currentQuestion);

            InterviewStartResponse response = new InterviewStartResponse(questionDto);
            broadcastToRoom(roomId, "interview-started", response);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.INTERVIEW_START_FAILED, e.getMessage());
        }
    }

    @MessageMapping("/interview-end")
    public void handleInterviewEnd(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            interviewSessionService.completeInterview(roomId);

            SuccessResponse response = new SuccessResponse(
                    InterviewMessages.INTERVIEW_COMPLETED,
                    String.valueOf(System.currentTimeMillis())
            );
            broadcastToRoom(roomId, "interview-ended", response);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.INTERVIEW_END_FAILED, e.getMessage());
        }
    }

    // ==================== 답변 처리 ====================

    @MessageMapping("/answer-start")
    public void handleAnswerStart(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        String roomId = request.getRoomId();
        SuccessResponse response = new SuccessResponse(
                InterviewMessages.ANSWER_RECORDING_STARTED,
                String.valueOf(System.currentTimeMillis())
        );
        broadcastToRoom(roomId, "answer-recording-started", response);
    }

    @MessageMapping("/answer-submit")
    public void handleAnswerSubmit(@Payload AnswerSubmitRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            String audioBase64 = request.getAudioBase64();

            // 1단계: STT 처리 및 답변 저장
            String transcribedText = interviewSessionService.processAudioAnswer(
                    roomId, authenticatedUserId, audioBase64);

            // 1단계: STT 결과를 방 전체에 먼저 브로드캐스트
            STTResultResponse sttResponse = new STTResultResponse(transcribedText);
            broadcastToRoom(roomId, "stt-result", sttResponse);

            // 2단계: 방 정보를 가져와서 면접관 확인 후 질문 선택지를 면접관에게만 개인 메시지로 전송
            InterviewRoom room = interviewSessionService.getRoom(roomId);
            Long interviewerId = room.getInterviewerId();
            
            if (interviewerId != null) {
                List<QuestionForm> nextQuestions = interviewSessionService.getNextQuestions(roomId);
                List<QuestionDto> nextQuestionDtos = nextQuestions.stream()
                        .map(QuestionDto::from)
                        .toList();

                NextQuestionChoicesResponse choicesResponse = new NextQuestionChoicesResponse(nextQuestionDtos);
                sendPersonalMessage(interviewerId, "next-question-choices", choicesResponse);
            }

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.ANSWER_PROCESSING_FAILED, e.getMessage());
        }
    }

    // ==================== 질문 관리 ====================

    @MessageMapping("/next-question-select")
    public void handleNextQuestionSelect(@Payload NextQuestionSelectRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();

            QuestionForm selectedQuestion = interviewSessionService.selectQuestion(
                    roomId,
                    com.kkori.component.interview.QuestionType.valueOf(request.getQuestionType()),
                    request.getQuestionId(),
                    request.getQuestionText());

            QuestionDto questionDto = QuestionDto.from(selectedQuestion);
            broadcastToRoom(roomId, "next-question-selected", questionDto);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.QUESTION_SELECT_FAILED, e.getMessage());
        }
    }

    @MessageMapping("/custom-question-start")
    public void handleCustomQuestionStart(@Payload CommonRoomRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        String roomId = request.getRoomId();
        SuccessResponse response = new SuccessResponse(
                InterviewMessages.CUSTOM_QUESTION_RECORDING_STARTED,
                String.valueOf(System.currentTimeMillis())
        );
        broadcastToRoom(roomId, "custom-question-recording-started", response);
    }

    @MessageMapping("/custom-question-create")
    public void handleCustomQuestionCreate(@Payload CustomQuestionCreateRequest request, SimpMessageHeaderAccessor headerAccessor) {
        Long authenticatedUserId = requireAuthenticatedUserId(headerAccessor);

        try {
            String roomId = request.getRoomId();
            String audioBase64 = request.getAudioBase64();

            QuestionForm customQuestion = interviewSessionService.createCustomQuestion(
                    roomId, audioBase64);

            QuestionDto questionDto = QuestionDto.from(customQuestion);
            broadcastToRoom(roomId, "custom-question-created", questionDto);

        } catch (Exception e) {
            sendErrorToUser(authenticatedUserId, ExceptionCode.AUDIO_TRANSCRIPTION_FAILED.getMessage(), e.getMessage());
        }
    }

    // ==================== 헬퍼 메서드 ====================

    private Long getAuthenticatedUserId(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (userId == null) {
            sendErrorToUser(null, ExceptionCode.WEBSOCKET_AUTHENTICATION_FAILED);
        }
        return userId;
    }

    /**
     * @throws UserException 인증되지 않은 사용자인 경우
     */
    private Long requireAuthenticatedUserId(SimpMessageHeaderAccessor headerAccessor) {
        Long userId = getAuthenticatedUserId(headerAccessor);
        if (userId == null) {
            throw UserException.webSocketAuthenticationFailed();
        }
        return userId;
    }

    private String createRoom(RoomCreateRequest request, Long userId) {
        return switch (request.getMode()) {
            case "SOLO_PRACTICE" -> interviewSessionService.createSoloRoom(request.getQuestionSetId(), userId);
            case "PAIR_INTERVIEW" -> interviewSessionService.createPairRoom(request.getQuestionSetId(), userId);
            default -> throw new IllegalArgumentException("지원하지 않는 모드: " + request.getMode());
        };
    }


    private void sendPersonalMessage(Long userId, String messageType, Object responseData) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    USER_QUEUE,
                    createResponse(messageType, responseData)
            );
        } catch (Exception e) {
            // 개인 메시지 전송 실패 시 무시
        }
    }

    private void broadcastToRoom(String roomId, String messageType, Object responseData) {
        try {
            messagingTemplate.convertAndSend(
                    ROOM_TOPIC_PREFIX + roomId,
                    createResponse(messageType, responseData)
            );
        } catch (Exception e) {
            // 브로드캐스트 전송 실패 시 무시
        }
    }

    private void sendErrorToUser(Long userId, String error, String message) {
        if (userId != null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    error,
                    message,
                    String.valueOf(System.currentTimeMillis())
            );
            sendPersonalMessage(userId, "error", errorResponse);
        }
    }
    
    /**
     * ExceptionCode를 활용한 에러 전송
     */
    private void sendErrorToUser(Long userId, ExceptionCode exceptionCode, String detailMessage) {
        sendErrorToUser(userId, exceptionCode.getMessage(), detailMessage);
    }

    private void sendErrorToUser(Long userId, ExceptionCode exceptionCode) {
        sendErrorToUser(userId, exceptionCode.getMessage(), exceptionCode.getMessage());
    }

    private WebSocketResponse createResponse(String type, Object data) {
        return WebSocketResponse.of(type, data);
    }
}