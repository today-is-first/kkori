
## 🗂️ 팀 문서 & 자료 링크

<p align="center">
  <a href="https://www.notion.so/229656e282d88069a69fe9a18ca1cc58?v=229656e282d88040a866000cc977e780">📒 팀 노션</a> &nbsp;|&nbsp;
  <a href="https://www.notion.so/230656e282d8801991e7fda7326561a8">📚 개발 위키</a> &nbsp;|&nbsp;
  <a href="https://www.notion.so/Ground-Rule-229656e282d88055a6bccf41c7cf1064">📏 그라운드 룰</a> &nbsp;|&nbsp;
<!--   <a href="https://kkori.site/">🚀 데모 페이지</a> -->
</p>

<br/>
<br/>

## 🧑‍💻 역할 및 기여

| 담당자 | 주요 작업                                                                                                                                                                                       |
|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **이찬** | - Lighthouse 성능 개선 (LCP 2.5s → 0.6s)<br>- [역할 기반 구조에서 기능 기반 구조(FSD)로 리팩토링](https://velog.io/@today-is-first/%EC%86%8C%EC%9E%83%EA%B3%A0-%ED%8F%B4%EB%8D%94-%EA%B5%AC%EC%A1%B0-%EA%B3%A0%EC%B9%98%EA%B8%B0)<br>- [프론트엔드 코드 고도화 및 리팩토링](https://velog.io/@today-is-first/%EC%B6%94%EC%83%81%ED%99%94%EB%8A%94-%ED%95%9C-%EB%81%97-%EC%B0%A8%EC%9D%B4)<br>- [OAuth 로그인 로직 설계 및 프론트 구현](https://velog.io/@today-is-first/%EB%8B%B9%EC%8B%A0%EC%9D%B4-%EB%A1%9C%EA%B7%B8%EC%9D%B8%EC%9D%84-%EA%B5%AC%ED%98%84%ED%95%A0-%EB%95%8C-%EB%B0%98%EB%93%9C%EC%8B%9C-%EC%95%8C%EC%95%84%EC%95%BC%ED%95%98%EB%8A%94-%EA%B2%83%EB%93%A4)<br>- 프론트엔드 테스트 코드 작성 [참고1](https://velog.io/@today-is-first/%EC%9D%B4%EB%B2%88-%ED%94%8C%EC%A0%9D%EC%97%90-TDD%EB%A5%BC-%EB%8F%84%EC%9E%85%ED%95%B4%EB%B3%BC%EA%B9%8C) [참고2](https://velog.io/@today-is-first/TDD%EB%A5%BC-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B3%A0-%EB%82%98%EC%9D%98-%EC%84%B1%EA%B3%B5%EC%8B%9C%EB%8C%80-%EC%8B%9C%EC%9E%91%EB%90%90%EB%8B%A4)<br>- [면접 질문 세트 버전 관리 구조 설계](https://velog.io/@today-is-first/%EC%9A%B0%EB%A6%AC-%EC%84%9C%EB%B9%84%EC%8A%A4%EC%97%90-%EB%A7%9E%EB%8A%94-%EC%8A%A4%ED%82%A4%EB%A7%88%EB%8A%94-%EB%AC%B4%EC%97%87%EC%9D%BC%EA%B9%8C)<br>- 면접 질문 세트 생성 / 수정 페이지 구현<br>- 내가 만든 세트 / 가져온 세트 마이페이지 구현 |


<br/>
<br/>

## 👋 프로젝트 소개

### 🎤 *"AI 기반 실시간 면접 연습 플랫폼"*

**Kkori**는 사용자가 **실시간 WebSocket 통신을 통해 면접을 연습**하고,  
**AI 기반 음성 인식과 꼬리질문 생성**으로 실제 면접과 같은 경험을 제공하는 **면접 연습 서비스**입니다.

- 사용자는 **혼자 연습(SOLO)** 또는 **둘이서 연습(PAIR)** 모드를 선택할 수 있어요.
- **실시간 음성 인식(STT)** 으로 답변을 텍스트로 변환하고 **AI가 꼬리질문을 자동 생성**합니다.
- **WebRTC 기반 영상/음성 통화**와 **실시간 채팅**으로 생생한 면접 환경을 제공해요.

<br/>
<br/>

## 🧩 **고민과 해결 방안**

### 🔄 **WebSocket 재연결 전략으로 안정적인 면접 환경 구축**

실시간 면접 시스템에서 네트워크 불안정이나 브라우저 강제 종료로 인한 WebSocket 연결 끊김은 치명적인 문제였습니다. 연결이 끊어지면 진행 중인 면접이 중단되고, 사용자는 처음부터 다시 시작해야 하는 상황이 발생할 수 있었습니다.

전통적인 heartbeat나 ping/pong 방식은 지속적인 메시지 교환으로 인한 오버헤드가 발생했고, 우리 서비스의 특성상 대부분의 이벤트가 broadcast되어 즉각적인 연결 끊김 감지가 어려웠습니다.

이를 해결하기 위해 **"마지막 이벤트 재발송"** 전략을 도입했습니다. 각 사용자별로 마지막 발생 이벤트를 메모리에 저장하고, 재연결 시 해당 이벤트를 재전송하여 끊어진 지점부터 자연스럽게 이어갈 수 있도록 구현했습니다.

JWT 기반 사용자 인증과 userId 매핑을 통해 재연결 시 이전 세션 상태를 정확히 복구할 수 있었고, 연결 상태 모니터링 오버헤드 없이도 빠른 재연결이 가능한 안정적인 면접 환경을 구축했습니다.

[Wiki로 자세히 보기](https://github.com/today-is-first/kkori/wiki/%F0%9F%94%84-WebSocket-%EC%9E%AC%EC%97%B0%EA%B2%B0-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84-%EB%B0%8F-%EA%B5%AC%ED%98%84)

<br/>

### 🛡️ **백**
### 📚 **복잡한 질문세트 버전 관리와 데이터 무결성 보장**
만약 1000명이 같은 "Spring 면접 질문 30개"를 각자 복사해서 조금씩만 수정한다면, 실제로는 거의 동일한 질문들이 1000번 중복 저장되는 문제가 발생했습니다.

"자바란 무엇인가?" 라는 질문이 데이터베이스에 1000번 똑같이 저장되는 상황이었습니다.

또, 질문 세트를 '가져가기' 하게 되면 원본 작성자가 질문을 수정했을 때, 그걸 복사한 모든 사람들의 질문 세트도 함께 바뀌지 않도록 설계해야 했습니다.

완전한 복사 방식으로 모든 질문을 새로 저장하는 방법은 저장 공간 낭비 문제를 해결할 수 없었고, 참조 방식으로 원본을 계속 바라보는 방법은 개인화 요구사항을 만족시킬 수 없었습니다.

질문과 기대되는 답변을 하나로 묶어 변하지 않도록 관리하고, 해당 질문 엔티티를 참조하는 개별 질문 세트 엔티티를 사용하는 방식을 선택했습니다. 독립적으로 관리가 필요했기 때문에 질문 세트의 버전과 질문 순서를 관리하는 질문 세트 맵을 통해서 공유되면서 독립적인 구조로 설계했습니다.

[Wiki로 자세히 보기](https://lab.ssafy.com/s13-webmobile1-sub1/S13P11A707/-/wikis/Home/%7B%EC%A7%84%EA%B7%9C,-%EC%88%98%ED%95%9C%EC%9D%98-%EA%B3%A0%EB%AF%BC%EA%B3%BC-%ED%95%B4%EA%B2%B0%7D))
### 🔄 **N+1 문제**
면접 연습 서비스를 개발하면서 질문 세트 목록 조회가 핵심 기능 중 하나였습니다.

하지만 사용자 시나리오 테스트를 진행하면서 **필요할 때마다 개별 조회**하는 반복적 조회 문제가 발생했습니다.

질문 세트 50,000개의 가짜 데이터를 넣고 질문 목록 조회 기능을 테스트했을 때, 평균 418ms만큼의 응답 시간이 나왔습니다. 더 많은 사용자가 사이트를 이용하게 되면 응답 시간이 더 증가할 우려가 있어 한 번의 조회로 연관되는 데이터들을 모두 가져오는 방식으로 문제를 해결했습니다.


[Wiki로 자세히 보기](https://github.com/today-is-first/kkori/wiki/%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0-%EA%B8%B0%EB%8A%A5-%EA%B3%A0%EB%8F%84%ED%99%94#-%EA%B3%A0%EB%AF%BC-2-%EC%A7%88%EB%AC%B8-%EB%AA%A9%EB%A1%9D-%EC%A1%B0%ED%9A%8C-n1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0)


<br />

### 🚀 프론트
사용자 진입 시점 UX 최적화를 위해 CWV를 개선할 필요가 있었습니다.

MVP 개발 후 최초로 LCP를 측정했을 때 2.5s가 나왔습니다.

구글에서는 LCP가 2.5초가 넘으면 개선이 필요하다고 권장하고 있어서, LCP 개선 필요성을 느꼈습니다.

**이미지 파일 개선, 코드 스플리트, SEO 개선 3가지를 시도**하였습니다.

최종적으로 **LCP 2.5초에서 0.6초로 개선**하였고, `Performance`, `SEO` 항목을 100점으로 만들 수 있었습니다.


[Wiki로 자세히 보기](https://github.com/today-is-first/kkori/wiki/Web-Core-Vital-%EA%B0%9C%EC%84%A0%ED%95%98%EA%B8%B0)

<br/>
<br/>

## 🎯 주요 기능 소개

### 🎤 실시간 면접 세션 관리
> **"혼자도, 둘이서도! 다양한 면접 연습 모드"**

**사용자는 SOLO 또는 PAIR 모드를 선택해 면접을 시작**할 수 있습니다.  
**WebSocket 기반 실시간 통신**으로 면접 상태가 실시간으로 동기화되며,  
**면접관/면접자 역할을 자유롭게 바꿔가며** 연습할 수 있어요.

<p align="center">
  <img src="https://raw.githubusercontent.com/SwnBae/kkori_img/main/%EB%91%98%EC%9D%B4.gif" width="70%" alt="면접 세션 관리" />
</p>

<br/>
<br/>

### 🤖 AI 기반 꼬리질문 생성
> **"답변에 따라 달라지는 똑똑한 질문!"**

**사용자의 답변을 실시간으로 분석**하여  
**GPT API를 통해 맞춤형 꼬리질문을 자동 생성**합니다.  
**STT(Speech-to-Text) 기술**로 음성 답변을 텍스트로 변환하고,  
이를 바탕으로 **개인화된 후속 질문**을 제공해요.

<p align="center">
  <img src="https://raw.githubusercontent.com/SwnBae/kkori_img/main/STT.gif" width="70%" alt="AI 꼬리질문" />
</p>

<br/>
<br/>

### 💬 실시간 채팅 시스템
> **"면접 중에도 소통은 계속되어야죠!"**

**WebSocket 기반 실시간 채팅**으로 면접 진행 중  
**참여자들이 즉시 소통**할 수 있습니다.  
**방별 채팅방 관리**와 **메시지 브로드캐스팅**으로  
원활한 커뮤니케이션을 지원해요.

<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/실시간채팅시스템.gif" width="70%" alt="실시간 채팅 시스템" />
</p>

<br/>

### 🔐 카카오 소셜 로그인
> "간편하고 안전한 로그인!"

**카카오 OAuth2 연동**으로 복잡한 회원가입 절차 없이 원클릭으로 빠른 로그인이 가능합니다.<br/>
JWT 토큰 기반 인증으로 보안성을 보장하며, 리프레시 토큰을 통해 안정적인 세션 관리를 제공해요.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/카카오로그인.gif" width="70%" alt="카카오 소셜 로그인" />
</p>

### 👤 게스트 로그인
> "회원가입 없이도 바로 체험!"

**익명 사용자**도 즉시 면접 연습을 시작할 수 있습니다.<br/>
임시 게스트 계정 생성으로 서비스 제한 없이 모든 기능을 자유롭게 이용할 수 있어요.<br/>
부담 없는 서비스 체험이 가능합니다.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/게스트로그인.gif" width="70%" alt="익명 게스트 로그인" />
</p>

### 📋 질문 세트 조회
> "모든 질문 세트를 깔끔하게!"

**태그별 필터링 시스템**으로 태그 기반 검색과 페이징 처리로 효율적인 질문 세트 탐색이 가능합니다.<br/>
**내 질문세트(/my)**와 공개 질문세트 분리 조회로 체계적인 관리가 가능해요.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/질문세트조회.gif" width="70%" alt="질문 세트 조회" />
</p>

### ➕ 질문 세트 생성
> "나만의 맞춤 질문 세트 만들기!"

직관적인 UI로 **질문과 예상 답변**을 한 번에 등록하고, 태그 시스템으로 체계적인 분류가 가능합니다.<br/>
**공개/비공개 설정**으로 다른 사용자와의 공유 여부를 자유롭게 결정할 수 있어요.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/질문세트추가.gif" width="70%" alt="질문 세트 생성" />
</p>

### 📥 질문 세트 가져오기
> "마음에 드는 질문을 내 것으로!"

다른 사용자가 공유한 질문 세트를 발견했다면 클릭 한 번으로 내 계정에 복사할 수 있습니다.<br/>
제목과 설명을 자유롭게 수정할 수 있어요.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/질문세트가져오기.gif" width="70%" alt="질문 세트 가져오기기" />
</p>

### ✏️ 질문 세트 수정
> "기존 질문의 답을 더 좋게 만들어보세요!"

**내 소유의 질문 세트를 수정**해 기존 답변을 더 좋게 개선할 수 있습니다.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/질문세트수정.gif" width="70%" alt="질문 세트 수정" />
</p>

### 🗑️ 질문 세트 삭제
> "실수도 걱정없는 소프트 삭제!"

**내가 만든 질문 세트만 삭제**할 수 있어요.
더 이상 필요하지 않은 질문 세트는 삭제해 내 질문 세트를 관리할 수 있습니다.
<p align="center">
  <img src="https://raw.githubusercontent.com/llcodingll/kkori_img/main/질문세트삭제.gif" width="70%" alt="질문 세트 삭제" />
</p>

<br/>

## ✅ 서비스 구조도

<img src="https://raw.githubusercontent.com/SwnBae/kkori_img/main/Architecture.png" width="700" alt="Kkori 서비스 아키텍처" />

<br/>
<br/>

## ⚒️ Tech Stacks

| 분류 | 기술 스택                                                                                                |
|------|------------------------------------------------------------------------------------------------------|
| **Frontend** | [![My Skills](https://skillicons.dev/icons?i=react,vite,tailwind,ts,nodejs)](https://skillicons.dev) |
| **Backend** | [![My Skills](https://skillicons.dev/icons?i=java,spring,hibernate)](https://skillicons.dev)          |
| **Database / Infra** | [![My Skills](https://skillicons.dev/icons?i=mysql,nginx,aws)](https://skillicons.dev)               |
| **배포** | [![My Skills](https://skillicons.dev/icons?i=docker,jenkins)](https://skillicons.dev)                 |
| **협업 / 개발도구** | [![My Skills](https://skillicons.dev/icons?i=git,gitlab,notion,jira)](https://skillicons.dev)        |

<br/>
<br/>

## 🤼 팀원 소개

| 이찬 | 김형진 | 강진규 | 배수한 | 유윤지 | 장동현 |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="https://github.com/user-attachments/assets/3bc958ec-4303-4559-b20e-465fe1776e17" width="120"> | <img src="https://avatars.githubusercontent.com/u/49364688?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/64190888?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/128581113?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/105447233?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/205485545?v=4" width="120"> |
| **Frontend** | **Frontend** | **Backend** | **Backend** | **Backend** | **Backend** |
| [@today-is-first](https://github.com/today-is-first) | [@hyeongjin-kim](https://github.com/hyeongjin-kim) | [@jin0410](https://github.com/jin0410) | [@SwnBae](https://github.com/SwnBae) | [@llcodingll](https://github.com/llcodingll) | [@SuitGGam](https://github.com/SuitGGam) |

