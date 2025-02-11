import * as userInfo from "../../../utils/userInfo";
import {fireEvent, render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import {SessionContext} from "../../CurrentSession";
import Dashboard from "../dashboard";
import DashboardTopbar from "./topbar";
import "../../../utils/i18n/index.mock.js";


beforeEach(() => {
    userInfo.setUserInfo("student", "token", "userrrrname")
});

afterEach(() => {
    jest.restoreAllMocks();
    userInfo.clearUserInfo();
});

test("Topbar render", async () => {
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardTopbar></DashboardTopbar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
});

test("Topbar ham", async () => {
    const mockfn = jest.fn()

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardTopbar handleHam={mockfn}></DashboardTopbar>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const ham = screen.getByTestId("topbar-ham")

    fireEvent.click(ham)

    expect(ham).toBeInTheDocument();
    expect(ham).toHaveClass("lg:hidden")
    expect(ham).toHaveClass("inline-block")
    expect(mockfn).toBeCalled()
});

test("Topbar logout", async () => {
    const mockfn = jest.fn()

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardTopbar handleLogout={mockfn}></DashboardTopbar>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const logout = screen.getByTestId("topbar-logout")

    fireEvent.click(logout)

    expect(logout).toBeInTheDocument();
    expect(mockfn).toBeCalled()
});

test("Topbar username", async () => {
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardTopbar></DashboardTopbar>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const username = screen.getByTestId("topbar-username")

    expect(username).toBeInTheDocument();
    expect(username.innerHTML).toBe("Welcome, <b>userrrrname</b>")
});

test("Topbar date", async () => {
    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate();
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardTopbar></DashboardTopbar>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const dateElement = screen.getByText(date)

    expect(dateElement).toBeInTheDocument();
});