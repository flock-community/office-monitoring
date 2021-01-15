import React, {useEffect, useState} from "react";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../../RSocketUtil";
import SingleStat from "./SingleStat";
import {Grid} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";


const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));

const RSocketSingleStat = ({property, deviceType,background }) => {
    const [deviceState, setDeviceState] = useState(undefined)
    const [subscription, setSubscription] = useState(undefined);
    const [client, setClient] = useState(undefined)
    const [connected, setConnected] = useState(false)

    useEffect(() => {
        console.log("RSocketSingleStat is here");
        subscribeToDeviceState();

        return () => {
            cancelWords();
        }
    }, []);

    const subscribeToDeviceState = () => {
        console.log("Connecting to update stream...")
        const rSocketClient = createRSocketClient();
        setClient(rSocketClient)

        let onNext = payload => {
            console.log(payload)
            setDeviceState(payload.data)
            setConnected(true)
        };

        let onSubscribe = sub => {
            setSubscription(sub);
            sub.request(2147483647);
        };

        const onComplete = () => {
            setConnected(false)
            setTimeout(subscribeToDeviceState, 5000)
        }

        const onError = () => {
            setConnected(false)
            setTimeout(subscribeToDeviceState, 5000)
        }

        const requestParams = {
            deviceId: "1234abc",
            deviceType: deviceType || "CONTACT_SENSOR",
            since: new Date().toISOString()
        }

        connectAndSubscribeToEndpoint(rSocketClient, "mock", requestParams, onNext, onSubscribe, onComplete, onError)
    };

    const cancelWords = () => {
        client.close();
    };
    return (
        <Grid item container xs={12} >
            <SingleStat deviceState={deviceState} connected={connected} property={property} background={background}/>
        </Grid>
    )
};

export default RSocketSingleStat