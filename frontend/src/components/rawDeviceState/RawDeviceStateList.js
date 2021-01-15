import React, {useEffect} from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import {useAddToList} from "../Util";

const RawDeviceStateList = ({deviceState}) => {
    const [deviceStates, addToList] = useAddToList(2);

    useEffect(() => {
        addToList(JSON.stringify(deviceState, null, '\t'))
    }, [deviceState]);

    return (
        <>
            <Grid item container spacing={5}>
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