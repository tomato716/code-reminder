const SELECTOR = {
  RESULT_SPAN: ".result span",
  ROW: "tr",
  USER_NAME_TD: "td:nth-child(2)",
  PROBLEM_ID_TD: "td:nth-child(3)",
  SUBMIT_TIME: "td:nth-child(9) a"
};


const TIMER = {
  POLLING_INTERVAL_MS: 1000,
  MAX_RETRY_COUNT: 120
};

const PROCESSING_MESSAGES = ["기다리는 중", "채점 준비 중", "채점 중", "%"];

let checkCount = 0;
let observerTimer = null;

function startObservation() {
  observerTimer = setInterval(checkSubmitStatus, TIMER.POLLING_INTERVAL_MS);
}


function checkSubmitStatus() {
  const resultElement = document.querySelector(SELECTOR.RESULT_SPAN);
  if (!resultElement) return;

  const resultText = resultElement.innerText.trim();
  if (resultText.length === 0) return;

  checkCount++;

  if (checkCount > TIMER.MAX_RETRY_COUNT) {
    stopObservation();
    return;
  }

  if (isStillProcessing(resultText)) return;

  //오늘 제출한 문제인지 검증
  const row = resultElement.closest(SELECTOR.ROW);
  const submitTimestampMs = getSubmitTimestamp(row);
  if (!isSubmittedToday(submitTimestampMs)) {
    console.log("⏳ 오늘 제출한 문제가 아닙니다. 전송을 차단합니다.");
    stopObservation();
    return;
  }
  
  stopObservation();
  const reviewData = extractReviewData(resultElement, resultText, submitTimestampMs);
  sendToBackground(reviewData);
}


//채점 중인지 여부 판별
function isStillProcessing(text) {
  return PROCESSING_MESSAGES.some((msg) => text.includes(msg));
}

function getSubmitTimestamp(row) {
  const timeAnchor = row.querySelector(SELECTOR.SUBMIT_TIME);
  if (!timeAnchor) return null;

  const timestampStr = timeAnchor.getAttribute("data-timestamp");
  if (!timestampStr) return null;

  return parseInt(timestampStr, 10); // 밀리초로 변환해서 반환
}

//제출 날짜가 오늘인지 검증 로직
function isSubmittedToday(submitTimestampMs) {

  const submitDate = new Date(submitTimestampMs * 1000);
  const today = new Date();

  return (
      submitDate.getFullYear() === today.getFullYear() &&
      submitDate.getMonth() === today.getMonth() &&
      submitDate.getDate() === today.getDate()
  );
}

function extractReviewData(resultElement, resultText, submitTimestampMs) {
  const row = resultElement.closest(SELECTOR.ROW);
  return {
    userName: row.querySelector(SELECTOR.USER_NAME_TD).innerText.trim(),
    problemId: parseInt(row.querySelector(SELECTOR.PROBLEM_ID_TD).innerText.trim(), 10),
    resultText: resultText,
    timestamp: submitTimestampMs
  };
}

function stopObservation() {
  if (observerTimer) clearInterval(observerTimer);
}

function sendToBackground(data) {
  chrome.runtime.sendMessage({ action: "sendData", data: data });
}

startObservation();