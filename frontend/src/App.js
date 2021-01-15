import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import {Grid, Typography} from "@material-ui/core";
import {HelloWorld} from "./components/HelloWorld";
import RSocketRawDeviceStateList from "./components/RSocketRawDeviceStateList";
import SSEWord from "./components/SSEWord";


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
                <Grid item xs={12}>
                    <RSocketRawDeviceStateList/>
                </Grid>
            </Grid>
        </div>)

};

export default App