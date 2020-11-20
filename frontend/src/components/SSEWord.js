import React, {useEffect, useState} from "react";
import {createEventSource} from "../EventSourceUtil"
import Word from "./Word";
import {useAddToList} from "./Util";
import PulsatingDot from "./PulsatingDot";
import RedDot from "./RedDot";
import {Grid} from "@material-ui/core";


const SSEWord = ({alignRight = false}) => {
    const [words, addWord] = useAddToList();
    const [source, setSource] = useState(undefined);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        console.log("Word is here");
        subscribeToWords();

        return () => {
            cancelWords();
        }
    }, []);

    const subscribeToWords = () => {
        const onError = () => {
            setConnected(false)
            setTimeout(subscribeToWords, 5000)
        }

        const source = createEventSource(`//api.office.flock.community/device-updates`, handleNewWord, onError)
        setSource(source);
        setConnected(true)
    };

    const handleNewWord = (event) => {
        console.debug("hello new word", event);
        const word = JSON.parse(event.data);
        addWord(event.data)
    };


    const cancelWords = () => {
        source.close()
    };

    return <Grid item container>
        <Grid item={12}>
            Connected: {connected ? (<PulsatingDot/>) : (<RedDot/>)}
        </Grid>
        <Grid>
            <Word
                alignRight={alignRight}
                onRequest={() => {
                }}
                words={words}
            />
        </Grid>
    </Grid>
};

export default SSEWord