import * as userInfo from "../../../../utils/userInfo";
import {fireEvent, render, screen} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import {SessionContext} from "../../../CurrentSession";
import Dashboard from "../../dashboard";
import SessionDropdown from "./sessionDropdown";
import "../../../../utils/i18n/index.mock.js";

const mockResponse = {
    value: [{id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}],
    exception: ""
}
const mockResponse2 = {
    value: [
        {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []},
        {id: 2, season: "Été", year: "2025", startDate: "", endDate: "", jobOffers: []}
    ],
    exception: ""
}
const mockResponse3 = {
    value: [
        {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []},
        {id: 3, season: "Automne", year: "2024", startDate: "", endDate: "", jobOffers: []},
        {id: 2, season: "Été", year: "2025", startDate: "", endDate: "", jobOffers: []}
    ],
    exception: ""
}
beforeEach(() => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
        json: jest.fn().mockResolvedValue(JSON.parse(JSON.stringify(mockResponse)))
    })
    userInfo.setUserInfo("student", "token", "username")
});

afterEach(() => {
    jest.restoreAllMocks();
    userInfo.clearUserInfo();
});

test("Session Dropdown render", async () => {

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <SessionDropdown></SessionDropdown>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const topdiv = screen.getByTestId("sessionDropdown")
    expect(topdiv).toBeInTheDocument()
});

test("Session Dropdown one sessions", async () => {

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <SessionDropdown></SessionDropdown>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const options = await screen.findAllByTestId("sessionDropdown-options")
    expect(options.length).toBe(1)
});

test("Session Dropdown two sessions", async () => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
        json: jest.fn().mockResolvedValue(JSON.parse(JSON.stringify(mockResponse2)))
    })

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <SessionDropdown></SessionDropdown>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const options = await screen.findAllByTestId("sessionDropdown-options")
    expect(options.length).toBe(2)
    expect(options[0].text).toBe(mockResponse2.value[0].season + " " + mockResponse2.value[0].year)
    expect(options[1].text).toBe(mockResponse2.value[1].season + " " + mockResponse2.value[1].year)
});

test("Session Dropdown three sessions sort", async () => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
        json: jest.fn().mockResolvedValue(JSON.parse(JSON.stringify(mockResponse3)))
    })

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <SessionDropdown></SessionDropdown>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const options = await screen.findAllByTestId("sessionDropdown-options")
    console.log(options[0].text)
    console.log(options[1].text)
    console.log(options[2].text)
    console.dir(mockResponse3)
    expect(options.length).toBe(3)
    expect(options[0].text).toBe(mockResponse3.value[0].season + " " + mockResponse3.value[0].year)
    expect(options[1].text).toBe(mockResponse3.value[2].season + " " + mockResponse3.value[2].year)
    expect(options[2].text).toBe(mockResponse3.value[1].season + " " + mockResponse3.value[1].year)
});

test("Session Dropdown change session", async () => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
        json: jest.fn().mockResolvedValue(JSON.parse(JSON.stringify(mockResponse2)))
    })
    const setCurrentSession = jest.fn()

    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []},setCurrentSession:setCurrentSession}}>
                <SessionDropdown></SessionDropdown>
            </SessionContext.Provider>
        </MemoryRouter>
    );

    const dropdown = await screen.findByTestId("sessionDropdown-select")

    fireEvent.change(dropdown)

    expect(setCurrentSession).toBeCalled()
});