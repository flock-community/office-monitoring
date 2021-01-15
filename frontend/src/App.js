import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import {Grid, Typography} from "@material-ui/core";
import {HelloWorld} from "./components/HelloWorld";
import RSocketSingleStat from "./components/singleStat/RSocketSingleStat";
import RSocketRawDeviceStateList from "./components/rawDeviceState/RSocketRawDeviceStateList";


const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));



const App = () => {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    <Typography variant="h6">Hello world?  </Typography>
                </Grid>
                <Grid item xs={12}><HelloWorld/></Grid>
                <Grid item xs={12}>
                    <Typography variant="h6">Updates ... </Typography>
                </Grid>
                <Grid item xs={6}>
                    <RSocketSingleStat/>
                </Grid>
                <Grid item xs={6}>
                    <RSocketRawDeviceStateList/>
                </Grid>
            </Grid>
        </div>)

};

export default App