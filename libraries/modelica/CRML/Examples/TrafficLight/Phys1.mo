within CRML.Examples.TrafficLight;

model Phys1
        Modelica.Electrical.Analog.Basic.Resistor greenLamp(R = 1) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {-30, 60})));
        Modelica.Electrical.Analog.Basic.Resistor yellowLamp(R = 1) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {10, 60})));
        Modelica.Electrical.Analog.Sources.ConstantVoltage mainPowerVoltage(V = 10) annotation(
          Placement(transformation(extent = {{10, -10}, {-10, 10}}, rotation = 90, origin = {-70, 40})));
        Modelica.Electrical.Analog.Basic.Resistor redLamp(R = 1) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {50, 60})));
        Modelica.Electrical.Analog.Ideal.IdealClosingSwitch greenSwitch annotation(
          Placement(transformation(extent = {{10, -10}, {-10, 10}}, rotation = 90, origin = {-30, 20})));
        Modelica.Electrical.Analog.Ideal.IdealClosingSwitch yellowSwitch annotation(
          Placement(transformation(extent = {{10, -10}, {-10, 10}}, rotation = 90, origin = {10, 20})));
        Modelica.Electrical.Analog.Ideal.IdealClosingSwitch redSwitch annotation(
          Placement(transformation(extent = {{10, -10}, {-10, 10}}, rotation = 90, origin = {50, 20})));
        Modelica.Electrical.Analog.Basic.Resistor R(R = 1) annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {80, 40})));
        Modelica.Electrical.Analog.Basic.Ground ground annotation(
          Placement(transformation(extent = {{-80, -30}, {-60, -10}})));
        ETL.Connectors.BooleanInput green annotation(
          Placement(transformation(origin = {12, 68}, extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
        ETL.Connectors.BooleanInput yellow "Boolean condition" annotation(
          Placement(transformation(origin = {12, 26}, extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.BooleanInput red "External Boolean variable" annotation(
          Placement(transformation(origin = {12, -2}, extent = {{-120, 70}, {-100, 90}}), iconTransformation(extent = {{-120, 70}, {-100, 90}})));
        Modelica.Blocks.Sources.BooleanExpression greenCom(y = green) annotation(
          Placement(transformation(extent = {{-54, 10}, {-46, 30}})));
        Modelica.Blocks.Sources.BooleanExpression yellowCom(y = yellow) annotation(
          Placement(transformation(extent = {{-14, 10}, {-6, 30}})));
        Modelica.Blocks.Sources.BooleanExpression redCom(y = red) annotation(
          Placement(transformation(extent = {{26, 10}, {34, 30}})));
      equation
        connect(mainPowerVoltage.p, greenLamp.p) annotation(
          Line(points = {{-70, 50}, {-70, 80}, {-30, 80}, {-30, 70}}, color = {0, 0, 255}));
        connect(greenLamp.p, yellowLamp.p) annotation(
          Line(points = {{-30, 70}, {-30, 80}, {10, 80}, {10, 70}}, color = {0, 0, 255}));
        connect(yellowLamp.p, redLamp.p) annotation(
          Line(points = {{10, 70}, {10, 80}, {50, 80}, {50, 70}}, color = {0, 0, 255}));
        connect(redLamp.p, R.p) annotation(
          Line(points = {{50, 70}, {50, 80}, {80, 80}, {80, 50}}, color = {0, 0, 255}));
        connect(R.n, redSwitch.n) annotation(
          Line(points = {{80, 30}, {80, 0}, {50, 0}, {50, 10}}, color = {0, 0, 255}));
        connect(redSwitch.n, yellowSwitch.n) annotation(
          Line(points = {{50, 10}, {50, 0}, {10, 0}, {10, 10}}, color = {0, 0, 255}));
        connect(yellowSwitch.n, greenSwitch.n) annotation(
          Line(points = {{10, 10}, {10, 0}, {-30, 0}, {-30, 10}}, color = {0, 0, 255}));
        connect(greenSwitch.n, mainPowerVoltage.n) annotation(
          Line(points = {{-30, 10}, {-30, 0}, {-70, 0}, {-70, 30}}, color = {0, 0, 255}));
        connect(mainPowerVoltage.n, ground.p) annotation(
          Line(points = {{-70, 30}, {-70, -10}}, color = {0, 0, 255}));
        connect(greenCom.y, greenSwitch.control) annotation(
          Line(points = {{-45.6, 20}, {-42, 20}}, color = {255, 0, 255}));
        connect(yellowCom.y, yellowSwitch.control) annotation(
          Line(points = {{-5.6, 20}, {-2, 20}}, color = {255, 0, 255}));
        connect(redCom.y, redSwitch.control) annotation(
          Line(points = {{34.4, 20}, {38, 20}}, color = {255, 0, 255}));
        connect(greenLamp.n, greenSwitch.p) annotation(
          Line(points = {{-30, 50}, {-30, 30}}, color = {0, 0, 255}));
        connect(yellowLamp.n, yellowSwitch.p) annotation(
          Line(points = {{10, 50}, {10, 30}}, color = {0, 0, 255}));
        connect(redLamp.n, redSwitch.p) annotation(
          Line(points = {{50, 50}, {50, 30}}, color = {0, 0, 255}));
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {28, 108, 200}, fillColor = {170, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 98}, {20, 58}}, lineColor = {238, 46, 47}, fillColor = {238, 46, 47}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 18}, {20, -20}}, lineColor = {244, 125, 35}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, -60}, {20, -98}}, lineColor = {0, 140, 72}, fillColor = {0, 140, 72}, fillPattern = FillPattern.Solid), Text(extent = {{-92, 126}, {90, 108}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.None, textString = "%name")}),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-140, 100}, {100, -40}})));
      end Phys1;