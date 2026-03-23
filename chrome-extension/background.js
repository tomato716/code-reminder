console.log = function () {};
// ==========================================
// 1. [데이터 저장] 스파이의 무전을 받아 백엔드로 POST 전송
// ==========================================
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "sendData") {
    console.log(
      "📡 스파이 수신 완료! 서버로 제출 데이터를 전송합니다:",
      request.data,
    );

    // 알림 기능을 위해 제출한 유저의 아이디를 브라우저에 저장해둡니다.
    chrome.storage.local.set({ userName: request.data.userName });

    // 백엔드의 POST 저장 API 호출
    fetch("http://localhost:8080/api/review-item", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request.data),
    })
      .then((response) => {
        if (!response.ok) throw new Error("서버 응답 오류: " + response.status);
        return response.text();
      })
      .then((data) => {
        console.log("✅ 백엔드 DB 저장 완료:", data);
        sendResponse({ success: true });
      })
      .catch((error) => {
        console.error("❌ 서버 저장 중 에러 발생:", error);
        sendResponse({ success: false, error: error.message });
      });

    return true; // 비동기 응답을 위해 필수!
  }
});

// 2. [알림 시스템] 브라우저 접속 시 "딱 한 번" 실행
// 크롬 브라우저를 완전히 껐다가 새로 켰을 때 실행됩니다.
chrome.runtime.onStartup.addListener(() => {
  checkAndNotify();
});

// 백엔드에 개수 물어보고 알림 띄우는 함수
async function checkAndNotify() {
  const storageData = await chrome.storage.local.get([
    "userName",
    "lastNotifyDate",
  ]);
  if (!storageData.userName) return;

  // 1. 오늘 이미 알람을 보냈는지 날짜 체크
  const todayDate = new Date().toLocaleDateString(); // 예: "2026. 3. 23."
  if (storageData.lastNotifyDate === todayDate) {
    console.log("이미 오늘 알람을 보냈습니다. 통과!");
    return;
  }

  try {
    const response = await fetch(
      `http://localhost:8080/api/review-item/${storageData.userName}/today-count`,
    );
    const result = await response.json();
    const count = result["today-count"];

    if (count > 0) {
      chrome.notifications.create({
        type: "basic",
        iconUrl: "icon.png",
        title: "Code Reminder ⏰",
        message: `오늘 복습해야 할 백준 문제가 ${count}개 남았습니다!\n서둘러 복습을 완료해주세요.`,
        priority: 2,
        buttons: [{ title: "📖 복습하러 가기" }, { title: "나중에 하기" }],
        requireInteraction: true,
      });

      // 2. 알람을 보낸 후, 오늘 날짜를 storage에 저장
      chrome.storage.local.set({ lastNotifyDate: todayDate });
    }
  } catch (error) {
    console.error("복습 알림 체크 중 서버 접속 실패:", error);
  }
}

// 버튼을 눌렀을 때 실행되는 로직
chrome.notifications.onButtonClicked.addListener(
  (notificationId, buttonIndex) => {
    if (buttonIndex === 0) {
      // 첫 번째 버튼(복습하러 가기)을 눌렀을 때
      chrome.storage.local.get("userName", (data) => {
        const reviewUrl = `http://localhost:8080/reviews/${data.userName}`;
        chrome.tabs.create({ url: reviewUrl });
      });
    }
    chrome.notifications.clear(notificationId);
  },
);
