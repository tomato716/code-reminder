let checkCount = 0;

console.log = function () {};

const observerTimer = setInterval(() => {
  // 현재 화면에서 결과 텍스트 가져오기 (채점 현황 테이블 기준)
  const resultElement = document.querySelector(".result span");
  if (!resultElement) return;

  const resultText = resultElement.innerText.trim();
  checkCount++;

  // 1. 시간 초과 (약 2분) 시 포기
  if (checkCount > 120) {
    clearInterval(observerTimer);
    console.log("⏱️ 채점이 너무 오래 걸려 감시를 종료합니다.");
    return;
  }

  // 2. 텍스트가 없으면 대기
  if (resultText.length === 0) return;

  // 3. 채점 진행 중이면 대기
  const processingMessages = ["기다리는 중", "채점 준비 중", "채점 중", "%"];
  const isProcessing = processingMessages.some((msg) =>
    resultText.includes(msg),
  );
  if (isProcessing) {
    console.log(`⏳ 채점 진행 중... 현재 상태: ${resultText}`);
    return;
  }

  // 4. 최종 결과 감지 완료!
  console.log(`✅ 최종 채점 결과: ${resultText}`);
  clearInterval(observerTimer);

  // 5. 백엔드 DTO 형식에 맞춰 데이터 수집
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

  // 6. 무조건 서버로 전송 (정답/오답 처리는 이제 백엔드가 알아서 함!)
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
