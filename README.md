```markdown
# WebCraft 실시간 게임 서버

Spring Boot, JPA, MySQL, Redis, WebSocket을 이용해  
플레이어 등록, 월드 관리, 채팅 저장 및 실시간 전송, 접속 상태 관리, 접속자 조회 기능을 구현한 프로젝트입니다.

## 기술 스택

- Java 21
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- WebSocket
- Docker
- Lombok
- Bean Validation

## API 명세

### REST API

| Method | URL | 설명 |
| ------ | --- | --- |
| POST | `/players` | 플레이어 등록 |
| GET | `/worlds` | 월드 목록 조회 |
| POST | `/worlds` | 월드 생성 |
| GET | `/worlds/{worldId}/chats` | 최근 채팅 조회 |

### WebSocket

연결 URL

```text
ws://localhost:8080/ws/worlds/{worldId}?nickname={nickname}
```

주요 WebSocket 메시지

| type | 설명 |
| --- | --- |
| `ping` | 연결 상태 확인 및 Presence 갱신 |
| `move` | 플레이어 이동 정보 전달 |
| `chat` | 채팅 저장 및 같은 월드에 실시간 전송 |
| `onlineUsers` | 현재 월드 접속자 목록 조회 |

## 주요 요청 / 응답

### 플레이어 등록

`POST /players`

요청 예시:

```json
{
  "nickname": "Test1234"
}
```

닉네임은 2~12자의 영문, 숫자, `_`만 사용할 수 있으며 중복 닉네임은 등록할 수 없습니다.

---

### 최근 채팅 조회

`GET /worlds/{worldId}/chats`

응답 예시:

```json
[
  {
    "sender": "Test1234",
    "content": "Hello!",
    "createdAt": "2026-09-22T14:29:58"
  }
]
```

최근 채팅을 조회한 뒤 실제 대화 순서에 맞도록 오래된 메시지부터 반환합니다.

---

### WebSocket 채팅

요청:

```json
{
  "type": "chat",
  "content": "Hello!"
}
```

응답:

```json
{
  "type": "chat",
  "sender": "Test1234",
  "content": "Hello!",
  "timestamp": "2026-09-22T14:29:58"
}
```

채팅은 MySQL에 저장한 뒤 같은 월드에 접속한 사용자에게 WebSocket으로 전달합니다.

---

### 접속자 목록 조회

요청:

```json
{
  "type": "onlineUsers"
}
```

응답:

```json
{
  "type": "onlineUsers",
  "users": [
    "Test1234",
    "Test12345"
  ],
  "count": 2
}
```

현재 서버의 같은 월드에 연결된 WebSocket 세션 중 실제로 열린 연결만 조회합니다.

닉네임은 원래 대소문자를 유지하고 Java 문자열 자연 순서로 정렬합니다.

## 주요 구현 내용

- Docker를 이용한 MySQL / Redis 실행 환경 구성
- JPA `@Index`를 이용한 채팅 테이블 복합 인덱스 생성
- Bean Validation을 이용한 플레이어 닉네임 검증
- 중복 닉네임 등록 방지
- 월드 생성 개수 제한
- Spring Data JPA 기반 채팅 저장 및 최근 채팅 조회
- DTO를 이용한 Entity와 API 응답 분리
- WebSocket Handshake 단계에서 플레이어 및 월드 검증
- WebSocket Session Attribute를 이용한 사용자 정보 관리
- 같은 월드 내 동일 닉네임 중복 접속 방지
- Redis Sorted Set을 이용한 접속 상태(Presence) 관리
- Ping / Pong 기반 Heartbeat 처리
- WebSocket MessageRouter를 이용한 메시지 타입별 Handler 분리
- 플레이어 이동 메시지를 게임 엔진 Queue에 전달
- 채팅 저장 후 같은 월드 사용자에게 Broadcast
- 현재 월드의 열린 WebSocket 연결을 이용한 접속자 목록 조회

## 서버 처리 구조

### REST API

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL
```

### WebSocket

```text
Client
  ↓
WebSocket Handshake
  ↓
MessageRouter
  ↓
WsMessageHandler
  ↓
Service / Game Engine
  ↓
MySQL / Redis
  ↓
sendTo / broadcast
  ↓
Client
```

## MySQL / Redis 사용

### MySQL

다음과 같이 영속적으로 저장해야 하는 데이터를 관리합니다.

- 플레이어
- 월드
- 채팅 메시지

### Redis

실시간 접속 상태를 관리하는 데 사용합니다.

```text
world:{worldId}:presence
```

Redis Sorted Set에 WebSocket 연결 ID와 만료 시각을 저장하고  
Heartbeat가 들어올 때마다 접속 상태를 갱신합니다.

## 프로젝트에서 학습한 내용

- REST API와 WebSocket의 요청 처리 방식 차이
- Controller / Service / Repository 역할 분리
- Entity와 DTO의 역할 구분
- JPA를 이용한 데이터 저장 및 조회
- 복합 인덱스를 이용한 조회 성능 개선
- WebSocket Session과 HandshakeInterceptor 활용
- 실시간 메시지 Routing과 Broadcasting
- Redis를 이용한 실시간 접속 상태 관리
- WebSocket 연결과 Redis Presence의 역할 차이
```
