package com.kkori.message;

/**
 * 면접 관련 메시지 상수 관리 클래스
 */
public final class InterviewMessages {

    private InterviewMessages() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    /**
     * GPT 꼬리질문 생성을 위한 시스템 메시지
     */
    public static final String TAIL_QUESTION_SYSTEM_MESSAGE =
            "You are an interview assistant. Always generate 2 follow-up questions regarding the most recent answer. " +
                    "The questions must be highly relevant and coherent based on both the content and context of the interview. " +
                    "If the most recent answer or the previous question is off-topic or unrelated to the intended interview flow, " +
                    "ignore them and instead generate the 2 most suitable and contextually appropriate follow-up questions " +
                    "based on the overall interview context. Do not generate any follow-up questions that are unrelated or " +
                    "irrelevant to the context.\n" +
                    "The answer format must always be as follows:\n" +
                    "`방금 말씀하신 프로젝트에 대해 더 상세히 말씀해주세요.\n동시성 문제를 해결하기 위해 synchronized 키워드를 사용한다고 말씀하셨는데, 다른 방법은 없나요?`";

    // ==================== WebSocket 응답 메시지 ==================== 
    
    /**
     * 사용자 참여/퇴장 관련 메시지
     */
    public static final String USER_JOINED = "새 사용자가 참여했습니다";
    public static final String USER_EXITED = "사용자가 방을 나갔습니다";
    
    /**
     * 역할 변경 관련 메시지
     */
    public static final String ROLES_SWAPPED = "면접관과 면접자 역할이 변경되었습니다";
    
    /**
     * 면접 진행 관련 메시지
     */
    public static final String INTERVIEW_COMPLETED = "면접이 완료되었습니다";
    
    /**
     * 녹음 관련 메시지
     */
    public static final String ANSWER_RECORDING_STARTED = "답변 녹음이 시작되었습니다";
    public static final String CUSTOM_QUESTION_RECORDING_STARTED = "커스텀 질문 녹음이 시작되었습니다";
}
