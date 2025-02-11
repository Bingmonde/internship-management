import {Trans, useTranslation} from "react-i18next";
import {getUserInfo} from "../../../utils/userInfo";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
    faBars, faRightFromBracket,
} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import SessionDropdown from "./sessionDropdown/sessionDropdown";

const DashboardTopbar = ({handleLogout, handleHam}) => {

    const { t, i18n } = useTranslation();

    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate();

    const username = getUserInfo().username;






    return (
        <>
            {/*<div*/}
            {/*    className="h-14 w-screen lg:w-4/5 flex items-center justify-end pe-32 absolute top-0 z-10 bg-lightpurple border-b-2 border-darkpurple "></div>*/}

            <div
                className="flex flex-col md:flex-row md:h-14 lg:w-4/5 w-full items-start md:items-center justify-center absolute top-0 z-10 bg-lightpurple border-b-2 border-darkpurple
                bg-gradient-to-tl from-darkpurple from-10% via-selected via-30% to-lightpurple to-90% ">

                <div className="flex flex-row self-center px-3 lg:1/5 md:w-1/3 w-full">
                    <FontAwesomeIcon data-testid={"topbar-ham"} icon={faBars}
                                     className="inline-block lg:hidden mr-auto px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 z-30 text-white "
                                     onClick={handleHam}/>
                    <div className="w-full me-5 md:w-auto flex justify-center md:justify-end">
                        <SessionDropdown/>
                    </div>
                </div>


                {/*<div className="ml-auto self-center pl-auto pt-0">{date}</div>*/}
                <div className="md:px-2 self-center md:w-2/3 w-full p-2 me-auto text-prose-neutral" data-testid={"topbar-username"}>
                    {username && <Trans i18nKey="welcome">Welcome, <b>{{username}}</b></Trans>}
                    {username && (
                        <FontAwesomeIcon
                            data-testid={"topbar-logout"}
                            icon={faRightFromBracket}
                            className="px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 cursor-pointer"
                            onClick={handleLogout}
                        />
                    )}
                </div>
            </div>

            {/*<div*/}
            {/*    className="h-14 w-screen lg:w-4/5 flex items-center justify-end pe-32 absolute top-0 z-10 bg-lightpurple border-b-2 border-darkpurple "></div>*/}

            {/*<div*/}
            {/*    className="flex flex-row h-14 w-4/5 md:w-2/3 items-center justify-center absolute top-0 z-10 bg-lightpurple border-b-2 border-darkpurple">*/}
            {/*    <FontAwesomeIcon data-testid={"topbar-ham"} icon={faBars}*/}
            {/*                     className="inline-block lg:hidden mr-auto px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 "*/}
            {/*                     onClick={handleHam}/>*/}
            {/*    <SessionDropdown></SessionDropdown>*/}

            {/*    <div className="ml-auto self-center pl-auto pt-0">{date}</div>*/}
            {/*    <div className="px-2 self-center" data-testid={"topbar-username"}>*/}
            {/*        {username && <Trans i18nKey="welcome">Welcome, <b>{{username}}</b></Trans>}*/}
            {/*    </div>*/}
            {/*    {username && (*/}
            {/*        <FontAwesomeIcon*/}
            {/*            data-testid={"topbar-logout"}*/}
            {/*            icon={faRightFromBracket}*/}
            {/*            className="px-4 self-center hover:scale-125 transition-transform duration-300 pb-0 cursor-pointer"*/}
            {/*            onClick={handleLogout}*/}
            {/*        />*/}
            {/*    )}*/}
            {/*</div>*/}

        </>
    )
}

export default DashboardTopbar;