import React, {useEffect, useState} from "react";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../RSocketUtil";
import RawDeviceStateList from "./RawDeviceStateList";
import {useAddToList} from "./Util";
import {Grid} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import PulsatingDot from "./PulsatingDot";
import RedDot from "./RedDot";


const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));

const RSocketRawDeviceStateList = ({alignRight}) => {
    const [deviceState, setDeviceState] = useState(undefined)
    const [subscription, setSubscription] = useState(undefined);
    const [client, setClient] = useState(undefined)
    const [connected, setConnected] = useState(false)

    const classes = useStyles()
    useEffect(() => {
        console.log("RSocketRawDeviceStateList is here");
        subscribeToWords();

        return () => {
            cancelWords();
        }
    }, []);

    const subscribeToWords = () => {
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
            setTimeout(subscribeToWords, 5000)
        }

        const onError = () => {
            setConnected(false)
            setTimeout(subscribeToWords, 5000)
        }

        const requestParams = {
            deviceId:"1234abc",
            deviceType: "CONTACT_SENSOR",
            since: new Date().toISOString()
        }

        connectAndSubscribeToEndpoint(rSocketClient, "mock", requestParams,onNext, onSubscribe, onComplete, onError)
    };

    const cancelWords = () => {
        client.close();
    };

    const requestWords = (number) => {
        console.log(`Requesting ${number} words`)
        subscription.request(number);
    };

    return <Grid item container spacing={2}>
        <Grid item={12}>
            Connected: {connected ? (<PulsatingDot />) : (<RedDot />)}
        </Grid>
        <Grid item xs={12}>

            <RawDeviceStateList alignRight={alignRight} deviceState={deviceState} onRequest={requestWords} initialRequest={2147483647}/>
        </Grid>
    </Grid>
};

export default RSocketRawDeviceStateList