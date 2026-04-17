within CRML.ETL.Tests;

model Delay
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1, 3, 6, 9, 11}) annotation(
          Placement(transformation(extent = {{-100, 40}, {-80, 60}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 5) annotation(
          Placement(transformation(extent = {{0, -100}, {20, -80}})));
        CRML.Blocks.Math.Constant const(k = 10) annotation(
          Placement(transformation(extent = {{-100, -20}, {-80, 0}})));
        CRML.Blocks.Events.EventDelay eventDelay annotation(
          Placement(transformation(extent = {{-50, 40}, {-30, 60}})));
        CRML.Blocks.Math.Constant const1(k = 5) annotation(
          Placement(transformation(extent = {{-100, 10}, {-80, 30}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-64, 56}, {-56, 64}})));
        CRML.Blocks.Events.ShowEvent showEvent1 annotation(
          Placement(transformation(extent = {{-14, 26}, {-6, 34}})));
        CRML.Blocks.Logical.BooleanTable booleanTable(option_width = false, instant = {1, 1.2, 3, 3.2, 6, 6.2, 9, 9.2, 11, 11.2}) annotation(
          Placement(transformation(extent = {{-100, -50}, {-80, -30}})));
        CRML.Blocks.Logical.BooleanDelay booleanDelay annotation(
          Placement(transformation(extent = {{-48, -50}, {-28, -30}})));
        CRML.Blocks.Logical.BooleanClockedDelay booleanClockedDelay annotation(
          Placement(transformation(extent = {{60, -80}, {80, -60}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic(period = 1) annotation(
          Placement(transformation(extent = {{0, -60}, {20, -40}})));
        CRML.Blocks.Events.ShowEvent showEvent2 annotation(
          Placement(transformation(extent = {{36, -44}, {44, -36}})));
        CRML.Blocks.Events.EventClockedDelay eventClockedDelay annotation(
          Placement(transformation(extent = {{60, 0}, {80, 20}})));
        CRML.Blocks.Events.ShowEvent showEvent3 annotation(
          Placement(transformation(extent = {{68, 36}, {76, 44}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic1(period = 1.05) annotation(
          Placement(transformation(extent = {{20, 20}, {40, 40}})));
        CRML.Blocks.Events.ShowEvent showEvent4 annotation(
          Placement(transformation(extent = {{88, 6}, {96, 14}})));
      equation
        connect(const1.y, eventDelay.delay) annotation(
          Line(points = {{-79, 20}, {-60, 20}, {-60, 42}, {-51, 42}}, color = {0, 0, 0}));
        connect(eventTable.y, showEvent.u) annotation(
          Line(points = {{-79, 50}, {-70, 50}, {-70, 60}, {-64.4, 60}}, color = {217, 67, 180}));
        connect(eventDelay.y, showEvent1.u) annotation(
          Line(points = {{-29, 50}, {-24, 50}, {-24, 30}, {-14.4, 30}}, color = {217, 67, 180}));
        connect(eventTable.y, eventDelay.u) annotation(
          Line(points = {{-79, 50}, {-51, 50}}, color = {217, 67, 180}));
        connect(booleanTable.y, booleanDelay.u) annotation(
          Line(points = {{-79, -40}, {-49, -40}}, color = {217, 67, 180}));
        connect(const1.y, booleanDelay.delay) annotation(
          Line(points = {{-79, 20}, {-60, 20}, {-60, -48}, {-49, -48}}, color = {0, 0, 0}));
        connect(integerConstant.y, booleanClockedDelay.delay) annotation(
          Line(points = {{21, -90}, {30, -90}, {30, -78}, {59, -78}}, color = {255, 127, 0}));
        connect(booleanTable.y, booleanClockedDelay.u) annotation(
          Line(points = {{-79, -40}, {-70, -40}, {-70, -70}, {59, -70}}, color = {217, 67, 180}));
        connect(eventPeriodic.y, booleanClockedDelay.clock) annotation(
          Line(points = {{21, -50}, {30, -50}, {30, -62}, {59, -62}}, color = {217, 67, 180}));
        connect(eventPeriodic.y, showEvent2.u) annotation(
          Line(points = {{21, -50}, {30, -50}, {30, -40}, {35.6, -40}}, color = {217, 67, 180}));
        connect(integerConstant.y, eventClockedDelay.delay) annotation(
          Line(points = {{21, -90}, {48, -90}, {48, 2}, {59, 2}}, color = {255, 127, 0}));
        connect(eventTable.y, eventClockedDelay.u) annotation(
          Line(points = {{-79, 50}, {-70, 50}, {-70, 10}, {59, 10}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, eventClockedDelay.clock) annotation(
          Line(points = {{41, 30}, {50, 30}, {50, 18}, {59, 18}}, color = {217, 67, 180}));
        connect(eventPeriodic1.y, showEvent3.u) annotation(
          Line(points = {{41, 30}, {50, 30}, {50, 40}, {67.6, 40}}, color = {217, 67, 180}));
        connect(eventClockedDelay.y, showEvent4.u) annotation(
          Line(points = {{81, 10}, {87.6, 10}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 20));
      end Delay;