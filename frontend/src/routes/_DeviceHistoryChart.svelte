<script lang="ts">
  import {beforeUpdate, onMount} from "svelte";
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import * as am4plugins_timeline from "@amcharts/amcharts4/plugins/timeline";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";

  import type { TimelineChartRecord } from "./model";

  export let chartData: TimelineChartRecord[];

  let chart
  onMount(async () => {
    am4core.ready(onready);
  });


  async function delay(msec) {
    return new Promise(resolve => {  setTimeout(() => { resolve('') }, msec);})
  }

  let updating = false;
  beforeUpdate(async () => {
    if (updating) return;
    updating = true;

    console.log("Updating DeviceChartHistory")
    await delay(100);
    if (!!chart) {
      chart.data = chartData
    }
    updating = false;

  });


  function onready() {
    am4core.useTheme(am4themes_animated);

    chart = am4core.create("chartdiv", am4plugins_timeline.SerpentineChart);
    chart.curveContainer.padding(50, 20, 50, 20);
    chart.levelCount = 4;
    chart.yAxisRadius = am4core.percent(25);
    chart.yAxisInnerRadius = am4core.percent(-25);
    chart.maskBullets = false;
    chart.logo.disabled = true;

    let colorSet = new am4core.ColorSet();
    colorSet.saturation = 0.5;

    chart.data = chartData;

    chart.dateFormatter.dateFormat = "yyyy-MM-dd HH:mm";
    chart.dateFormatter.inputDateFormat = "yyyy-MM-dd HH:mm";
    chart.fontSize = 11;

    let categoryAxis = chart.yAxes.push(new am4charts.CategoryAxis());
    categoryAxis.dataFields.category = "category";
    categoryAxis.renderer.grid.template.disabled = true;
    categoryAxis.renderer.labels.template.paddingRight = 25;
    categoryAxis.renderer.minGridDistance = 10;
    categoryAxis.renderer.innerRadius = -60;
    categoryAxis.renderer.radius = 60;

    let dateAxis = chart.xAxes.push(new am4charts.DateAxis());
    dateAxis.renderer.minGridDistance = 70;
    dateAxis.baseInterval = { count: 1, timeUnit: "minute" };
    dateAxis.renderer.tooltipLocation = 0;
    dateAxis.startLocation = -0.5;
    dateAxis.renderer.line.strokeDasharray = "1,4";
    dateAxis.renderer.line.strokeOpacity = 0.6;
    dateAxis.tooltip.background.fillOpacity = 0.2;
    dateAxis.tooltip.background.cornerRadius = 5;
    dateAxis.tooltip.label.fill = new am4core.InterfaceColorSet().getFor(
      "alternativeBackground"
    );
    dateAxis.tooltip.label.paddingTop = 7;
    // const min = new Date()
    // min.setHours(min.getHours() - 2)

    // dateAxis.min = min.getTime();
    dateAxis.max = new Date().getTime();

    let labelTemplate = dateAxis.renderer.labels.template;
    labelTemplate.verticalCenter = "middle";
    labelTemplate.fillOpacity = 0.7;
    labelTemplate.background.fill = new am4core.InterfaceColorSet().getFor(
      "background"
    );
    labelTemplate.background.fillOpacity = 1;
    labelTemplate.padding(7, 7, 7, 7);

    let series = chart.series.push(new am4plugins_timeline.CurveColumnSeries());
    series.columns.template.height = am4core.percent(20);
    series.columns.template.tooltipText =
      "{category} geopend van [bold]{openDateX}[/] tot [bold]{dateX}[/]";

    series.dataFields.openDateX = "start";
    series.dataFields.dateX = "end";
    series.dataFields.categoryY = "category";
    series.columns.template.strokeOpacity = 0;

    chart.scrollbarX = new am4core.Scrollbar();
    chart.scrollbarX.align = "center";
    chart.scrollbarX.width = am4core.percent(85);

    let cursor = new am4plugins_timeline.CurveCursor();
    chart.cursor = cursor;
    cursor.xAxis = dateAxis;
    cursor.yAxis = categoryAxis;
    cursor.lineY.disabled = true;
    cursor.lineX.strokeDasharray = "1,4";
    cursor.lineX.strokeOpacity = 1;

    dateAxis.renderer.tooltipLocation2 = 0;
    categoryAxis.cursorTooltipEnabled = false;
  }
</script>

<div class="p-3 h-full w-full">
  <div class="w-full h-full" id="chartdiv" />
</div>
