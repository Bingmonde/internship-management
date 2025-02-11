import {useTranslation} from "react-i18next";
import React, {useState} from "react";
import {useOutletContext} from "react-router-dom";


const Building = () => {
        const [setPdfModalHelper] = useOutletContext()

    const { t, i18n } = useTranslation();

    return (
        <div className={"fullpage-form"}>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
            <h1>En construction</h1>
                <button onClick={() => setPdfModalHelper("https://www.orimi.com/pdf-test.pdf","Ceci est un long title ish woah woah woah cool")} className={"p-4 bg-black text-white"}>openPdf</button>
        </div>
    );
}

export default Building