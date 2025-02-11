import * as userInfo from "../../utils/userInfo";
import {render} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import Dashboard from "./dashboard";
import "../../utils/i18n/index.mock.js";
import {SessionContext} from "../CurrentSession";


const mockResponse = {
            value: [{id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}],
            exception: ""
        }
beforeEach(() => {
    jest.spyOn(global, 'fetch').mockResolvedValue({
        json: jest.fn().mockResolvedValue(mockResponse)
    })
    userInfo.setUserInfo("student", "token", "username")
});

afterEach(() => {
    jest.restoreAllMocks();
    userInfo.clearUserInfo();
});

test("Dashboard render", async () => {
    console.log(SessionContext);
    render(
        <MemoryRouter>
            <SessionContext.Provider value={{currentSession: {id: 1, season: "Hiver", year: "2025", startDate: "", endDate: "", jobOffers: []}}}>
                <Dashboard setNotifications={() => {}} notifications={[]}/>
            </SessionContext.Provider>
        </MemoryRouter>
    );
});

// No outlet tests because they will probably be replaced once notification FE will be ready


