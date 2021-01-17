import React, {useEffect} from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import {useAddToList} from "../Util";
import useDeviceState from "../../hooks/useDeviceState.hook";
import PulsatingDot from "../PulsatingDot";
import RedDot from "../RedDot";

const RawDeviceStateList = ({request, itemsLimit = 10}) => {
    const [deviceStates, addToList] = useAddToList(itemsLimit);
    const [deviceState, connected, updateRequest] = useDeviceState({initialRequest: request})

    useEffect(() => {
        addToList(JSON.stringify(deviceState, null, 2))
    }, [deviceState]);

    return (
        <>
            <Grid item container spacing={5}>
                <Grid item xs={12}>
                    <Typography variant="h6">RawDeviceStateList ({itemsLimit} latest items)</Typography>
                    {connected ? (<PulsatingDot/>) : (<RedDot/>)}
                </Grid>
                <Grid item container spacing={2}>
                    {(deviceStates || []).map((value, idx) =>
                        (
                            <Grid key={idx} item xs={12}>
                                <Typography variant="h6">
                                    <span><pre>{value}</pre></span>
                                </Typography>
                            </Grid>
                        )
                    )}
                </Grid>
            </Grid>
        </>
    )
};

export default RawDeviceStateList