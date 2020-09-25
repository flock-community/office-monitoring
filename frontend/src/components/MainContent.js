import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import {Grid, Typography} from '@material-ui/core';
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import CardHeader from "@material-ui/core/CardHeader";
import LatestWords from "./LatestWords";
import RSocketWordTrend from "./RSocketWordTrend";


const useStyles = makeStyles(theme => ({
    toolbar: theme.mixins.toolbar,
    title: {
        flexGrow: 1,
        backgroundColor: theme.palette.background.default,
        padding: theme.spacing(3),
    },
    content: {
        flexGrow: 1,
        padding: theme.spacing(3),
    },
    fullWidth: {
        width: '80%',
    },
}));

function MainContent() {
    const classes = useStyles();

    return (
        <main className={classes.fullWidth}>
            <div className={classes.toolbar} />
            <div className={classes.title}>
                <Typography variant='h6'>Trending words</Typography>
            </div>
            <div className={classes.content}>
                <Typography paragraph>
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc maximus,
                    nulla ut commodo sagittis, sapien dui mattis dui, non pulvinar lorem
                    felis nec erat
                </Typography>
                <Grid container spacing={1}>
                    <Grid item xs={12}>
                        <Typography variant="h6">Using rSocket</Typography>
                    </Grid>
                    <Grid item xs={6}>
                        <Card>
                            <CardHeader title="WordTrend"></CardHeader>
                            <CardContent>
                                <RSocketWordTrend />
                            </CardContent>
                        </Card>

                    </Grid>
                    {/*<Grid item xs={6}>*/}
                    {/*    <Card>*/}
                    {/*        <CardHeader title="Latest messages"></CardHeader>*/}
                    {/*        <CardContent>*/}
                    {/*            <LatestWords />*/}
                    {/*        </CardContent>*/}
                    {/*    </Card>*/}

                    {/*</Grid>*/}
                </Grid>
            </div>
        </main>
    );
}

export default MainContent;