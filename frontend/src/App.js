import React from "react";
import SideMenu from "./components/SideMenu";
import TopMenu from "./components/TopMenu";
import Footer from "./components/Footer";
import MainContent from "./components/MainContent";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));

const App = () => {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <TopMenu/>
            <SideMenu/>
            <MainContent/>
            <Footer/>
        </div>)

};

export default App