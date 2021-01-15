import React from "react";
import Grid from "@material-ui/core/Grid";
import {Typography} from "@material-ui/core";

const SingleStat = ({deviceState}) => {
    const state =  () =>  (deviceState || {}).state || {};
    const isContact = () => !!state().contact
    const getDeviceId = () => (deviceState || {}).id
    const getDeviceType = () => (deviceState || {}).type
    return (
        <Grid item container spacing={5}>
            <Grid item container spacing={2}>
                <Typography variant="h6">id: {getDeviceId()}</Typography>
                <Typography variant="h6">type: {getDeviceType()}</Typography>
                <Typography variant="h6">contact: {isContact() ? ("dicht") : ("open")}</Typography>
            </Grid>
        </Grid>
    )
};

export default SingleStat