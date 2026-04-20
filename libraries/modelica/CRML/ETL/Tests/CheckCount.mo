within CRML.ETL.Tests;
model CheckCount
        TimeLocators.Periods timePeriod annotation(
          Placement(transformation(extent = {{-20, 70}, {0, 90}})));
        CRML.Blocks.Events.EventPeriodic eventPeriodic annotation(
          Placement(transformation(extent = {{-80, 20}, {-60, 40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_1 annotation(
          Placement(transformation(extent = {{-44, 26}, {-36, 34}})));
        CRML.Blocks.Events.EventTable eventTable(option_width = false, instant = {1}) annotation(
          Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
        CRML.Blocks.Events.EventTable eventTable1(option_width = false, instant = {12}) annotation(
          Placement(transformation(extent = {{-80, 50}, {-60, 70}})));
        CRML.Blocks.MathInteger.IntegerConstant integerConstant(K = 11) annotation(
          Placement(transformation(extent = {{-80, -10}, {-60, 10}})));
        Requirements.CheckInteger ensure(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerEqual) annotation(
          Placement(transformation(extent = {{-20, 20}, {0, 40}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_2 annotation(
          Placement(transformation(extent = {{-44, 76}, {-36, 84}})));
        CRML.Blocks.Logical4.BooleanToBoolean4 booleanToBoolean4_3 annotation(
          Placement(transformation(extent = {{-44, 68}, {-36, 76}})));
        Requirements.CheckInteger ensure1(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerGreaterEqual) annotation(
          Placement(transformation(extent = {{-20, -20}, {0, 0}})));
        Requirements.CheckInteger ensure2(redeclare model Function = ETL.Requirements.Functions.MathInteger.EventCounter, redeclare model Condition = ETL.Requirements.Conditions.MathInteger.IntegerLowerEqual) annotation(
          Placement(transformation(extent = {{-20, -60}, {0, -40}})));
        CRML.Blocks.Logical4.bAnd4 and4
                                       annotation(
          Placement(transformation(extent = {{40, -40}, {60, -20}})));
        CRML.Blocks.Events.ShowEvent showEvent annotation(
          Placement(transformation(extent = {{-34, 46}, {-26, 54}})));
equation
        connect(eventPeriodic.y, booleanToBoolean4_1.u) annotation(
          Line(points = {{-59, 30}, {-44.4, 30}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_1.y, ensure.u) annotation(
          Line(points = {{-35.6, 30}, {-21, 30}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure.tl) annotation(
          Line(points = {{-10, 70}, {-10, 40}}, color = {0, 0, 255}));
        connect(integerConstant.y, ensure.threshold) annotation(
          Line(points = {{-59, 0}, {-40, 0}, {-40, 22}, {-21, 22}}, color = {255, 127, 0}));
        connect(eventTable.y, booleanToBoolean4_2.u) annotation(
          Line(points = {{-59, 90}, {-50, 90}, {-50, 80}, {-44.4, 80}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_2.y, timePeriod.u1) annotation(
          Line(points = {{-35.6, 80}, {-21, 80}}, color = {162, 29, 33}));
        connect(eventTable1.y, booleanToBoolean4_3.u) annotation(
          Line(points = {{-59, 60}, {-50, 60}, {-50, 72}, {-44.4, 72}}, color = {217, 67, 180}));
        connect(booleanToBoolean4_3.y, timePeriod.u2) annotation(
          Line(points = {{-35.6, 72}, {-21, 72}}, color = {162, 29, 33}));
        connect(integerConstant.y, ensure1.threshold) annotation(
          Line(points = {{-59, 0}, {-40, 0}, {-40, -18}, {-21, -18}}, color = {255, 127, 0}));
        connect(integerConstant.y, ensure2.threshold) annotation(
          Line(points = {{-59, 0}, {-40, 0}, {-40, -58}, {-21, -58}}, color = {255, 127, 0}));
        connect(booleanToBoolean4_1.y, ensure1.u) annotation(
          Line(points = {{-35.6, 30}, {-30, 30}, {-30, -10}, {-21, -10}}, color = {162, 29, 33}));
        connect(booleanToBoolean4_1.y, ensure2.u) annotation(
          Line(points = {{-35.6, 30}, {-30, 30}, {-30, -50}, {-21, -50}}, color = {162, 29, 33}));
        connect(timePeriod.y, ensure1.tl) annotation(
          Line(points = {{-10, 70}, {-10, 50}, {10, 50}, {10, 10}, {-10, 10}, {-10, 0}}, color = {0, 0, 255}));
        connect(timePeriod.y, ensure2.tl) annotation(
          Line(points = {{-10, 70}, {-10, 50}, {10, 50}, {10, -30}, {-10, -30}, {-10, -40}}, color = {0, 0, 255}));
        connect(ensure1.y, and4.u1) annotation(
          Line(points = {{1, -10}, {20, -10}, {20, -22}, {39, -22}}, color = {162, 29, 33}));
        connect(ensure2.y, and4.u2) annotation(
          Line(points = {{1, -50}, {20, -50}, {20, -38}, {39, -38}}, color = {162, 29, 33}));
        connect(eventPeriodic.y, showEvent.u) annotation(
          Line(points = {{-59, 30}, {-50, 30}, {-50, 50}, {-34.4, 50}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(lineColor = {0, 0, 255}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-36, 60}, {64, 0}, {-36, -60}, {-36, 60}})}),
          Diagram(coordinateSystem(preserveAspectRatio = false)),
          experiment(StopTime = 20));
end CheckCount;
