within CRML.ETL.Tests;

model EnsureAnytime
        import CRML;
        TimeLocators.Periods timePeriod annotation(
          Placement(transformation(extent = {{-40, 30}, {-20, 50}})));
        CRML.Blocks.Logical.BooleanStep eventPeriodic(startTime = 5) annotation(
          Placement(transformation(extent = {{-100, -50}, {-80, -30}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-64, -44}, {-56, -36}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false) annotation(
          Placement(transformation(extent = {{-100, 40}, {-80, 60}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {10}) annotation(
          Placement(transformation(extent = {{-100, 10}, {-80, 30}})));
        CRML.ETL.Requirements.CheckAtEnd ensure annotation(
          Placement(transformation(extent = {{-40, -50}, {-20, -30}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-64, 36}, {-56, 44}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-64, 28}, {-56, 36}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse(width = 6, period = 20, startTime = 2) annotation(
          Placement(transformation(extent = {{0, 80}, {20, 100}})));
        CRML.Blocks.Events.TimeFrom timeFrom annotation(
          Placement(transformation(extent = {{-76, 84}, {-64, 96}})));
        inner CRML.TimeLocators.Continuous.Master master annotation(
          Placement(transformation(extent = {{60, 80}, {80, 100}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_7 annotation(
          Placement(transformation(extent = {{36, 86}, {44, 94}})));
        CRML.Requirements.CheckAnytime checkAnytime annotation(
          Placement(transformation(extent = {{60, -80}, {80, -60}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{-34, -74}, {-26, -66}})));
        CRML.Blocks.Logical.BooleanPulse booleanPulse1 annotation(
          Placement(transformation(extent = {{-100, -80}, {-80, -60}})));
        CRML.TimeLocators.Continuous.During during annotation(
          Placement(transformation(extent = {{60, -20}, {80, 0}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_5 annotation(
          Placement(transformation(extent = {{36, -14}, {44, -6}})));
        CRML.Blocks.Logical.BooleanStep eventPeriodic1(startTime = 0.001) annotation(
          Placement(transformation(extent = {{0, -20}, {20, 0}})));
        CRML.ETL.Requirements.CheckAnytime check annotation(
          Placement(transformation(extent = {{0, -60}, {20, -40}})));
      equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-79, -40}, {-64.4, -40}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-55.6, -40}, {-41, -40}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{-30, 30}, {-30, -30}}, color = {0, 0, 255}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-79, 50}, {-70, 50}, {-70, 40}, {-64.4, 40}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-55.6, 40}, {-41, 40}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-79, 20}, {-70, 20}, {-70, 32}, {-64.4, 32}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{-55.6, 32}, {-41, 32}}, color = {162, 29, 33}));
        connect(booleanPulse.y, booleanToBoolean4_7.u) annotation(
          Line(points = {{21, 90}, {35.6, 90}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_7.y, master.u) annotation(
          Line(points = {{44.4, 90}, {59, 90}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_4.y, checkAnytime.u) annotation(
          Line(points = {{-25.6, -70}, {59, -70}}, color = {162, 29, 33}));
        connect(booleanPulse1.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-79, -70}, {-34.4, -70}}, color = {217, 67, 180}));
        connect(during.y, checkAnytime.tl) annotation(
          Line(points = {{70, -20}, {70, -60}}, color = {0, 0, 255}));
        connect(booleanToBoolean4_5.y, during.u) annotation(
          Line(points = {{44.4, -10}, {59, -10}}, color = {162, 29, 33}));
        connect(eventPeriodic1.y, booleanToBoolean4_5.u) annotation(
          Line(points = {{21, -10}, {35.6, -10}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_4.y, check.u) annotation(
          Line(points = {{-25.6, -70}, {-10, -70}, {-10, -50}, {-1, -50}}, color = {162, 29, 33}));
        connect(during.y, check.tl) annotation(
          Line(points = {{70, -20}, {70, -28}, {10, -28}, {10, -40}}, color = {0, 0, 255}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 14));
      end EnsureAnytime;