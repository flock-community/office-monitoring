import React from 'react';


class LineChart extends React.Component {
    constructor(props) {
        super(props);
        this.chartRef = React.createRef();
    }

    componentDidUpdate() {
        // debugger;
        if (this.props.dataPoint.time != null){
            this.myChart.data.labels.push(this.props.dataPoint.time)
            this.myChart.data.datasets[0].data.push(this.props.dataPoint.value)
        }
        // this.myChart.data.labels = this.props.data.map(d => d.time);
        // this.myChart.data.datasets[0].data = this.props.data.map(d => d.value);
        if (this.myChart.data.labels.length > 20){
            this.myChart.data.labels.shift()
            this.myChart.data.datasets[0].data.shift()
        }
        this.myChart.update();
    }

    componentDidMount() {
        this.myChart = new Chart(this.chartRef.current, {
            type: 'line',
            options: {
                animation: {
                    duration: 800,
                    easing:'easeOutBack'
                },
                scales: {
                    xAxes: [
                        {
                            type: 'time',
                            time: {
                                unit: 'milliseconds'
                            }
                        }
                    ],
                    yAxes: [
                        {
                            ticks: {
                                min: 0,
                                max: 5
                            }
                        }
                    ]
                }
            },
            data: {
                labels: this.props.data.map(d => d.time),
                datasets: [{
                    label: this.props.title,
                    data: this.props.data.map(d => d.value),
                    fill: 'none',
                    backgroundColor: this.props.color,
                    // pointRadius: 5,
                    borderColor: this.props.color,
                    // borderWidth: 2,
                    // lineTension: 0.8,
                    cubicInterpolationMode: 'monotone'
                }]
            }
        });
    }

    render() {
        return <canvas ref={this.chartRef} />;
    }
}

export default LineChart