import ChattingWindowContainer from '@/widgets/chattingWindow';
import InterviewSection from '@/widgets/interviewSection';
import { usePracticeSessionStore } from '@/shared/lib/usePracticeSessionStore';
import { useEffect, useState } from 'react';
import useInterviewRoomStore, {
  interviewStatus,
} from '@/entities/interviewRoom/model/useInterviewRoomStore';
import NextQuestionModal from '@/widgets/interviewSection/ui/NextQuestionModal';
import { useModal } from '@/shared/lib/useModal';
import { useInterviewQuestionStore } from '@/widgets/interviewSection/model/useInterviewQuestionStore';
import { usePracticeSessionHydrated } from '@/shared/lib/usePracticeSessionHydrated';
import { useInterviewRoomHydrated } from '@/entities/interviewRoom/model/useInterviewRoomHydrated';
import useMediaStreamStore from '@/widgets/interviewSection/model/useMediaStreamStore';
function PairPracticePage() {
  const { connect, disconnect } = usePracticeSessionStore();
  const practiceSessionHydrated = usePracticeSessionHydrated();
  const interviewRoomHydrated = useInterviewRoomHydrated();

  if (!practiceSessionHydrated || !interviewRoomHydrated) return null;

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { initMyStream } = useMediaStreamStore.getState();
        await initMyStream();
      } catch (e) {
        console.error('getUserMedia failed', e);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    useInterviewRoomStore
      .getState()
      .setStatus(interviewStatus.BEFORE_INTERVIEW);
    connect();
    return () => {
      disconnect();
      useInterviewQuestionStore.getState().clearCurrentQuestion();
      nextQuestionModal.close();
    };
  }, []);

  const nextQuestionModal = useModal();
  const handleNextQuestionModalClose = () => {
    nextQuestionModal.close();
  };
  return (
    <main
      aria-label={`pair-practice-page`}
      className="flex h-full max-h-screen w-full items-center justify-center gap-5 px-8"
    >
      {nextQuestionModal.isOpen && (
        <NextQuestionModal
          onClose={handleNextQuestionModalClose}
          contentRef={nextQuestionModal.contentRef}
        />
      )}
      <InterviewSection openModal={nextQuestionModal.open} />
      <ChattingWindowContainer />
    </main>
  );
}

export default PairPracticePage;
