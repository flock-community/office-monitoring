import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import {Grid, Typography} from "@material-ui/core";
import {HelloWorld} from "./components/HelloWorld";
import SingleStat from "./components/singleStat/SingleStat";
import RawDeviceStateList from "./components/rawDeviceState/RawDeviceStateList";
import './css/App.css'

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
                    <Grid item xs={6}>
                        <SingleStat property={"contact"} request= {{ deviceType:"CONTACT_SENSOR"}}/>
                    </Grid>
                    <Grid item xs={4}>
                        <SingleStat property={"temperature"} request={{
                            deviceId: "myThermometerId#1",
                            deviceType: "TEMPERATURE_SENSOR",
                            since: new Date().toISOString()
                        }} background={(p) => {
                            let x
                            if (p < 10) x = 'blue'
                            else if (p < 18) x = 'yellow'
                            else if (p < 25) x = 'orange'
                            else x = 'red'
                            return `background-${x}`
                        }}
                        />
                    </Grid>
                    <Grid item xs={6}>
                        <RawDeviceStateList/>
                    </Grid>

                </Grid>
            </Grid>
        </div>)

};

export default App