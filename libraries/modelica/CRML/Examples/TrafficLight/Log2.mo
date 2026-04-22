within CRML.Examples.TrafficLight;

model Log2
        Modelica.StateGraph.StepWithSignal green(nIn = 2, nOut = 1) annotation(
          Placement(transformation(extent = {{-70, 20}, {-50, 40}})));
        Modelica.StateGraph.TransitionWithSignal T1 annotation(
          Placement(transformation(extent = {{-30, 20}, {-10, 40}})));
        Modelica.StateGraph.StepWithSignal yellow(nOut = 1, nIn = 1) annotation(
          Placement(transformation(extent = {{10, 20}, {30, 40}})));
        Modelica.StateGraph.StepWithSignal red(nOut = 1, nIn = 1) annotation(
          Placement(transformation(extent = {{30, -40}, {10, -20}})));
        Modelica.StateGraph.TransitionWithSignal T3 annotation(
          Placement(transformation(extent = {{-10, -40}, {-30, -20}})));
        Modelica.Blocks.Logical.Timer timer2 annotation(
          Placement(transformation(extent = {{26, -4}, {34, 4}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold greaterEqual2(threshold = 4) annotation(
          Placement(transformation(extent = {{46, -4}, {54, 4}})));
        Modelica.StateGraph.TransitionWithSignal T2 annotation(
          Placement(transformation(extent = {{50, 20}, {70, 40}})));
        Modelica.Blocks.Logical.Timer timer1 annotation(
          Placement(transformation(extent = {{-54, -4}, {-46, 4}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold greaterEqual1(threshold = 30) annotation(
          Placement(transformation(extent = {{-34, -4}, {-26, 4}})));
        Modelica.Blocks.Logical.Timer timer3 annotation(
          Placement(transformation(extent = {{14, -64}, {6, -56}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold greaterEqual3(threshold = 30) annotation(
          Placement(transformation(extent = {{-6, -64}, {-14, -56}})));
        Modelica.Blocks.Sources.BooleanExpression green_active(y = green.active) annotation(
          Placement(transformation(extent = {{60, -90}, {80, -70}})));
        Modelica.Blocks.Sources.BooleanExpression yellow_active(y = yellow.active) annotation(
          Placement(transformation(extent = {{72, -10}, {92, 10}})));
        Modelica.Blocks.Sources.BooleanExpression red_active(y = red.active) annotation(
          Placement(transformation(extent = {{60, 70}, {80, 90}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_green "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, -90}, {120, -70}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_yellow "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        Modelica.Blocks.Interfaces.BooleanOutput y_red "Value of Boolean output" annotation(
          Placement(transformation(extent = {{100, 70}, {120, 90}})));
        Modelica.StateGraph.InitialStepWithSignal initialStep(nIn = 0, nOut = 1) annotation(
          Placement(transformation(extent = {{-80, 80}, {-60, 100}})));
        Modelica.StateGraph.TransitionWithSignal T0 annotation(
          Placement(transformation(extent = {{-30, 80}, {-10, 100}})));
        Modelica.Blocks.Logical.Timer timer0 annotation(
          Placement(transformation(extent = {{-54, 56}, {-46, 64}})));
        Modelica.Blocks.Logical.GreaterEqualThreshold greaterEqual0(threshold = 1) annotation(
          Placement(transformation(extent = {{-34, 56}, {-26, 64}})));
        inner Modelica.StateGraph.StateGraphRoot stateGraphRoot annotation(
          Placement(transformation(extent = {{-80, -80}, {-60, -60}})));
      equation
        connect(T3.outPort, green.inPort[1]) annotation(
          Line(points = {{-21.5, -30}, {-80, -30}, {-80, 29.75}, {-71, 29.75}}, color = {0, 0, 0}));
        connect(timer2.y, greaterEqual2.u) annotation(
          Line(points = {{34.4, 0}, {45.2, 0}}, color = {0, 0, 127}));
        connect(yellow.active, timer2.u) annotation(
          Line(points = {{20, 19}, {20, 0}, {25.2, 0}}, color = {255, 0, 255}));
        connect(greaterEqual2.y, T2.condition) annotation(
          Line(points = {{54.4, 0}, {60, 0}, {60, 18}}, color = {255, 0, 255}));
        connect(timer1.y, greaterEqual1.u) annotation(
          Line(points = {{-45.6, 0}, {-34.8, 0}}, color = {0, 0, 127}));
        connect(green.active, timer1.u) annotation(
          Line(points = {{-60, 19}, {-60, 0}, {-54.8, 0}}, color = {255, 0, 255}));
        connect(greaterEqual1.y, T1.condition) annotation(
          Line(points = {{-25.6, 0}, {-20, 0}, {-20, 18}}, color = {255, 0, 255}));
        connect(red.active, timer3.u) annotation(
          Line(points = {{20, -41}, {20, -60}, {14.8, -60}}, color = {255, 0, 255}));
        connect(timer3.y, greaterEqual3.u) annotation(
          Line(points = {{5.6, -60}, {-5.2, -60}}, color = {0, 0, 127}));
        connect(greaterEqual3.y, T3.condition) annotation(
          Line(points = {{-14.4, -60}, {-20, -60}, {-20, -42}}, color = {255, 0, 255}));
        connect(green_active.y, y_green) annotation(
          Line(points = {{81, -80}, {110, -80}}, color = {255, 0, 255}));
        connect(yellow_active.y, y_yellow) annotation(
          Line(points = {{93, 0}, {110, 0}}, color = {255, 0, 255}));
        connect(red_active.y, y_red) annotation(
          Line(points = {{81, 80}, {110, 80}}, color = {255, 0, 255}));
        connect(T0.outPort, green.inPort[2]) annotation(
          Line(points = {{-18.5, 90}, {-8, 90}, {-8, 48}, {-80, 48}, {-80, 30.25}, {-71, 30.25}}, color = {0, 0, 0}));
        connect(timer0.y, greaterEqual0.u) annotation(
          Line(points = {{-45.6, 60}, {-34.8, 60}}, color = {0, 0, 127}));
        connect(greaterEqual0.y, T0.condition) annotation(
          Line(points = {{-25.6, 60}, {-20, 60}, {-20, 78}}, color = {255, 0, 255}));
        connect(initialStep.active, timer0.u) annotation(
          Line(points = {{-70, 79}, {-70, 60}, {-54.8, 60}}, color = {255, 0, 255}));
        connect(green.outPort[1], T1.inPort) annotation(
          Line(points = {{-49.5, 30}, {-24, 30}}, color = {0, 0, 0}));
        connect(red.outPort[1], T3.inPort) annotation(
          Line(points = {{9.5, -30}, {-16, -30}}, color = {0, 0, 0}));
        connect(yellow.outPort[1], T2.inPort) annotation(
          Line(points = {{30.5, 30}, {56, 30}}, color = {0, 0, 0}));
        connect(T1.outPort, yellow.inPort[1]) annotation(
          Line(points = {{-18.5, 30}, {9, 30}}, color = {0, 0, 0}));
        connect(T2.outPort, red.inPort[1]) annotation(
          Line(points = {{61.5, 30}, {64, 30}, {64, -30}, {31, -30}}, color = {0, 0, 0}));
        connect(initialStep.outPort[1], T0.inPort) annotation(
          Line(points = {{-59.5, 90}, {-24, 90}}, color = {0, 0, 0}));
        annotation(
          Icon(graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 98}, {20, 58}}, lineColor = {238, 46, 47}, fillColor = {238, 46, 47}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, 18}, {20, -20}}, lineColor = {244, 125, 35}, fillColor = {244, 125, 35}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-20, -60}, {20, -98}}, lineColor = {0, 140, 72}, fillColor = {0, 140, 72}, fillPattern = FillPattern.Solid), Text(extent = {{-92, 126}, {90, 108}}, lineColor = {28, 108, 200}, fillColor = {255, 255, 255}, fillPattern = FillPattern.None, textString = "%name")}));
      end Log2;