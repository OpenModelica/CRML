within CRML.Examples.TrafficLight;

model Verif
        Spec spec annotation(
          Placement(transformation(extent = {{-92, 26}, {-130, 54}})));
        Modelica.Blocks.Sources.RealExpression greenObserver(y = phys.greenLamp.i) annotation(
          Placement(transformation(extent = {{56, 10}, {30, 30}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold oGreen(threshold = 9) annotation(
          Placement(transformation(extent = {{12, 16}, {4, 24}})));
        Modelica.Blocks.Sources.RealExpression yellowObservor(y = phys.yellowLamp.i) annotation(
          Placement(transformation(extent = {{56, 30}, {30, 50}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold oYellow(threshold = 9) annotation(
          Placement(transformation(extent = {{12, 36}, {4, 44}})));
        Modelica.Blocks.Sources.RealExpression redObserver(y = phys.redLamp.i) annotation(
          Placement(transformation(extent = {{56, 50}, {30, 70}})));
        Log2 log annotation(
          Placement(transformation(extent = {{148, 30}, {128, 50}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold oRed(threshold = 9) annotation(
          Placement(transformation(extent = {{12, 56}, {4, 64}})));
        Phys1 phys annotation(
          Placement(transformation(extent = {{108, 30}, {88, 50}})));
        Modelica.Blocks.Sources.BooleanExpression green_active(y = oGreen.y) annotation(
          Placement(transformation(extent = {{-30, 10}, {-50, 30}})));
        Modelica.Blocks.Sources.BooleanExpression yellow_active(y = oYellow.y) annotation(
          Placement(transformation(extent = {{-30, 30}, {-50, 50}})));
        Modelica.Blocks.Sources.BooleanExpression red_active(y = oRed.y) annotation(
          Placement(transformation(extent = {{-30, 50}, {-50, 70}})));
      equation
        connect(spec.green, green_active.y) annotation(
          Line(points = {{-90.5385, 28.8}, {-80, 28.8}, {-80, 20}, {-51, 20}}, color = {217, 67, 180}));
        connect(spec.yellow, yellow_active.y) annotation(
          Line(points = {{-90.5385, 40}, {-51, 40}}, color = {217, 67, 180}));
        connect(spec.red, red_active.y) annotation(
          Line(points = {{-90.5385, 51.2}, {-80, 51.2}, {-80, 60}, {-51, 60}}, color = {217, 67, 180}));
        connect(greenObserver.y, oGreen.u) annotation(
          Line(points = {{28.7, 20}, {12.8, 20}}, color = {0, 0, 127}));
        connect(yellowObservor.y, oYellow.u) annotation(
          Line(points = {{28.7, 40}, {12.8, 40}}, color = {0, 0, 127}));
        connect(redObserver.y, oRed.u) annotation(
          Line(points = {{28.7, 60}, {12.8, 60}}, color = {0, 0, 127}));
        connect(phys.green, log.y_green) annotation(
          Line(points = {{109, 32}, {114, 32}, {114, 32}, {118, 32}, {118, 32}, {127, 32}}, color = {217, 67, 180}));
        connect(phys.yellow, log.y_yellow) annotation(
          Line(points = {{109, 40}, {127, 40}}, color = {217, 67, 180}));
        connect(phys.red, log.y_red) annotation(
          Line(points = {{109, 48}, {114, 48}, {114, 48}, {118, 48}, {118, 48}, {127, 48}}, color = {217, 67, 180}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-160, -40}, {180, 100}}), graphics = {Rectangle(extent = {{-160, 100}, {180, -40}}, lineColor = {28, 108, 200}), Line(points = {{-40, 28}, {0, -30}, {62, 96}}, color = {0, 127, 0}, smooth = Smooth.None, thickness = 1)}),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-160, -40}, {180, 100}}), graphics = {Rectangle(extent = {{-150, 90}, {172, -30}}, lineColor = {28, 108, 200}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid), Rectangle(extent = {{80, 80}, {158, 0}}, lineColor = {28, 108, 200}, fillColor = {170, 255, 213}, fillPattern = FillPattern.Solid), Text(extent = {{84, 14}, {156, 4}}, lineColor = {28, 108, 200}, textString = "Behavioral model"), Rectangle(extent = {{-4, 80}, {60, 0}}, lineColor = {28, 108, 200}, fillColor = {170, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-10, 12}, {64, 4}}, lineColor = {28, 108, 200}, textString = "Observation model"), Text(extent = {{116, 64}, {156, 56}}, lineColor = {28, 108, 200}, fillColor = {255, 213, 170}, fillPattern = FillPattern.Solid, textString = "I&C"), Text(extent = {{80, 64}, {120, 56}}, lineColor = {28, 108, 200}, fillColor = {255, 213, 170}, fillPattern = FillPattern.Solid, textString = "Physical"), Rectangle(extent = {{-54, 80}, {-26, 0}}, lineColor = {28, 108, 200}, fillColor = {255, 213, 170}, fillPattern = FillPattern.Solid), Text(extent = {{-76, 12}, {-4, 4}}, lineColor = {28, 108, 200}, textString = "Bindings"), Rectangle(extent = {{-140, 80}, {-72, 0}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 170}, fillPattern = FillPattern.Solid), Text(extent = {{-142, 12}, {-68, 4}}, lineColor = {28, 108, 200}, textString = "Requirements model"), Text(extent = {{-66, -8}, {78, -26}}, lineColor = {28, 108, 200}, fillColor = {255, 170, 255}, fillPattern = FillPattern.Solid, textString = "Verification model")}),
          experiment(StopTime = 100));
      end Verif;