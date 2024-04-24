// API 서버 정보 기입
const apiServer = "localhost:11000";
const loginPage = "/templates/security/login.html";
const jwtTokenName = "jwtAccessToken";

function loginCheck() {
    let token = $.cookie(jwtTokenName);

    if (token === "undefined" || token == null) {
        alert("로그인 되지 않았습니다. 로그인 하시길 바랍니다.");
        location.href = loginPage;
    }
}

