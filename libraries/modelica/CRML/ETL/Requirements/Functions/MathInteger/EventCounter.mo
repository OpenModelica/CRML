within CRML.ETL.Requirements.Functions.MathInteger;

block EventCounter "Event counter"
            extends MathInteger.IntegerFunction(name = "Event counter");
            import CRML.ETL.Types.FunctionType;
            CRML.Blocks.Events.Event4ToEvent event4ToEvent annotation(
              Placement(transformation(extent = {{-94, -4}, {-86, 4}})));
            ETL.TimeLocators.Attributes.PeriodStart timePeriodStartingEvents annotation(
              Placement(transformation(extent = {{-92, 34}, {-84, 42}})));
            ETL.TimeLocators.Attributes.PeriodEnd timePeriodClosingEvents annotation(
              Placement(transformation(extent = {{-92, -42}, {-84, -34}})));
            CRML.Blocks.Events.Before before1 annotation(
              Placement(transformation(extent = {{-40, 20}, {-20, 40}})));
            CRML.Blocks.Events.Before before2 annotation(
              Placement(transformation(extent = {{-40, -40}, {-20, -20}})));
            Modelica.Blocks.Logical.And and1 annotation(
              Placement(transformation(extent = {{-4, 4}, {4, 12}})));
            ETL.TimeLocators.Attributes.IsLeftBoundaryIncluded timePeriodIsLeftBoundaryIncluded annotation(
              Placement(transformation(extent = {{-64, 26}, {-56, 34}})));
            ETL.TimeLocators.Attributes.IsRightBoundaryIncluded timePeriodIsRightBoundaryIncluded annotation(
              Placement(transformation(extent = {{-64, -34}, {-56, -26}})));
            Modelica.Blocks.Logical.Not not1 annotation(
              Placement(transformation(extent = {{-50, 28}, {-46, 32}})));
            Modelica.Blocks.Logical.Not not2 annotation(
              Placement(transformation(extent = {{-50, -32}, {-46, -28}})));
            CRML.Blocks.Events.EventCounter eventCounter annotation(
              Placement(transformation(extent = {{60, -10}, {80, 10}})));
            CRML.Blocks.Events.EventFilter eventFilter annotation(
              Placement(transformation(extent = {{20, -10}, {40, 10}})));
          equation
            ft = FunctionType.monotonicIncreasing;
            connect(u, event4ToEvent.u) annotation(
              Line(points = {{-110, 0}, {-94.4, 0}}, color = {162, 29, 33}));
            connect(tl, timePeriodStartingEvents.tl) annotation(
              Line(points = {{0, 100}, {0, 50}, {-88, 50}, {-88, 42}}, color = {0, 0, 255}));
            connect(tl, timePeriodClosingEvents.tl) annotation(
              Line(points = {{0, 100}, {0, 76}, {0, 76}, {0, 50}, {-80, 50}, {-80, -12}, {-88, -12}, {-88, -34}}, color = {0, 0, 255}));
            connect(timePeriodStartingEvents.y, before1.u1) annotation(
              Line(points = {{-83.6, 38}, {-41, 38}}, color = {217, 67, 180}));
            connect(event4ToEvent.y, before1.u2) annotation(
              Line(points = {{-85.6, 0}, {-50, 0}, {-50, 22}, {-41, 22}}, color = {217, 67, 180}));
            connect(event4ToEvent.y, before2.u1) annotation(
              Line(points = {{-85.6, 0}, {-50, 0}, {-50, -22}, {-41, -22}}, color = {217, 67, 180}));
            connect(timePeriodClosingEvents.y, before2.u2) annotation(
              Line(points = {{-83.6, -38}, {-41, -38}}, color = {217, 67, 180}));
            connect(before1.y, and1.u1) annotation(
              Line(points = {{-19, 30}, {-10, 30}, {-10, 8}, {-4.8, 8}}, color = {217, 67, 180}));
            connect(before2.y, and1.u2) annotation(
              Line(points = {{-19, -30}, {-10, -30}, {-10, 4.8}, {-4.8, 4.8}}, color = {217, 67, 180}));
            connect(timePeriodStartingEvents.y, before1.reset) annotation(
              Line(points = {{-83.6, 38}, {-70, 38}, {-70, 10}, {-30, 10}, {-30, 19}}, color = {217, 67, 180}));
            connect(timePeriodStartingEvents.y, before2.reset) annotation(
              Line(points = {{-83.6, 38}, {-70, 38}, {-70, -50}, {-30, -50}, {-30, -41}}, color = {217, 67, 180}));
            connect(tl, timePeriodIsLeftBoundaryIncluded.tl) annotation(
              Line(points = {{0, 100}, {0, 50}, {-60, 50}, {-60, 34}}, color = {0, 0, 255}));
            connect(tl, timePeriodIsRightBoundaryIncluded.tl) annotation(
              Line(points = {{0, 100}, {0, 50}, {-80, 50}, {-80, -12}, {-60, -12}, {-60, -26}}, color = {0, 0, 255}));
            connect(timePeriodIsLeftBoundaryIncluded.y, not1.u) annotation(
              Line(points = {{-55.6, 30}, {-50.4, 30}}, color = {217, 67, 180}));
            connect(not1.y, before1.strictlyBefore) annotation(
              Line(points = {{-45.8, 30}, {-41, 30}}, color = {255, 0, 255}));
            connect(timePeriodIsRightBoundaryIncluded.y, not2.u) annotation(
              Line(points = {{-55.6, -30}, {-50.4, -30}}, color = {217, 67, 180}));
            connect(not2.y, before2.strictlyBefore) annotation(
              Line(points = {{-45.8, -30}, {-41, -30}}, color = {255, 0, 255}));
            connect(eventFilter.y, eventCounter.u) annotation(
              Line(points = {{41, 0}, {59, 0}}, color = {217, 67, 180}));
            connect(timePeriodStartingEvents.y, eventCounter.reset) annotation(
              Line(points = {{-83.6, 38}, {-70, 38}, {-70, -50}, {70, -50}, {70, -11}}, color = {217, 67, 180}));
            connect(and1.y, eventFilter.cond) annotation(
              Line(points = {{4.4, 8}, {19, 8}}, color = {255, 0, 255}));
            connect(event4ToEvent.y, eventFilter.u) annotation(
              Line(points = {{-85.6, 0}, {19, 0}}, color = {217, 67, 180}));
            connect(eventCounter.y, y) annotation(
              Line(points = {{81, 0}, {110, 0}}, color = {255, 127, 0}));
            annotation(
              Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, -100}, {100, 100}}, lineColor = {255, 127, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-94, 0}, {66, 0}}, color = {238, 46, 47}), Line(points = {{-74, 26}, {-74, 0}}, color = {238, 46, 47}), Line(points = {{-34, 26}, {-34, 0}}, color = {238, 46, 47}), Line(points = {{26, 26}, {26, 0}}, color = {238, 46, 47}), Line(points = {{56, 26}, {56, 0}}, color = {238, 46, 47}), Line(points = {{-94, -90}, {-74, -90}, {-74, -70}, {-34, -70}, {-34, -50}, {26, -50}, {26, -30}, {56, -30}, {56, -10.0195}, {76, -10}}, color = {244, 125, 35})}),
              Diagram(coordinateSystem(preserveAspectRatio = false)),
              Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>EventCounter</b>(<b>u</b> = condition, <b>tl</b> = time_period); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p>This blocks computes the number of event occurrences on the Boolean4 condition <b>u </b>over time period <b>tl </b>(i.e. the total number of occurrences when <b>u</b> becomes true over <b>tl</b>).</p>
<p><br><b><span style=\"font-family: MS Shell Dlg 2;\">Example</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block is demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.TimeLocators.Continuous.After\">example</a>:</span></p>
</html>"));
          end EventCounter;