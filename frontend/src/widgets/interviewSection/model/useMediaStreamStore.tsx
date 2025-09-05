import { create } from 'zustand';
import { MediaStreamType } from '@/widgets/interviewSection/model/types';

interface MediaStreamState {
  myStream: MediaStream | null;
  peerStream: MediaStream | null;
  isMyVideoOn: boolean;
  isMyAudioOn: boolean;
  isPeerVideoOn: boolean;
  isPeerAudioOn: boolean;
  mainStreamType: MediaStreamType;
  subStreamType: MediaStreamType;
  myRecorder: MediaRecorder | null;
  blob: Blob | null;
  timerId: NodeJS.Timeout | null;

  isReady: boolean;
  error: string | null;

  _readyPromise: Promise<MediaStream> | null;
  _resolveReady?: (s: MediaStream) => void;
  _rejectReady?: (e: unknown) => void;
}

interface MediaStreamActions {
  setMyStream: (myStream: MediaStream) => void;
  setMyRecorder: (myRecorder: MediaRecorder) => void;
  setPeerStream: (peerStream: MediaStream) => void;
  setIsMyVideoOn: (isMyVideoOn: boolean) => void;
  setIsMyAudioOn: (isMyAudioOn: boolean) => void;
  setIsPeerVideoOn: (isPeerVideoOn: boolean) => void;
  setIsPeerAudioOn: (isPeerAudioOn: boolean) => void;
  reset: () => void;
  setMainStreamType: (mainStreamType: MediaStreamType) => void;
  setSubStreamType: (subStreamType: MediaStreamType) => void;
  setBlob: (blob: Blob | null) => void;
  setTimerId: (timerId: NodeJS.Timeout | null) => void;
  initMyStream: () => Promise<MediaStream>;
  waitForReady: () => Promise<MediaStream>;
}

const initialState: Omit<
  MediaStreamState,
  '_readyPromise' | '_resolveReady' | '_rejectReady'
> = {
  myStream: null,
  peerStream: null,
  isMyVideoOn: false,
  isMyAudioOn: false,
  isPeerVideoOn: false,
  isPeerAudioOn: false,
  mainStreamType: 'my',
  subStreamType: 'peer',
  myRecorder: null,
  blob: null,
  timerId: null,

  isReady: false,
  error: null,
};

const useMediaStreamStore = create<MediaStreamState & MediaStreamActions>(
  (set, get) => ({
    ...initialState,
    _readyPromise: null,

    setMyStream: myStream => set({ myStream }),
    setMyRecorder: myRecorder => set({ myRecorder }),
    setPeerStream: peerStream => set({ peerStream }),
    setIsMyVideoOn: isMyVideoOn => set({ isMyVideoOn }),
    setIsMyAudioOn: isMyAudioOn => set({ isMyAudioOn }),
    setIsPeerVideoOn: isPeerVideoOn => set({ isPeerVideoOn }),
    setIsPeerAudioOn: isPeerAudioOn => set({ isPeerAudioOn }),
    setMainStreamType: mainStreamType => set({ mainStreamType }),
    setSubStreamType: subStreamType => set({ subStreamType }),
    setBlob: blob => set({ blob }),
    setTimerId: timerId => set({ timerId }),

    reset: () => {
      const { myStream, myRecorder } = get();
      try {
        myStream?.getTracks().forEach(t => t.stop());
      } catch {}
      try {
        if (myRecorder && myRecorder.state !== 'inactive') myRecorder.stop();
      } catch {}

      set({
        ...initialState,
        _readyPromise: null,
        _resolveReady: undefined,
        _rejectReady: undefined,
      });
    },

    initMyStream: async () => {
      const { myStream, _readyPromise } = get();

      if (myStream) return myStream;

      if (_readyPromise) return _readyPromise;
      const { promise, resolve, reject } = Promise.withResolvers<MediaStream>();

      set({
        _readyPromise: promise,
        _resolveReady: resolve,
        _rejectReady: reject,
        error: null,
      });

      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: {
            echoCancellation: true,
            noiseSuppression: true,
            autoGainControl: true,
          },
        });

        const audioOnly = new MediaStream(stream.getAudioTracks());
        const recorder = new MediaRecorder(audioOnly);

        set({
          myStream: stream,
          myRecorder: recorder,
          isMyVideoOn: true,
          isMyAudioOn: true,
          isReady: true,
        });

        resolve(stream);
        return promise;
      } catch (e) {
        set({ error: (e as Error)?.message ?? 'getUserMedia 실패' });
        get()._rejectReady?.(e);
        set({
          _readyPromise: null,
          _resolveReady: undefined,
          _rejectReady: undefined,
          isReady: false,
        });
        throw e;
      }
    },

    waitForReady: () => {
      const { myStream, _readyPromise, initMyStream } = get();
      if (myStream) return Promise.resolve(myStream);
      if (_readyPromise) return _readyPromise;
      return initMyStream();
    },
  }),
);

export default useMediaStreamStore;
