import React from "react";
import {makeStyles} from "@material-ui/core/styles";
import {Grid, Typography} from "@material-ui/core";
import {HelloWorld} from "./components/HelloWorld";
import Card from "@material-ui/core/Card";
import CardHeader from "@material-ui/core/CardHeader";
import CardContent from "@material-ui/core/CardContent";
import RSocketWord from "./components/RSocketWord";


const useStyles = makeStyles(theme => ({
    root: {
        display: 'flex',
    },
}));


const App = () => {
    const classes = useStyles();

    return (
        <div className={classes.root}>
            <Grid container spacing={1}>
                <Grid><HelloWorld/></Grid>
                <Grid item xs={12}>
                    <Typography variant="h6">Using rSocket</Typography>
                </Grid>
                <Grid item xs={6}>
                    <Card>
                        <CardHeader title="WordTrend"></CardHeader>
                        <CardContent>
                            <RSocketWord/>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>
        </div>)

};

export default App