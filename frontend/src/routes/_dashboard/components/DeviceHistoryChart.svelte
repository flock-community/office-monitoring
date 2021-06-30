<script lang="ts">
  import {beforeUpdate, onMount} from "svelte";
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import * as am4plugins_timeline from "@amcharts/amcharts4/plugins/timeline";
  import * as am4plugins_bullets from "@amcharts/amcharts4/plugins/bullets";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";

  import type { TimelineChartRecord } from "./model";

  export let chartData: TimelineChartRecord[];

  let chart
  onMount(async () => {
    am4core.ready(onready);
  })

  beforeUpdate(async () => {
    if (!!chart) {
      chart.data = chartData
    }
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

    chart.dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss";
    chart.dateFormatter.inputDateFormat = "i";
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
    dateAxis.tooltip.label.fill = "color"
    dateAxis.tooltip.label.fill = new am4core.InterfaceColorSet().getFor(
      "alternativeBackground"
    );
    dateAxis.tooltip.label.paddingTop = 7;
    // const min = new Date()
    // min.setHours(min.getHours() - 4)
    //
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
    series.columns.template.height = am4core.percent(10);
    series.columns.template.tooltipText = "{openDateX}\n{text}";
    series.tooltip.getFillFromObject = false;
    // series.tooltip.label.propertyFields.fill = "color";
    // series.tooltip.background.propertyFields.stroke = "color";
    series.tooltip.background.propertyFields.fill = "color";

    series.dataFields.openDateX = "start";
    series.dataFields.dateX = "end";
    series.dataFields.categoryY = "category";
    // series.columns.template.strokeOpacity = 0;

    series.columns.template.propertyFields.fill = "color"; // get color from data
    series.columns.template.propertyFields.stroke = "color";
    series.columns.template.strokeOpacity = 0;
    series.columns.template.fillOpacity = 0.6;

    // series.columns.tooltip.template.propertyFields.fill = "color"
    // series.tooltip.template.propertyFields.stroke = "color"

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

    let imageBullet1 = series.bullets.push(new am4plugins_bullets.PinBullet());
    imageBullet1.background.radius = 18;
    imageBullet1.locationX = 1;
    imageBullet1.propertyFields.stroke = "color";
    imageBullet1.background.propertyFields.fill = "color";
    imageBullet1.image = new am4core.Image();
    imageBullet1.image.propertyFields.href = "icon";
    imageBullet1.image.scale = 0.7;
    imageBullet1.circle.radius = am4core.percent(100);
    imageBullet1.background.fillOpacity = 0.8;
    imageBullet1.background.strokeOpacity = 0;
    imageBullet1.dy = -2;
    imageBullet1.background.pointerBaseWidth = 10;
    imageBullet1.background.pointerLength = 10
    imageBullet1.tooltipText = "{openDateX}\n{text}";

    imageBullet1.background.adapter.add("pointerAngle", (value, target) => {
      if (target.dataItem) {
        let position = dateAxis.valueToPosition(target.dataItem.openDateX.getTime());
        return dateAxis.renderer.positionToAngle(position);
      }
      return value;
    });

    let hs = imageBullet1.states.create("hover")
    hs.properties.scale = 1.3;
    hs.properties.opacity = 1;

  }
</script>

<div class="p-3 h-full w-full">
  <div class="w-full h-full" id="chartdiv" />
</div>
