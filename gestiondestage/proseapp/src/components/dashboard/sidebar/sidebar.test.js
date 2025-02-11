import * as userInfo from "../../../utils/userInfo";
import {fireEvent, queryAllByTestId, render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import {SessionContext} from "../../CurrentSession";
import Dashboard from "../dashboard";
import DashboardSidebar from "./sidebar";
import "../../../utils/i18n/index.mock.js";
import {useTranslation} from "react-i18next";

// userInfo.setUserInfo("student", "token", "username")


afterEach(() => {
    jest.restoreAllMocks();
    userInfo.clearUserInfo()
});

test("Sidebar render", async () => {
    userInfo.setUserInfo("student", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const sidebar = screen.getByTestId("dashboard-sidebar")
    expect(sidebar).toBeInTheDocument()
});



test("Sidebar always present", async () => {

    userInfo.setUserInfo("", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const elements = screen.queryAllByTestId("navlink")
    const home = screen.queryByText("dashboardMenu.home")
    const about = screen.queryByText("dashboardMenu.about")

    expect(elements.length).toBe(2)
    expect(home).toBeInTheDocument()
    expect(about).toBeInTheDocument()
});

test("Sidebar student present", async () => {
    userInfo.setUserInfo("student", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const elements = screen.queryAllByTestId("navlink")
    const home = screen.queryByText("dashboardMenu.home")
    const about = screen.queryByText("dashboardMenu.about")

    const internship = screen.queryByText("dashboardMenu.internships")
    const cvs = screen.queryByText("dashboardMenu.studentUploadCV")
    const interview = screen.queryByText("dashboardMenu.myInterviews")
    const pending = screen.queryByText("dashboardMenu.pendingInternships")
    const contract = screen.queryByText("dashboardMenu.contracts")

    expect(elements.length).toBe(7)
    expect(home).toBeInTheDocument()
    expect(about).toBeInTheDocument()

    expect(interview).toBeInTheDocument()
    expect(internship).toBeInTheDocument()
    expect(cvs).toBeInTheDocument()
    expect(pending).toBeInTheDocument()
    expect(contract).toBeInTheDocument()

});

test("Sidebar employeur present", async () => {
    userInfo.setUserInfo("employeur", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const elements = screen.queryAllByTestId("navlink")
    const home = screen.queryByText("dashboardMenu.home")
    const about = screen.queryByText("dashboardMenu.about")

    const upload = screen.queryByText("dashboardMenu.employerUploadOffer")
    const candidates = screen.queryByText("dashboardMenu.employerCandidatesList")
    const interview = screen.queryByText("dashboardMenu.consultInterview")
    const contracts = screen.queryByText("dashboardMenu.contracts")
    const evaluate = screen.queryByText("dashboardMenu.evaluateStudent")

    expect(elements.length).toBe(7)
    expect(home).toBeInTheDocument()
    expect(about).toBeInTheDocument()

    expect(upload).toBeInTheDocument()
    expect(candidates).toBeInTheDocument()
    expect(interview).toBeInTheDocument()
    expect(contracts).toBeInTheDocument()
    expect(evaluate).toBeInTheDocument()
});

test("Sidebar GS present", async () => {
    userInfo.setUserInfo("projet_manager", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const elements = screen.queryAllByTestId("navlink")

    const home = screen.queryByText("dashboardMenu.home")
    const about = screen.queryByText("dashboardMenu.about")

    const offers = screen.queryByText("validateOffers.dashboardTitle")
    const cvs = screen.queryByText("validationCV.validateCVs")
    const contracts = screen.queryByText("dashboardMenu.contracts")
    const teachers = screen.queryByText("dashboardMenu.assginToProf")

    expect(elements.length).toBe(6)
    expect(home).toBeInTheDocument()
    expect(about).toBeInTheDocument()

    expect(offers).toBeInTheDocument()
    expect(cvs).toBeInTheDocument()
    expect(contracts).toBeInTheDocument()
    expect(teachers).toBeInTheDocument()
});

test("Sidebar teacher present", async () => {
    userInfo.setUserInfo("teacher", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const elements = screen.queryAllByTestId("navlink")
    const home = screen.queryByText("dashboardMenu.home")
    const about = screen.queryByText("dashboardMenu.about")

    const students = screen.queryByText("studentList.myStudents")

    expect(elements.length).toBe(3)
    expect(home).toBeInTheDocument()
    expect(about).toBeInTheDocument()

    expect(students).toBeInTheDocument()
});

test("Sidebar ham style noham", async () => {
    userInfo.setUserInfo("teacher", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar hamburger={false}></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const sidebar = screen.getByTestId("dashboard-sidebar")
    expect(sidebar).toHaveClass("hidden")

});

test("Sidebar ham style ham", async () => {
    userInfo.setUserInfo("teacher", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar hamburger={true}></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const sidebar = screen.getByTestId("dashboard-sidebar")
    expect(sidebar).toHaveClass("h-screen")
    expect(sidebar).toHaveClass("w-screen")

});

test("Sidebar ham style ignore big", async () => {
    userInfo.setUserInfo("teacher", "token", "username")
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar hamburger={false}></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const sidebar = screen.getByTestId("dashboard-sidebar")
    expect(sidebar).toHaveClass("lg:block")
    expect(sidebar).toHaveClass("lg:w-1/5")

});


test("Sidebar ham toggle", async () => {
    userInfo.setUserInfo("teacher", "token", "username")
    const handleHam = jest.fn()
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <DashboardSidebar hamburger={true} handleHam={handleHam}></DashboardSidebar>
            </SessionContext.Provider>
        </MemoryRouter>
    );
    const hamButton = screen.getByTestId("sidebar-ham")

    fireEvent.click(hamButton)

    expect(handleHam).toHaveBeenCalled()

});