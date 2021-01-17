import React from "react";
import Grid from "@material-ui/core/Grid";
import {Typography} from "@material-ui/core";
import PulsatingDot from "../PulsatingDot";
import RedDot from "../RedDot";
import useDeviceState from "../../hooks/useDeviceState.hook";


const requestParams = {
    deviceId: "1234abc",
    deviceType: "CONTACT_SENSOR",
    since: new Date().toISOString()
}
const SingleStat = ({
                        request = requestParams,
                        property = "contact",
                        background = (p) => `background-${p === 'false' ? 'red' : p === 'true' ? 'green' : 'yellow'}`
                    }) => {

    const [deviceState, connected, updateRequest] = useDeviceState({initialRequest: request})

    const nonNullState = () => deviceState || {};
    const getDeviceId = () => nonNullState().id
    const getDeviceType = () => nonNullState().type
    const state = () => nonNullState().state || {};

    const getBackground = () => background(getProperty())

    const getProperty = () => {
        const x = state()[property]
        return x === undefined ? "" : x.toString()
    }
    return (
        <Grid item className={getBackground() + " single-stat"}>
            <div>{connected ? (<PulsatingDot/>) : (<RedDot/>)}</div>
            <Typography variant="h6">id: {getDeviceId()}</Typography>
            <Typography variant="h6">type: {getDeviceType()}</Typography>
            <Typography variant="h6">{property}: {getProperty()}</Typography>
        </Grid>
    )
};

export default SingleStat