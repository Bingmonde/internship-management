import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { getUserInfo } from "../../../../utils/userInfo";
import { useSession } from "../../../CurrentSession";

const SessionDropdown = () => {
    const { t, i18n } = useTranslation();
    const { currentSession, setCurrentSession } = useSession();
    const [allSessions, setAllSessions] = useState([]);

    useEffect(() => {
        const getSess = async () => {
            await getAllSessions();
        };
        getSess();
    }, [currentSession]);

    const getAllSessions = async () => {
        try {
            const res = await fetch(`http://localhost:8080/academicSessions`, {
                method: 'GET',
                headers: {
                    'Authorization': getUserInfo().token,
                }
            });
            const data = await res.json();
            sortSessions(data.value);
            return data;
        } catch (error) {
            console.log(error);
        }
    };

    const seasonOrder = {
        "Hiver": 1,
        "Printemps": 2,
        "Été": 3,
        "Automne": 4
    };

    const sortSessions = (sessions) => {
        setAllSessions(sessions.sort((a, b) => {
            if (a.year !== b.year) {
                return b.year - a.year; // Sort by year descending
            } else {
                return seasonOrder[b.season] - seasonOrder[a.season]; // Sort seasons in defined order
            }
        }));
    };

    return (
        <div data-testid="sessionDropdown" className="ms-2 mt-3">
            {allSessions && (
                <div className="text-darkpurple">
                    <select
                        data-testid="sessionDropdown-select"
                        value={currentSession.id}
                        className="w-full bg-prose-neutral border-2 border-black p-2"
                        onChange={(e) => {
                            const selectedSession = allSessions.find(
                                (session) => session.id === parseInt(e.target.value)
                            );
                            setCurrentSession(selectedSession);
                        }}
                    >
                        <option disabled>{t('sessions')}</option>
                        {allSessions.map((session) => (
                            <option
                                data-testid="sessionDropdown-options"
                                key={session.id}
                                value={session.id}
                            >
                                {t(`season.${session.season}`)} {session.year}
                            </option>
                        ))}
                    </select>
                </div>
            )}
        </div>
    );
};

export default SessionDropdown;
