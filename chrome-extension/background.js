const CONSTANT = {
    API_BASE_URL: "https://code-reminder.duckdns.org",
    ALARM_NAME: "checkReviewAlarm",
    ALARM_PERIOD_MINUTES: 60,
    ICON: "alarm_icon.png"
};

const STORAGE_KEYS = {
    USER_NAME: "userName",
    LAST_NOTIFY_DATE: "lastNotifyDate"
};

console.log = function () {};

//로컬 스토리지 관리 객체
const StorageService = {
    async getUserName() {
        const data = await chrome.storage.local.get(STORAGE_KEYS.USER_NAME);
        return data[STORAGE_KEYS.USER_NAME];
    },
    async saveUserName(userName) {
        await chrome.storage.local.set({[STORAGE_KEYS.USER_NAME]: userName});
    },
    async getLastNotifyDate() {
        const data = await chrome.storage.local.get(STORAGE_KEYS.LAST_NOTIFY_DATE);
        return data[STORAGE_KEYS.LAST_NOTIFY_DATE];
    },
    async saveLastNotifyDate(date) {
        await chrome.storage.local.set({[STORAGE_KEYS.LAST_NOTIFY_DATE]: date});
    }
};

//외부 API 통신 객체
const ApiService = {
    async sendReviewData(data) {
        const response = await fetch(`${CONSTANT.API_BASE_URL}/api/review-item`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(data),
        });
        if (!response.ok) throw new Error(`서버 응답 오류: ${response.status}`);
    },
    async fetchTodayReviewCount(userName) {
        const response = await fetch(`${CONSTANT.API_BASE_URL}/api/review-item/${userName}/today-count`);
        if (!response.ok) throw new Error("개수 조회 실패");
        const result = await response.json();
        return result["today-count"];
    }
};

//알림(UI) 처리 객체
const NotificationService = {
    show(count) {
        chrome.notifications.create({
            type: "basic",
            iconUrl: CONSTANT.ICON,
            title: "Code Reminder ⏰",
            message: `오늘 복습해야 할 백준 문제가\n${count}개 남았습니다!\n서둘러 복습을 완료해주세요.`,
            priority: 2,
            buttons: [{title: "📖 복습하러 가기"}, {title: "나중에 하기"}],
            requireInteraction: true,
        });
    },
    async openReviewPage() {
        const userName = await StorageService.getUserName();
        if (userName) {
            chrome.tabs.create({url: `${CONSTANT.API_BASE_URL}/reviews/${userName}`});
        }
    }
};

const ReviewService = {
    //content.js에서 넘어온 데이터 처리
    async handleNewReview(data, sendResponse) {
        try {
            await StorageService.saveUserName(data.userName);
            await ApiService.sendReviewData(data);
            sendResponse({success: true});
        } catch (error) {
            console.error("❌ 서버 저장 중 에러 발생:", error);
            sendResponse({success: false, error: error.message});
        }
    },

    // 복습 알림 스케줄러 로직
    async checkAndNotify() {
        const userName = await StorageService.getUserName();
        if (!userName) return;

        const todayDate = new Date().toLocaleDateString();
        const lastNotifyDate = await StorageService.getLastNotifyDate();
        if (lastNotifyDate === todayDate) return;

        try {
            const count = await ApiService.fetchTodayReviewCount(userName);
            if (count > 0) {
                NotificationService.show(count);
                await StorageService.saveLastNotifyDate(todayDate);
            }
        } catch (error) {
            console.error("복습 알림 체크 중 실패:", error);
        }
    }
};

//메시지 수신 (content.js -> background.js)
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "sendData") {
        ReviewService.handleNewReview(request.data, sendResponse);
        return true; // 비동기 응답을 위해 true 반환
    }
});

//익스텐션 설치 및 스케줄러 등록
chrome.runtime.onInstalled.addListener(() => {
    chrome.alarms.create(CONSTANT.ALARM_NAME, {periodInMinutes: CONSTANT.ALARM_PERIOD_MINUTES});
    ReviewService.checkAndNotify();
});

//브라우저 시작 시 검사
chrome.runtime.onStartup.addListener(() => {
    ReviewService.checkAndNotify();
});

//알람(스케줄러) 동작 시 검사
chrome.alarms.onAlarm.addListener((alarm) => {
    if (alarm.name === CONSTANT.ALARM_NAME) ReviewService.checkAndNotify();
});

//알림 버튼 클릭 이벤트
chrome.notifications.onButtonClicked.addListener((notificationId, buttonIndex) => {
    if (buttonIndex === 0) {
        NotificationService.openReviewPage();
    }
    chrome.notifications.clear(notificationId);
});