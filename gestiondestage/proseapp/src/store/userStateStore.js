import {create} from 'zustand';

const userStateStore = create((set) => ({
    userType: '',
    username: '',
    accessToken: '',
    saveToken: (token) => set({accessToken: token}),
    saveUserInfo: (usertype, usename) => set({userType: usertype, username: usename}),
    logout: () => set({userType: '', username: '', accessToken: ''}),
}))

export default userStateStore





