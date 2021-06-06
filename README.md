## 2021-1 Software Capstone Design Project
## Team<span style="color:red"> Burning 🔥</span>
## 1. 프로젝트 개요
> **SEAT POOL은 KTX 4인 동반석 모집 및 여행 일행 찾기 앱이다.** 기차 여행은 가고 싶지만 같이 갈 사람이 없는 고객, KTX 를 이용하면서 새로운 인연을 찾고자 하는 고객, KTX의 높은 이용 가격에 부담을 느껴 다른 교통수단을 이용하려는 고객 등이 주요 사용자이다. 4인 동반석 할인제도는 예매 날짜에 따라 15% ~ 35% 까지 할인 혜택을 받을 수 있다. 하지만 이 제도를 이용하기 위해서는 반드시 4명의 KTX 이용객이 필요하다는 단점이 있다. 사용자는 이 서비스를 통해 기차표를 확인하여 그룹을 생성하거나 기존에 있는 그룹에 참여할 수 있고 혹은 마음 맞는 친구를 찾기 위해 게시판 기능을 활용하여 일행을 모집할 수 있다.

## 2. 개발 환경
**`Node.js`** : v14.16.0      
**`MySQL`** : 8.0.24      
**`npm`** : 6.14.11      
- **Google Firebase Database**
* **사용한 외부 API 목록**
    * 오픈뱅킹API
    * I'mport API 

##### 개발 환경 설정하기
    > git clone REPOSITORY ADDRESS
    > npm install
##### 실행하기
    > node server.js

## 3. 주요기능
- KTX 4인석 시간표 조회
    - Selenium을 이용하여 코레일 홈페이지로부터 4인석 정보를 조회해서 받아옴
    - 제공 정보 : 출발역, 도착역, 출발시간, 도착시간, 할인율, 요금
    - 이용 날짜 기준 3일 후 부터 조회가능

- 그룹 채팅
    - Firebase realtime database 기반 그룹 채팅
    - 한 채팅방에 최대 4명까지 참여가능
    - 그룹 내 참여자들의 결제여부 확인 가능
    - 결제를 했어도 마감일 전에 그룹에서 나올 시 자동으로 결제 취소
    - 출발일 2일 전 그룹 인원 수 및 결제여부를 확인해서 미달시 자동으로 방 삭제

- 이용객 모집 게시판
    - 출발지와 목적지를 선택해서 게시글 작성 가능
    - 출발지 기반 게시글 검색 가능
    - 댓글 신고가 누적 되었을 시 자동으로 블라인드 처리    

- 결제하기
    - I'mport 이니시스(inicis) 결제 사용
    - 그룹 채팅 내 사이드바를 통해 접근 가능





**Github Address**
ADDRESS HERE <--------------->

