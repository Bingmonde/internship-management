
import './switch.css'
import {useState} from "react";

const Switch = ({ checked, handleToggle }) => {
    const [value, setValue] = useState(checked);
    return (
        <>
            <input
                checked={value}
                onChange={
                    ()=> { setValue(!value); handleToggle()}}
                className="react-switch-checkbox"
                id={`react-switch-new`}
                type="checkbox"
            />
            <label
                style={{ background: value && '#1E555C' }}
                className={"react-switch-label"}
                htmlFor={`react-switch-new`}
            >
                <span className={`react-switch-button`}/>
            </label>
        </>
    )
}

export default Switch;