import React, {useEffect, useState} from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import {Input} from "@material-ui/core";
import Button from "@material-ui/core/Button";

const Word = ({onRequest, words, initialRequest=1}) => {
    const [wordRequestBatchSize, setwordRequestBatch] = useState(10);
    const [toReceiveCount, setToReceiveCount] = useState(initialRequest);


    useEffect(() => {
        setToReceiveCount(prevState => Math.max(0,prevState-1))
    }, [words]);

    const requestWord = () => {
        console.debug(`Requesting $wordRequestBatchSize words`);
        setToReceiveCount(toReceiveCount + wordRequestBatchSize)
        onRequest(wordRequestBatchSize)
    };

    const changeWordRequestBatch = (event) => {
        console.debug(event.target.value);
        setwordRequestBatch(+event.target.value)
    }

    const showWords = () =>
        words.map((value, idx) =>
            (
                <Grid key={idx} item xs={12}>
                    <Typography variant="h6" >
                        <span><pre>{value}</pre></span>
                    </Typography>
                </Grid>
            )
        );

    return (
        <>
            <Grid item container spacing={5}>
                <Grid item xs={4}>
                    <Input onChange={changeWordRequestBatch} defaultValue={wordRequestBatchSize} type="number"/>
                </Grid>
                <Grid item xs={4}>
                    <Button variant="contained" onClick={requestWord}>Request {wordRequestBatchSize} word(s)</Button>
                </Grid>
                <Grid item xs={4}>
                    <Typography variant="h6">Waiting for # words:</Typography>
                    <Typography align="center" variant="h6">{toReceiveCount} </Typography>
                </Grid>
                <Grid item container spacing={2}>
                    {showWords()}
                </Grid>
            </Grid>
        </>
    )
};

export default Word