import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import {Grid, Typography} from "@material-ui/core";
import {HelloWorld} from "./components/HelloWorld";
// import RSocketSingleStat from "./components/singleStat/RSocketSingleStat";

import './css/App.css'
import RSocketRawDeviceStateList from "./components/rawDeviceState/RSocketRawDeviceStateList";
import SingleStat from "./components/singleStat/SingleStat";

const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));


const App = () => {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <Grid container spacing={2}>
                <Grid item xs={12}>
                    <Typography variant="h6">Hello world? </Typography>
                </Grid>
                <Grid item xs={12}><HelloWorld/></Grid>
                <Grid item xs={12}>
                    <Typography variant="h6">Updates ... </Typography>
                </Grid>
                <Grid item container spacing={2}>
                    <Grid item xs={12}>
                        <SingleStat property={"contact"} deviceType={"CONTACT_SENSOR"}
                        />
                    </Grid>
                    <Grid item xs={12}>
                        {/*<RSocketSingleStat property={"temperature"} deviceType={"TEMPERATURE_SENSOR"} background= { (p) =>{*/}
                        {/*    let x*/}
                        {/*    if (p < 10 ) x = 'blue'*/}
                        {/*    else if (p < 18 ) x = 'yellow'*/}
                        {/*    else if (p < 25) x = 'orange'*/}
                        {/*    else x = 'red'*/}
                        {/*    return `background-${x}`*/}
                        {/*}}*/}
                        {/*/>*/}
                    </Grid>
                    <Grid item xs={6}>
                        {/*<RSocketRawDeviceStateList/>*/}
                    </Grid>

                </Grid>
            </Grid>
        </div>)

};

export default App