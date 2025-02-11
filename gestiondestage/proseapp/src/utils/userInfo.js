export const getUserInfo =  () => {


    // get userType from sessionStorage
    const userType = sessionStorage.getItem('userType');
    // get username from sessionStorage
    const username = sessionStorage.getItem('username');
    // get token from sessionStorage
    const token = sessionStorage.getItem('token');
    // get lang from sessionStorage
    const lang = localStorage.getItem('lang');

    const useSession = sessionStorage.getItem('useSession');

    return {
        userType,
        token,
        username,
        lang
    }

    // // get user type from localStorage
    // const userType = localStorage.getItem('userType');
    //
    // // get user token from localStorage
    // const token = localStorage.getItem('token');
    //
    // // get username from localStorage
    // const username = localStorage.getItem('username');
    //
    // const lang = localStorage.getItem('lang');

    // return {
    //     userType,
    //     token,
    //     username,
    //     lang
    // }

}


export const clearUserInfo = () => {
    sessionStorage.removeItem("userType");
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("username");
    sessionStorage.removeItem("currentSession");

}


export const setUserInfo = (userType, token, username) => {
    sessionStorage.setItem("userType", userType);
    sessionStorage.setItem("token", token);
    sessionStorage.setItem("username", username);
}

