within CRML.ETL.Tests;

model TimeLocator
        import CRML;
        CRML.Blocks.Events.EventTable clock1(option_width = false, instant = {1, 3, 6, 9, 11}) annotation(
          Placement(transformation(extent = {{-100, 60}, {-80, 80}})));
        CRML.Blocks.Events.ShowEvent showClock1 annotation(
          Placement(transformation(extent = {{-64, 76}, {-56, 84}})));
        CRML.Blocks.Events.EventProjection eventProjection annotation(
          Placement(transformation(extent = {{-40, 40}, {-20, 60}})));
        CRML.Blocks.Events.EventTable event1(option_width = false, instant = {0.5, 4, 5, 7, 10}) annotation(
          Placement(transformation(extent = {{-100, 30}, {-80, 50}})));
        CRML.Blocks.Events.ShowEvent showEvent1 annotation(
          Placement(transformation(extent = {{-64, 46}, {-56, 54}})));
        CRML.Blocks.Events.ShowEvent showEvent1ProjClock1 annotation(
          Placement(transformation(extent = {{44, 46}, {52, 54}})));
        TimeLocators.Periods fromFor
          annotation (Placement(transformation(extent={{40,0},{60,20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{16, 6}, {24, 14}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{16, -2}, {24, 6}})));
        TimeLocators.Periods fromFor_nticks(durationSpecified=true,
            discreteClockSpecified=false)
          annotation (Placement(transformation(extent={{40,-40},{60,-20}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_4 annotation(
          Placement(transformation(extent = {{16, -34}, {24, -26}})));
        CRML.Blocks.Events.EventTable event2(option_width = false, instant = {5, 7, 10, 12, 15}) annotation(
          Placement(transformation(extent = {{-100, -10}, {-80, 10}})));
        CRML.Blocks.Math.Constant duration(k=5)
          annotation (Placement(transformation(extent={{-40,-60},{-20,-40}})));
        CRML.Blocks.Events.ShowEvent showEvent2 annotation(
          Placement(transformation(extent = {{-54, 4}, {-46, 12}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic(period = 0.5) annotation(
          Placement(transformation(extent = {{-40, -30}, {-20, -10}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{16, -26}, {24, -18}})));
        CRML.TimeLocators.Attributes.PeriodsEnd timeLocatorStartingEvents annotation(
          Placement(transformation(extent = {{66, -56}, {74, -48}})));
        CRML.Blocks.Events.ShowEvent showEvent4 annotation(
          Placement(transformation(extent = {{86, -56}, {94, -48}})));
      equation
        connect(clock1.y, showClock1.u) annotation(
          Line(points = {{-79, 70}, {-70, 70}, {-70, 80}, {-64.4, 80}}, color = {217, 67, 180}));
        connect(event1.y, showEvent1.u) annotation(
          Line(points = {{-79, 40}, {-70, 40}, {-70, 50}, {-64.4, 50}}, color = {217, 67, 180}));
        connect(event1.y, eventProjection.u) annotation(
          Line(points = {{-79, 40}, {-50, 40}, {-50, 50}, {-41, 50}}, color = {217, 67, 180}));
        connect(clock1.y, eventProjection.clock) annotation(
          Line(points = {{-79, 70}, {-50, 70}, {-50, 58}, {-41, 58}}, color = {217, 67, 180}));
        connect(eventProjection.y, showEvent1ProjClock1.u) annotation(
          Line(points = {{-19, 50}, {43.6, 50}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, fromFor.u1)
          annotation (Line(points={{24.4,10},{39,10}}, color={162,29,33}));
        connect(booleanToBoolean4_1.y, fromFor.u2)
          annotation (Line(points={{24.4,2},{39,2}}, color={162,29,33}));
        connect(booleanToBoolean4_4.y, fromFor_nticks.u1)
          annotation (Line(points={{24.4,-30},{39,-30}}, color={162,29,33}));
        connect(event2.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-79, 0}, {8, 0}, {8, 2}, {15.6, 2}}, color = {217, 67, 180}));
        connect(eventProjection.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-19, 50}, {0, 50}, {0, 10}, {15.6, 10}}, color = {217, 67, 180}));
        connect(event2.y, showEvent2.u) annotation(
          Line(points = {{-79, 0}, {-60, 0}, {-60, 8}, {-54.4, 8}}, color = {217, 67, 180}));
        connect(eventProjection.y, booleanToBoolean4_4.u) annotation(
          Line(points = {{-19, 50}, {0, 50}, {0, -30}, {15.6, -30}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, fromFor_nticks.clock)
          annotation (Line(points={{24.4,-22},{39,-22}}, color={162,29,33}));
        connect(eventPeriodic.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-19, -20}, {8, -20}, {8, -22}, {15.6, -22}}, color = {217, 67, 180}));
        connect(fromFor_nticks.y, timeLocatorStartingEvents.tl) annotation (
            Line(points={{50,-40},{50,-44},{70,-44},{70,-48}}, color={0,0,255}));
        connect(timeLocatorStartingEvents.y, showEvent4.u) annotation(
          Line(points = {{74.4, -52}, {80, -52}, {80, -52}, {85.6, -52}}, color = {217, 67, 180}));
        connect(duration.y, fromFor_nticks.continuousDuration) annotation (Line(
              points={{-19,-50},{34,-50},{34,-38},{39,-38}}, color={0,0,0}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 20));
      end TimeLocator;