let checkCount = 0;

console.log = function () {};

const observerTimer = setInterval(() => {
  const resultElement = document.querySelector(".result span");
  if (!resultElement) return;

  const resultText = resultElement.innerText.trim();
  checkCount++;

  if (checkCount > 120) {
    clearInterval(observerTimer);
    console.log("⏱️ 채점이 너무 오래 걸려 감시를 종료합니다.");
    return;
  }

  if (resultText.length === 0) return;

  const processingMessages = ["기다리는 중", "채점 준비 중", "채점 중", "%"];
  const isProcessing = processingMessages.some((msg) =>
    resultText.includes(msg),
  );
  if (isProcessing) {
    console.log(`⏳ 채점 진행 중... 현재 상태: ${resultText}`);
    return;
  }

  console.log(`✅ 최종 채점 결과: ${resultText}`);
  clearInterval(observerTimer);

  const tr = resultElement.closest("tr");
  const userName = tr.querySelector("td:nth-child(2)").innerText.trim();
  const problemId = parseInt(
    tr.querySelector("td:nth-child(3)").innerText.trim(),
    10,
  );
  const timestamp = Date.now();

  const reviewItemDto = {
    userName: userName,
    problemId: problemId,
    resultText: resultText,
    timestamp: timestamp,
  };

  sendToBackground(reviewItemDto);
}, 1000);

function sendToBackground(data) {
  chrome.runtime.sendMessage({ action: "sendData", data: data }, (response) => {
    if (response && response.success) {
      console.log("🚀 본부(백그라운드)로 제출 데이터 발송 성공!");
    } else {
      console.error(
        "❌ 본부 발송 실패:",
        response ? response.error : "응답 없음",
      );
    }
  });
}
