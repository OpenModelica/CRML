within CRML.Examples.TrafficLight;

model Log1
        block Red
          annotation(
            Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Text(origin = {-4, 0}, extent = {{-100, 100}, {100, -100}}, textString = "%name"), Rectangle(lineColor = {0, 0, 0}, borderPattern = BorderPattern.Raised, extent = {{-100, 100}, {100, -100}}, radius = 25)}),
            Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, textString = "%stateName", fontSize = 10)}),
            showDiagram = true,
            singleInstance = true);
        end Red;

        block Yellow
          annotation(
            Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-100, 100}, {100, -100}}, textString = "%name"), Rectangle(lineColor = {0, 0, 0}, borderPattern = BorderPattern.Raised, extent = {{-100, 100}, {100, -100}}, radius = 25)}),
            Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, textString = "%stateName", fontSize = 10)}),
            showDiagram = true,
            singleInstance = true);
        end Yellow;

        block Green
          annotation(
            Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-100, 100}, {100, -100}}, textString = "%name"), Rectangle(lineColor = {0, 0, 0}, borderPattern = BorderPattern.Raised, extent = {{-100, 100}, {100, -100}}, radius = 25)}),
            Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Text(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 0}, textString = "%stateName", fontSize = 10)}),
            showDiagram = true,
            singleInstance = true);
        end Green;

        Green green annotation(
          Placement(transformation(origin = {20, -136}, extent = {{-70, 50}, {-50, 70}})));
        Yellow yellow annotation(
          Placement(transformation(origin = {2, -62}, extent = {{-12, 50}, {12, 70}})));
        Red red annotation(
          Placement(transformation(origin = {-94, 14}, extent = {{50, 50}, {70, 70}})));
        Modelica.Blocks.Sources.BooleanExpression green_active(y = hold(activeState(green))) annotation(
          Placement(transformation(extent = {{40, -90}, {80, -70}})));
        Modelica.Blocks.Sources.BooleanExpression yellow_active(y = hold(activeState(yellow))) annotation(
          Placement(transformation(extent = {{40, -10}, {80, 10}})));
        Modelica.Blocks.Sources.BooleanExpression red_active(y = hold(activeState(red))) annotation(
          Placement(transformation(extent = {{40, 70}, {80, 90}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_green "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, -90}, {120, -70}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_yellow "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_red "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, 70}, {120, 90}})));
      equation
        transition(green, yellow, timeInState() > 30, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{-48, 60}, {-14, 60}}, color = {0, 0, 0}, thickness = 0.25, smooth = Smooth.Bezier),
          Text(string = "%condition", extent = {{-4, 4}, {-4, 10}}, fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Right));
        transition(yellow, red, timeInState() > 4, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{14, 60}, {48, 60}}, color = {0, 0, 0}, thickness = 0.25, smooth = Smooth.Bezier),
          Text(textString = "%condition", extent = {{-4, 4}, {-4, 10}}, fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Right));
        transition(red, green, timeInState() > 30, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{60, 48}, {60, 34}, {60, 20}, {-60, 20}, {-60, 48}}, color = {0, 0, 0}, thickness = 0.25, smooth = Smooth.Bezier),
          Text(textString = "%condition", extent = {{4, -4}, {4, -10}}, fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Left));
        connect(green_active.y, y_green) annotation(
          Line(points = {{82, -80}, {110, -80}}, color = {255, 0, 255}));
        connect(yellow_active.y, y_yellow) annotation(
          Line(points = {{82, 0}, {110, 0}}, color = {255, 0, 255}));
        connect(red_active.y, y_red) annotation(
          Line(points = {{82, 80}, {110, 80}}, color = {255, 0, 255}));
        transition(red, green, false, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{-44, 67}, {-71, 35}, {-43, -66}}, smooth = Smooth.Bezier),
          Text(extent = {{4, -4}, {4, -10}}, textString = "%condition", fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Left));
        initialState(green) annotation(
          Line(points = {{-50, -78}, {-65, -84}, {-78, -62}}, smooth = Smooth.Bezier));
        transition(green, yellow, false, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{-40, -66}, {-22, -66}, {-22, -2}, {-10, -2}}, smooth = Smooth.Bezier),
          Text(extent = {{-4, 4}, {-4, 10}}, textString = "%condition", fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Right));
        transition(yellow, red, false, immediate = false, reset = true, synchronize = false, priority = 1) annotation(
          Line(points = {{14, -2}, {31, -2}, {31, 74}, {-24, 74}}, smooth = Smooth.Bezier),
          Text(extent = {{-4, 4}, {-4, 10}}, textString = "%condition", fontSize = 10, textStyle = {TextStyle.Bold}, horizontalAlignment = TextAlignment.Right));
        annotation(
          Icon(coordinateSystem(extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 98}, {20, 58}}, lineColor = {238, 46, 47}, fillColor = {238, 46, 47}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 18}, {20, -20}}, lineColor = {244, 125, 35}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, -60}, {20, -98}}, lineColor = {0, 140, 72}, fillColor = {0, 140, 72}, fillPattern = FillPattern.Solid), Text(extent = {{-92, 126}, {90, 108}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.None, textString = "%name")}),
          Diagram(coordinateSystem(extent = {{-100, -100}, {100, 100}})),
          experiment(StopTime = 100, __Dymola_Algorithm = "Dassl"));
      end Log1;