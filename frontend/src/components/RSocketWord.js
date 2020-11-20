import React, {useEffect, useState} from "react";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../RSocketUtil";
import Word from "./Word";
import {useAddToList} from "./Util";


const RSocketWord = ({alignRight}) => {
    const [words, addWord] = useAddToList();
    const [subscription, setSubscription] = useState(undefined);
    const [client, setClient] = useState(undefined)

    useEffect(() => {
        console.log("RSocketWord is here");
        subscribeToWords();

        return () => {
            cancelWords();
        }
    }, []);

    const subscribeToWords = () => {
        const rSocketClient = createRSocketClient();
        setClient(rSocketClient)

        let onNext = payload => {
            console.log(payload)
            addWord(JSON.stringify(payload.data))
        };

        let onSubscribe = sub => {
            setSubscription(sub);
            sub.request(2147483647);
        };

        connectAndSubscribeToEndpoint(rSocketClient, "start", onNext, onSubscribe)
    };

    const cancelWords = () => {
        client.close();
    };

    const requestWords = (number) => {
        console.log(`Requesting ${number} words`)
        subscription.request(number);
    };

    return <Word alignRight={alignRight} words={words} onRequest={requestWords}/>
};

export default RSocketWord