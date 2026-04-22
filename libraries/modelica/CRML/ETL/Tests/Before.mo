within CRML.ETL.Tests;

model Before
        import CRML;
        CRML.Blocks.Events.EventPeriodic start(startTime = 2, period = 11) annotation(
          Placement(transformation(extent = {{-100, 60}, {-80, 80}})));
        CRML.Blocks.Events.ShowEvent showEventStart annotation(
          Placement(transformation(extent = {{-54, 86}, {-46, 94}})));
        CRML.Blocks.Events.Before after annotation(
          Placement(transformation(extent = {{-10, 20}, {10, 40}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic1(startTime = 1) annotation(
          Placement(transformation(extent = {{-100, -20}, {-80, 0}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-54, -34}, {-46, -26}})));
        CRML.Blocks.Events.Before before annotation(
          Placement(transformation(extent = {{-10, -60}, {10, -40}})));
        CRML.Blocks.Events.EventPeriodic finish(period = 11, startTime = 10) annotation(
          Placement(transformation(extent = {{-100, -100}, {-80, -80}})));
        CRML.Blocks.Events.ShowEvent showEventFinish annotation(
          Placement(transformation(extent = {{-12, -94}, {-4, -86}})));
        Modelica.Blocks.Logical.And and1 annotation(
          Placement(transformation(extent = {{26, -6}, {34, 2}})));
        CRML.Blocks.Events.ShowEvent showEventReset annotation(
          Placement(transformation(extent = {{28, -74}, {36, -66}})));
        CRML.Blocks.Events.EventFilter eventFilter annotation(
          Placement(transformation(extent = {{40, -20}, {60, 0}})));
        CRML.Blocks.Events.ShowEvent showEventFilter annotation(
          Placement(transformation(extent = {{76, -44}, {84, -36}})));
        CRML.Blocks.Events.EventCounter eventCounter annotation(
          Placement(transformation(extent = {{80, -20}, {100, 0}})));
        CRML.ETL.TimeLocators.Periods timePeriod(leftBoundaryIncluded = true, rightBoundaryIncluded = false) annotation(
          Placement(transformation(extent = {{40, 70}, {60, 90}})));
        CRML.ETL.TimeLocators.Attributes.IsLeftBoundaryIncluded isLeftBoundaryIncluded annotation(
          Placement(transformation(extent = {{-54, 26}, {-46, 34}})));
        Modelica.Blocks.Logical.Not not1 annotation(
          Placement(transformation(extent = {{-34, 26}, {-26, 34}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{6, 76}, {14, 84}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{6, 68}, {14, 76}})));
        CRML.ETL.TimeLocators.Attributes.IsRightBoundaryIncluded isRightBoundaryIncluded annotation(
          Placement(transformation(extent = {{-54, -54}, {-46, -46}})));
        Modelica.Blocks.Logical.Not not2 annotation(
          Placement(transformation(extent = {{-34, -54}, {-26, -46}})));
        CRML.ETL.TimeLocators.Attributes.PeriodStart periodStart annotation(
          Placement(transformation(extent = {{-84, 34}, {-76, 42}})));
        CRML.ETL.TimeLocators.Attributes.PeriodEnd periodEnd annotation(
          Placement(transformation(extent = {{-84, -62}, {-76, -54}})));
      equation
        connect(start.y, showEventStart.u) annotation(
          Line(points = {{-79, 70}, {-60, 70}, {-60, 90}, {-54.4, 90}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, showEvent.u) annotation(
          Line(points = {{-79, -10}, {-70, -10}, {-70, -30}, {-54.4, -30}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, after.u2) annotation(
          Line(points = {{-79, -10}, {-20, -10}, {-20, 22}, {-11, 22}}, color = {217, 67, 180}));
        connect(start.y, after.reset) annotation(
          Line(points = {{-79, 70}, {-40, 70}, {-40, 10}, {0, 10}, {0, 19}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, before.u1) annotation(
          Line(points = {{-79, -10}, {-20, -10}, {-20, -42}, {-11, -42}}, color = {217, 67, 180}));
        connect(finish.y, showEventFinish.u) annotation(
          Line(points = {{-79, -90}, {-12.4, -90}}, color = {217, 67, 180}));
        connect(after.y, and1.u1) annotation(
          Line(points = {{11, 30}, {20, 30}, {20, -2}, {25.2, -2}}, color = {217, 67, 180}));
        connect(before.y, and1.u2) annotation(
          Line(points = {{11, -50}, {20, -50}, {20, -5.2}, {25.2, -5.2}}, color = {217, 67, 180}));
        connect(start.y, before.reset) annotation(
          Line(points = {{-79, 70}, {-40, 70}, {-40, -70}, {0, -70}, {0, -61}}, color = {217, 67, 180}));
        connect(start.y, showEventReset.u) annotation(
          Line(points = {{-79, 70}, {-40, 70}, {-40, -70}, {27.6, -70}}, color = {217, 67, 180}));
        connect(and1.y, eventFilter.cond) annotation(
          Line(points = {{34.4, -2}, {39, -2}}, color = {255, 0, 255}));
        connect(eventPeriodic1.y, eventFilter.u) annotation(
          Line(points = {{-79, -10}, {39, -10}}, color = {217, 67, 180}));
        connect(eventFilter.y, showEventFilter.u) annotation(
          Line(points = {{61, -10}, {68, -10}, {68, -40}, {75.6, -40}}, color = {217, 67, 180}));
        connect(eventFilter.y, eventCounter.u) annotation(
          Line(points = {{61, -10}, {79, -10}}, color = {217, 67, 180}));
        connect(start.y, eventCounter.reset) annotation(
          Line(points = {{-79, 70}, {-40, 70}, {-40, -80}, {90, -80}, {90, -21}}, color = {217, 67, 180}));
        connect(timePeriod.y[1], isLeftBoundaryIncluded.tl) annotation(
          Line(points = {{50, 70}, {50, 50}, {-50, 50}, {-50, 34}}, color = {0, 0, 255}));
        connect(start.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-79, 70}, {-40, 70}, {-40, 80}, {5.6, 80}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, timePeriod.u1) annotation(
          Line(points = {{14.4, 80}, {39, 80}}, color = {162, 29, 33}));
        connect(finish.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-79, -90}, {-60, -90}, {-60, 60}, {0, 60}, {0, 72}, {5.6, 72}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u2) annotation(
          Line(points = {{14.4, 72}, {39, 72}}, color = {162, 29, 33}));
        connect(isLeftBoundaryIncluded.y, not1.u) annotation(
          Line(points = {{-45.6, 30}, {-34.8, 30}}, color = {217, 67, 180}));
        connect(not1.y, after.strictlyBefore) annotation(
          Line(points = {{-25.6, 30}, {-11, 30}}, color = {255, 0, 255}));
        connect(isRightBoundaryIncluded.y, not2.u) annotation(
          Line(points = {{-45.6, -50}, {-34.8, -50}}, color = {217, 67, 180}));
        connect(not2.y, before.strictlyBefore) annotation(
          Line(points = {{-25.6, -50}, {-11, -50}}, color = {255, 0, 255}));
        connect(timePeriod.y[1], isRightBoundaryIncluded.tl) annotation(
          Line(points = {{50, 70}, {50, 50}, {-66, 50}, {-66, -40}, {-50, -40}, {-50, -46}}, color = {0, 0, 255}));
        connect(timePeriod.y[1], periodStart.tl) annotation(
          Line(points = {{50, 70}, {50, 50}, {-80, 50}, {-80, 42}}, color = {0, 0, 255}));
        connect(periodStart.y, after.u1) annotation(
          Line(points = {{-75.6, 38}, {-11, 38}}, color = {217, 67, 180}));
        connect(timePeriod.y[1], periodEnd.tl) annotation(
          Line(points = {{50, 70}, {50, 50}, {-66, 50}, {-66, -40}, {-80, -40}, {-80, -54}}, color = {0, 0, 255}));
        connect(periodEnd.y, before.u2) annotation(
          Line(points = {{-75.6, -58}, {-11, -58}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 22));
      end Before;