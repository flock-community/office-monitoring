import React, {useEffect, useState} from "react";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../RSocketUtil";


const requestParams = {
    deviceId: "1234abc",
    deviceType: "CONTACT_SENSOR",
    since: new Date().toISOString()
}

const useDeviceState = ({initialRequest = requestParams}) => {
    const [deviceState, setDeviceState] = useState(undefined)
    const [request, setRequest] = useState(initialRequest)
    const [subscription, setSubscription] = useState(undefined);
    const [client, setClient] = useState(undefined)
    const [connected, setConnected] = useState(false)

    useEffect(() => {
        console.log("useDeviceState is here");

        return () => {
            cancelDeviceState().then()
        }
    }, []);

    useEffect(() => {
        cancelDeviceState()
            .then(() => {
                subscribeToDeviceState()
            })

    }, [request])

    const setPayload = async ({deviceId, deviceType, since}) => {
        setRequest({deviceId, deviceType, since})
    }

    const subscribeToDeviceState = () => {
        console.log("Connecting to update stream...")
        const rSocketClient = createRSocketClient();

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

        connectAndSubscribeToEndpoint(rSocketClient, "mock", request, onNext, onSubscribe, onComplete, onError)
        setClient(rSocketClient)
    };

    const cancelDeviceState = async () => {
        if (!!client) await client.close();
    };

    return [deviceState, connected, setPayload]
}

export default useDeviceState
