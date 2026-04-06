console.log = function () {};

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "sendData") {
    console.log(
      "📡 스파이 수신 완료! 서버로 제출 데이터를 전송합니다:",
      request.data,
    );

    chrome.storage.local.set({ userName: request.data.userName });

    fetch("https://code-reminder.duckdns.org/api/review-item", {
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

    return true;
  }
});

chrome.runtime.onInstalled.addListener(() => {
  chrome.alarms.create("checkReviewAlarm", { periodInMinutes: 60 });

  checkAndNotify();

});

chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === "checkReviewAlarm") {
    checkAndNotify();
  }
});

chrome.runtime.onStartup.addListener(() => {
  checkAndNotify();
});

async function checkAndNotify() {
  const storageData = await chrome.storage.local.get([
    "userName",
    "lastNotifyDate",
  ]);
  if (!storageData.userName) return;

  const todayDate = new Date().toLocaleDateString(); // 예: "2026. 3. 23."
  if (storageData.lastNotifyDate === todayDate) {
    console.log("이미 오늘 알람을 보냈습니다. 통과!");
    return;
  }

  try {
    const response = await fetch(
      `https://code-reminder.duckdns.org/api/review-item/${storageData.userName}/today-count`,
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

      chrome.storage.local.set({ lastNotifyDate: todayDate });
    }
  } catch (error) {
    console.error("복습 알림 체크 중 서버 접속 실패:", error);
  }
}

chrome.notifications.onButtonClicked.addListener(
  (notificationId, buttonIndex) => {
    if (buttonIndex === 0) {
      chrome.storage.local.get("userName", (data) => {
        const reviewUrl = `https://code-reminder.duckdns.org/reviews/${data.userName}`;
        chrome.tabs.create({ url: reviewUrl });
      });
    }
    chrome.notifications.clear(notificationId);
  },
);
