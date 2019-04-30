<%--
  Created by IntelliJ IDEA.
  User: pc
  Date: 25/04/2019
  Time: 16:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <title>Title</title>
</head>
<body>


<style>
    #chartdiv {
        width: 100%;
        height: 500px;
    }
</style>


<script src="../plugins/amcharts4/core.js"></script>
<script src="../plugins/amcharts4/charts.js"></script>
<script src="../plugins/amcharts4/themes/spiritedaway.js"></script>
<script src="../plugins/amcharts4/themes/animated.js"></script>

<!-- Chart code -->
<script>
    am4core.ready(function() {

// Themes begin
        am4core.useTheme(am4themes_spiritedaway);
        am4core.useTheme(am4themes_animated);
// Themes end

// create chart
        var chart = am4core.create("chartdiv", am4charts.TreeMap);
        chart.hiddenState.properties.opacity = 0; // this makes initial fade in effect

        chart.data =${subjects}
/*
        chart.data = [{
            name: "First",
            children: [
                {
                    name: "A1",
                    value: 100
                },
                {
                    name: "A2",
                    value: 60
                },
                {
                    name: "A3",
                    value: 30
                },
                {
                    name: "A4",
                    value: 10
                }
            ]
        }, {
            name: "Second",
            children: [
                {
                        name: "B1",
                        value: 135
                },
                {
                        name: "B2",
                        value: 98
                },
                {
                        name: "B3",
                        value: 56
                }
                ]
            },
            {
                name: "Third",
                children: [
                    {
                        name: "C1",
                        value: 335
                    },
                    {
                        name: "C2",
                        value: 148
                    },
                    {
                        name: "C3",
                        value: 126
                    },
                    {
                        name: "C4",
                        value: 26
                    }
                ]
            },
            {
                name: "Fourth",
                children: [
                    {
                        name: "D1",
                        value: 415
                    },
                    {
                        name: "D2",
                        value: 148
                    },
                    {
                        name: "D3",
                        value: 89
                    },
                    {
                        name: "D4",
                        value: 64
                    },
                    {
                        name: "D5",
                        value: 16
                    }
                ]
            },
            {
                name: "Fifth",
                children: [
                    {
                        name: "E1",
                      //  value: 687
                        value: 17
                    },
                    {
                        name: "E2",
                        value: 148
                    }
                ]
            }];
*/
        chart.colors.step = 2;

// define data fields
        chart.dataFields.value = "value";
        chart.dataFields.name = "name";
        chart.dataFields.children = "children";

        chart.zoomable = false;
        var bgColor = new am4core.InterfaceColorSet().getFor("background");

// level 0 series template
        var level0SeriesTemplate = chart.seriesTemplates.create("0");
        var level0ColumnTemplate = level0SeriesTemplate.columns.template;

        level0ColumnTemplate.column.cornerRadius(10, 10, 10, 10);
        level0ColumnTemplate.fillOpacity = 0;
        level0ColumnTemplate.strokeWidth = 4;
        level0ColumnTemplate.strokeOpacity = 0;

// level 1 series template
        var level1SeriesTemplate = chart.seriesTemplates.create("1");
        var level1ColumnTemplate = level1SeriesTemplate.columns.template;

        level1SeriesTemplate.tooltip.animationDuration = 0;
        level1SeriesTemplate.strokeOpacity = 1;

        level1ColumnTemplate.column.cornerRadius(10, 10, 10, 10)
        level1ColumnTemplate.fillOpacity = 1;
        level1ColumnTemplate.strokeWidth = 4;
        level1ColumnTemplate.stroke = bgColor;

        var bullet1 = level1SeriesTemplate.bullets.push(new am4charts.LabelBullet());
        bullet1.locationY = 0.5;
        bullet1.locationX = 0.5;
        bullet1.label.text = "{name}";
        bullet1.label.fill = am4core.color("#ffffff");

        chart.maxLevels = 3;

    }); // end am4core.ready()
</script>



<!-- HTML -->
<div id="chartdiv"></div>

<!--<script src="../dist/js/pages/beforeGraphs.js"></script>-->
</body>
</html>