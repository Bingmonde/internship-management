import {Outlet} from "react-router-dom";
import App from "../App";
import {PageInfo} from "./page_info";

const Layout = () => {
    return <>

        <Outlet />
    </>
}
export default Layout