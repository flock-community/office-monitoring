import React, {useEffect, useState} from "react";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import {makeStyles} from "@material-ui/core/styles";
import {Input} from "@material-ui/core";
import Button from "@material-ui/core/Button";


const useStyles = makeStyles({
        align: {
            textAlign: "right",
            display: "flex",
            flexDirection: "row-reverse"
        },
        card: {
            border: "5px solid black",
        }
    });


const Word = ({alignRight, onRequest, words}) => {
    const [wordRequestBatchSize, setwordRequestBatch] = useState(1);
    const [toReceiveCount, setToReceiveCount] = useState(0);

    const classes = useStyles();

    useEffect(() => {
        setToReceiveCount(prevState => Math.max(0,prevState-1))
    }, [words]);

    const requestWord = () => {
        console.debug(`Requesting $wordRequestBatchSize words`);
        setToReceiveCount(toReceiveCount + wordRequestBatchSize)
        // subscription.request(wordRequestBatchSize);
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
                        <span>{value}</span>
                    </Typography>
                </Grid>
            )
        );

    return (
        <>
            <Grid container spacing={5}>
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