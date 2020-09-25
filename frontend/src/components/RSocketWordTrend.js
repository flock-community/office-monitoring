import React, {useEffect, useState} from "react";
import {RadialChart} from "react-vis";
import {connectAndSubscribeToEndpoint, createRSocketClient} from "../util"
import Typography from "@material-ui/core/Typography";
import LineChart from "./LineChart";

const initialData = {
    total: 0,
    distribution: [{
            angle: 0.1,
            label: 'Flock.',
            x: 'Flock.',
            y: 0.1
        }]
};

let client = undefined;

const dateWithOffsetInMillis = (offset) => {
    const date = new Date()
    date.setMilliseconds(date.getMilliseconds() + offset)
    return date
}
const RSocketWordTrend = ({}) => {
    const [trend, setTrend] = useState(initialData);
    const [lineData, setLineData] = useState([])
    const [newDataPoint, setNewDataPoint] = useState({})

    useEffect(() => {
        console.log("RSocketWordTrend is here");
        subscribeToWordDistributions();



        setInterval(() => {
            setLineData(prevState => {
                let newValue = Math.floor(Math.random()*5);
                const newArray = prevState.concat({time: new Date(), value: newValue})
                    if (newArray.length > 5) {
                        newArray.shift()
                    }

                    return newArray
                })

            setNewDataPoint( _ => {
                return {time: new Date(), value: Math.floor(Math.random() * 5)};
            });
        }, 5000)


        return () => {
            cancelWordDistributions();
        }
    },[]);


    const mapToRadialChartFormat = (distributionDTO) => {
        return Object.entries(distributionDTO.wordDistribution).map(([word, value]) => {
            return {angle: value, label: word, x:word, y:value}
        });
    };

    const handleNewDist = distributionDTO => {
        let mapToRadialChartFormat1 = mapToRadialChartFormat(distributionDTO);
        console.log(mapToRadialChartFormat1);
        setTrend({
            total: distributionDTO.wordTotal,
            distribution: mapToRadialChartFormat1
        });
    };


    const subscribeToWordDistributions = () => {
        client = createRSocketClient();

        let onNext = payload => {
            console.log(payload.data);
            handleNewDist(payload.data || {})
        };

        let onSubscribe = subscription => {
            subscription.request(2147483647);
        };

        connectAndSubscribeToEndpoint(client, "word-distributions", onNext, onSubscribe)
    };

    const cancelWordDistributions = () => {
        client.close()
    };

    return (
        <>
        <Typography variant="h6" >out of {trend.total} words</Typography>
        {/*<RadialChart*/}
        {/*    data={trend.distribution}*/}
        {/*    labelsStyle={{*/}
        {/*        fontFamily: "Roboto",*/}
        {/*        fontSize: 18*/}
        {/*    }}*/}
        {/*    showLabels*/}
        {/*    width={300}*/}
        {/*    height={300}*/}
        {/*/>*/}
        <LineChart
            dataPoint={newDataPoint}
            data={lineData}
            title="Testing line charts"
            color="#3E517A"
        />
        </>)
};

export default RSocketWordTrend