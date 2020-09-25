import React, {useEffect, useState} from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../util";
import Button from "@material-ui/core/Button";
import {Input} from "@material-ui/core";


let client = undefined;
const LatestWords = ({}) => {
    const [latestWords, setLatestWords] = useState([]);
    const [wordRequestBatchSize, setwordRequestBatch] = useState(10);
    const [subscription, setSubscription] = useState(undefined);
    const [toReceiveCount, setToReceiveCount] = useState(0);

    useEffect(() => {
        console.log("LatestWords is here");
        subscribeToWords();

        return () => {
            cancelWords();
        }
    },[]);

    useEffect(()=>{
        console.debug("Update toReceiveCount: ", toReceiveCount)
    },[toReceiveCount]);

    const subscribeToWords = () => {
        client =createRSocketClient();


        let onNext = payload => {
            console.log(payload.data);
            addWordToList(payload.data.word || '<no-word>');
            setToReceiveCount((count) => count - 1);
        };

        let onSubscribe = sub => {
                setSubscription(sub);
                setToReceiveCount((count) => count + wordRequestBatchSize);
                sub.request(wordRequestBatchSize);
        };

        connectAndSubscribeToEndpoint(client, "words", onNext, onSubscribe)
    };


    const addWordToList = (word) => {
        setLatestWords(prevState => {
            const newArray = [word].concat(prevState)
            if (newArray.length > 15) {
                newArray.pop()
            }

            return newArray
        })

    }

    const cancelWords = () => {
        client.close();
    };

    const response = () =>
        latestWords.map((value, idx) =>
            (
                <Grid key={idx} item xs={12}>
                    <Typography variant="h6">{idx+1} - {value}</Typography>
                </Grid>
            )
        );


    const requestWord = () => {
        console.log("Requesting");
        setToReceiveCount(toReceiveCount + wordRequestBatchSize)
        subscription.request(wordRequestBatchSize);
    };

    const changeWordRequestBatch = (event) => {
        console.log(event.target.value);
        setwordRequestBatch(+event.target.value)
    }

    return (
        <>
            <Grid container spacing={5}>
                <Grid item  xs={4}>
                    <Input onChange={changeWordRequestBatch}  defaultValue={wordRequestBatchSize} type="number" />
                </Grid>
                <Grid item xs={4} >
                    <Button variant="contained" onClick={requestWord}>Request {wordRequestBatchSize} word(s)</Button>
                </Grid>
                <Grid item  xs={4}>
                    <Typography variant="h6">Waiting for # words:</Typography>
                    <Typography align="center" variant="h6">{toReceiveCount} </Typography>
                </Grid>
                <Grid item container spacing={2}>
                {response()}
                </Grid>
            </Grid>
        </>
    )
};

export default LatestWords