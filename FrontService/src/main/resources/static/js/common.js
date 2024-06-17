// API 서버 정보 기입
const apiServer = "localhost:11000";
const loginPage = "/security/login.html"
let loginUserId = "";

// 로그인 정보가 있는지 확인
function loginInfo() {
    return new Promise(function(resolve, reject) {
        // 컨트롤러에 토큰값에서 userId 정보 가져오기 요청
        $.ajax({
            url: "http://" + apiServer + "/user/v1/userInfo",
            type: "post",
            dataType: "JSON",
            xhrFields: {
                withCredentials: true
            }
        }).then(function (json) {
            console.log(json.data.userId);
            console.log(json.data.nickName);
            resolve(json.data.nickName);
            loginUserId = json.data.userId;
        }).fail(function (xhr, status, error) {
            alert("로그인 해주시길 바랍니다..");
            console.log(error);
            location.href = loginPage;
        });
    });
}

// MyPage 링크 클릭 시 처리
function goToMyPage() {
    location.href = "/user/MyPage.html";
}

// Closet 링크 클릭 시 처리
function goToCloset() {
    location.href = "/closet.html";
}

// Chatbot 링크 클릭 시 처리
function goToChatbot() {
    location.href = "/style/styleAI.html";
}

// Community 링크 클릭 시 처리
function goToCommunity() {
    location.href = "/community.html";
}

function goToStyle() {
    location.href = "/style/style.html";
}

// Style Dictionary 링크 클릭 시 처리
function goToStyleDictionary() {
    location.href = "/style/styleDictionary.html";
}

// Index 링크 클릭 시 처리
function goToIndex() {
    location.href = "/security/afterLogin.html"
}