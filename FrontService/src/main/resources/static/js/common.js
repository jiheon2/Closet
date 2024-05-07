// API 서버 정보 기입
const apiServer = "localhost:11000";
const loginPage = "/security/login.html"
let loginUserId = "";

// 로그인 정보가 있는지 확인
function loginInfo() {

    // 컨트롤러에 토큰값에서 userId 정보 가져오기 요청
    $.ajax({
        url: "http://" + apiServer + "/user/v1/loginCheck",
        type: "post",
        dataType: "JSON",
        xhrFields: {
            withCredentials: true
        }
    }).then(function (json) {
        loginUserId = json.data.userId;
        console.log(loginUserId);
    })
}


// MyPage 링크 클릭 시 처리
function goToMyPage() {
    location.href = "/user/MyPage.html";
}

// Closet 링크 클릭 시 처리
function goToCloset() {
    location.href = "/closet/closet.html";
}

// Chatbot 링크 클릭 시 처리
function goToChatbot() {
    location.href = "/style/styleAI.html";
}

// Community 링크 클릭 시 처리
function goToCommunity() {
    location.href = "/community/community.html";
}

// Style Dictionary 링크 클릭 시 처리
function goToStyleDictionary() {
    location.href = "/style/styleDictionary.html";
}

// Index 링크 클릭 시 처리
function goToIndex() {
    location.href = "/security/afterLogin.html"
}