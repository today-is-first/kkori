package com.kkori.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {

    // ==== Interview Room 관련 ====
    ROOM_NOT_FOUND(1001, "면접 방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_JOIN_ROOM(1002, "면접 방에 참여할 수 없습니다.", HttpStatus.CONFLICT),
    CANNOT_START_INTERVIEW(1003, "면접을 시작할 수 없습니다.", HttpStatus.CONFLICT),
    INTERVIEW_NOT_STARTED(1004, "면접이 시작되지 않았습니다.", HttpStatus.CONFLICT),
    INTERVIEW_NOT_IN_PROGRESS(1005, "진행 중인 면접이 없습니다.", HttpStatus.CONFLICT),
    BOTH_ROLES_REQUIRED(1006, "면접관과 면접자가 모두 필요합니다.", HttpStatus.PRECONDITION_FAILED),
    CANNOT_CHANGE_ROLES_AFTER_START(1007, "면접 시작 후에는 역할을 변경할 수 없습니다.", HttpStatus.LOCKED),
    USER_NOT_FOUND_IN_ROOM(1008, "방에서 해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // ==== Permission 관련 ====
    ONLY_INTERVIEWEE_CAN_SUBMIT_ANSWER(2001, "면접자만 답변을 제출할 수 있습니다.", HttpStatus.FORBIDDEN),
    PERMISSION_DENIED(2002, "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // ==== Question 관련 ====
    QUESTION_SET_NOT_FOUND(3001, "질문 세트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERVIEWER_NOT_FOUND(3002, "면접관 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERVIEWEE_NOT_FOUND(3003, "면접자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERVIEW_NOT_FOUND(3004, "면접 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DEFAULT_QUESTION_NOT_FOUND(3005, "기본 질문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TAIL_QUESTION_REQUIRES_PARENT(3006, "꼬리질문은 부모 질문이 필요합니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    PARENT_QUESTION_NOT_FOUND(3007, "부모 질문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),


    // ==== Audio Processing 관련 ====
    AUDIO_FILE_TOO_LARGE(4001, "음성 파일이 25MB를 초과합니다.", HttpStatus.PAYLOAD_TOO_LARGE),
    AUDIO_TRANSCRIPTION_FAILED(4002, "음성 변환에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AUDIO_API_CALL_FAILED(4003, "음성 변환 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),

    // ==== Tail Question Generation 관련 ====
    TAIL_QUESTION_GENERATION_FAILED(5001, "꼬리 질문 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    TAIL_QUESTION_API_CALL_FAILED(5002, "꼬리 질문 생성 API 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY),
    JSON_CONVERSION_FAILED(5003, "JSON 변환에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== User 관련 ====
    USER_NOT_FOUND(6001, "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    WEBSOCKET_AUTHENTICATION_FAILED(6002, "WebSocket 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS(6003, "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(6004, "접근이 거부되었습니다.", HttpStatus.FORBIDDEN),
    INVALID_USER_FORMAT(6005, "잘못된 사용자 ID 형식입니다.", HttpStatus.BAD_REQUEST),
    ADMIN_REQUIRED(6006, "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN),

    // ==== WebSocket 관련 ====
    ROOM_CREATE_FAILED(7001, "방 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROOM_JOIN_FAILED(7002, "방 참여에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROOM_STATUS_FAILED(7003, "방 상태 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERVIEW_START_FAILED(7004, "면접 시작에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERVIEW_END_FAILED(7005, "면접 종료에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANSWER_PROCESSING_FAILED(7006, "답변 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QUESTION_SELECT_FAILED(7007, "질문 선택에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_SWAP_FAILED(7008, "역할 변경에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== QuestionSet 생성 관련 ====
    EMPTY_QUESTIONS(8001, "질문이 비어있습니다.", HttpStatus.BAD_REQUEST),
    NO_TITLE(8002, "질문 세트 제목은 필수입니다.", HttpStatus.BAD_REQUEST),
    BLANK_TITLE(8003, "질문 세트 제목은 빈 문자열일 수 없습니다.", HttpStatus.BAD_REQUEST),
    NO_QUESTION_CONTENT(8004, "질문 내용은 필수입니다.", HttpStatus.BAD_REQUEST),
    NO_QUESTION_TYPE(8005, "질문 타입은 필수입니다.", HttpStatus.BAD_REQUEST),
    NOT_LOGGED_IN(8006, "로그인이 필요합니다.", HttpStatus.BAD_REQUEST),
    QUESTION_SET_INTERNAL_ERROR(8007, "질문 세트 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    QUESTION_SET_NOT_FOUND_EXCEPTION(8008, "질문 세트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NO_PERMISSION(8009, "질문 세트에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // ==== InterviewTailQuestion 관련 ====
    TAIL_QUESTION_NOT_FOUND(9001, "꼬리 질문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TAIL_QUESTION_ALREADY_ANSWERED(9002, "이미 답변한 꼬리 질문입니다.", HttpStatus.CONFLICT),
    INTERVIEW_ACCESS_DENIED(9003, "면접에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INTERVIEW_ALREADY_COMPLETED(9004, "이미 완료된 면접입니다.", HttpStatus.CONFLICT),
    INTERVIEW_CONCURRENCY_ERROR(9005, "동시 수정 오류가 발생했습니다.", HttpStatus.CONFLICT),

    // ==== 동시성 제어 관련 ====
    RESOURCE_LOCK_TIMEOUT(9010, "리소스 락 획득 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT),
    CONCURRENT_MODIFICATION_ERROR(9011, "동시 수정이 감지되었습니다.", HttpStatus.CONFLICT),

    // ==== InterviewRecord 관련 ====
    INTERVIEW_RECORD_NOT_FOUND(10001, "면접 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERVIEW_RECORD_ACCESS_DENIED(10002, "면접 기록에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INTERVIEW_RECORD_LIST_FETCH_FAILED(10003, "면접 기록 목록 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERVIEW_RECORD_DETAIL_FETCH_FAILED(10004, "면접 기록 상세 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ExceptionCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}