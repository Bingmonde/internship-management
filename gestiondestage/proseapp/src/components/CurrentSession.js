import React, { createContext, useContext, useState, useEffect } from 'react';
import {getUserInfo} from "../utils/userInfo";

export const SessionContext = createContext();

export const SessionProvider = ({ children }) => {
    const [currentSession, setCurrentSession] = useState({});
    //const currentUser = getUserInfo().username;

    useEffect(() => {
        const getCurrentSession = async () => {
            try {
                const interval = setInterval(async () => {
                    const userInfo = getUserInfo();

                    if (userInfo && userInfo.token) {
                        clearInterval(interval); // Arrêter l'intervalle une fois le token disponible

                        const res = await fetch(`http://localhost:8080/currentAcademicSession`, {
                            method: 'GET',
                            headers: {
                                'Authorization': userInfo.token,
                            }
                        });
                        const data = await res.json();
                        console.log('Session en cours:', data.value);
                        const sessFromStorage = sessionStorage.getItem("currentSession");
                        if(sessFromStorage) {
                            setCurrentSession(JSON.parse(sessFromStorage));
                        }
                        else {
                            setCurrentSession(data.value);
                        }
                    } else {
                        console.log("Token non disponible, en attente de connexion.");
                    }
                }, 1000); // Vérifie toutes les secondes
            } catch (error) {
                console.error("Erreur lors de la récupération de la session:", error);
            }
        };

        getCurrentSession();
    }, []);


    useEffect(() => {
        if(currentSession && currentSession.season && currentSession.year) {
            sessionStorage.setItem("currentSession", JSON.stringify(currentSession));
        }
    }, [currentSession]);

    return (
        <SessionContext.Provider value={{ currentSession, setCurrentSession }}>
            {children}
        </SessionContext.Provider>
    );
};

export const useSession = () => useContext(SessionContext);