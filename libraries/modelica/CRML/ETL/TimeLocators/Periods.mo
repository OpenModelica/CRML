within CRML.ETL.TimeLocators;

block Periods "Generates multiple time periods"
        import CRML.ETL.Types.Boolean4;
        import CRML;
        parameter Boolean leftBoundaryIncluded = true "If true, the left boundaries of the time periods are included";
        parameter Boolean rightBoundaryIncluded = true "If true, the right boundaries of the time periods are included";
        parameter Boolean durationSpecified = false "If true, closing events are given by duration on u2, else closing events is given by events on u2" annotation(
          Evaluate = true);
        parameter Boolean discreteClockSpecified = false "If true, duration is given in clock ticks" annotation(
          Evaluate = true,
          Dialog(enable = durationSpecified));
      protected
        Boolean e(start = false, fixed = true) = (time > 0);
        parameter String name = if durationSpecified then "periods" else "periods";
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
        discrete Integer index(start = 0, fixed = true);
        discrete Integer indexMax(start = 0, fixed = true);
        Real[N] tf(each start = -1, each fixed = true);
        Boolean[N] not_y_timePeriod(each start = false, each fixed = true) = not y.timeSlot;
        Boolean u1_(start = false, fixed = true) = (u1 == Boolean4.true4);
        discrete Integer ticks(start = 0, fixed = true);
        outer CRML.TimeLocators.Continuous.Master master;
        Boolean masterPeriod = tl.timePeriod and master.y.timePeriod;
      public
        CRML.ETL.Connectors.TimeLocatorOutput[N] y(timePeriod(each fixed = true, each start = false)) "Vector of time periods" annotation(
          Placement(transformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100}), iconTransformation(extent = {{-10, -10}, {10, 10}}, rotation = -90, origin = {0, -100})));
        Connectors.Boolean4Input u1 "Opening event" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
      public
        Connectors.WhileInput tl "Master time period" annotation(
          Placement(transformation(extent = {{-10, 90}, {10, 110}})));
        Connectors.Boolean4Input u2 "Closing event" annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), visible = not durationSpecified));
        Connectors.RealInput continuousDuration "Time period duration" annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), visible = durationSpecified and not discreteClockSpecified));
        Connectors.IntegerInput discreteDuration "Time period duration in clock ticks" annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), visible = durationSpecified and discreteClockSpecified));
        discrete Connectors.Boolean4Input clock "Clock ticks" annotation(
          Placement(transformation(extent = {{-120, 70}, {-100, 90}}), visible = durationSpecified and discreteClockSpecified));
      equation
        if (cardinality(tl) == 0) then
          tl.timePeriod = true;
          tl.clock = Boolean4.true4;
          tl.isLeftBoundaryIncluded = true;
          tl.isRightBoundaryIncluded = true;
        end if;
        if (cardinality(u2) == 0) then
          u2 = Boolean4.false4;
        end if;
        if (cardinality(continuousDuration) == 0) then
          continuousDuration = -1;
        end if;
        if (cardinality(discreteDuration) == 0) then
          discreteDuration = -1;
        end if;
        if (cardinality(clock) == 0) then
          clock = tl.clock;
        end if;
        indexMax = CRML.ETL.Utilities.firstTrueIndex(y.timePeriod, reverse = true);
        for i in 1:N loop
          y[i].timePeriod = y[i].timeSlot and masterPeriod;
          y[i].timeSlot = if discreteClockSpecified then ((ticks <= tf[i]) or ((i == index) and edge(u1_)) or ((i == index) and u1_ and edge(e))) else ((time < tf[i]) or ((i == index) and edge(u1_)) or ((i == index) and u1_ and edge(e)));
          y[i].clock = clock;
          y[i].isLeftBoundaryIncluded = leftBoundaryIncluded;
          y[i].isRightBoundaryIncluded = rightBoundaryIncluded;
          y[i].indexMax = indexMax;
        end for;
      algorithm
        /* Open a new time period at initial time if u1 is already true */
        when (u1 == Boolean4.true4 and edge(e)) then
          index := CRML.ETL.Utilities.firstTrueIndex(pre(not_y_timePeriod));
          if ((index >= 1) and (index <= N)) then
            tf[index] := if discreteClockSpecified then ticks + (if (discreteDuration >= 0) then discreteDuration else Modelica.Constants.inf) else time + (if (continuousDuration >= 0) then continuousDuration else Modelica.Constants.inf);
          else
            assert(false, "Maximum number N of overlapping time periods is exceeded");
          end if;
        end when;
        /* Open a new time period after each event on u1 */
        when u1 == Boolean4.true4 then
          index := CRML.ETL.Utilities.firstTrueIndex(pre(not_y_timePeriod));
          if ((index >= 1) and (index <= N)) then
            tf[index] := if discreteClockSpecified then ticks + (if (discreteDuration >= 0) then discreteDuration else Modelica.Constants.inf) else time + (if (continuousDuration >= 0) then continuousDuration else Modelica.Constants.inf);
          else
            assert(false, "Maximum number N of overlapping time periods is exceeded");
          end if;
        end when;
        /* Close all time periods at next event on u2 */
        when u2 == Boolean4.true4 then
          if continuousDuration < 0 or discreteDuration < 0 then
            for i in 1:N loop
              tf[i] := time;
            end for;
          end if;
        end when;
        /* Close all time periods when the master period ends */
        when not masterPeriod then
          for i in 1:N loop
            tf[i] := time;
          end for;
        end when;
        /* Count clock ticks */
        when clock == Boolean4.true4 then
          ticks := pre(ticks) + 1;
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {170, 213, 255}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-78, 86}, {76, 38}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = name), Rectangle(extent = {{-70, 30}, {70, -52}}, lineColor = {175, 175, 175}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Line(points = {{-50, -34}, {-50, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-48, -12}, {52, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{52, -12}, {52, -34}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-62, -34}, {58, -34}}, color = {175, 175, 175}), Line(points = {{-50, -12}, {-50, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Rectangle(extent = {{22, 2}, {52, -12}}, lineColor = {0, 0, 0}, fillColor = {175, 175, 175}, fillPattern = FillPattern.Solid), Line(points = {{-50, 10}, {22, 10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, 10}, {22, -12}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {20, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{22, -12}, {24, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, -12}, {-52, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, -12}, {-48, -10}}, color = {0, 0, 0}, pattern = LinePattern.Dash), Line(points = {{-50, 30}, {-70, 30}, {-70, -52}, {-50, -52}}, color = {0, 0, 0}, visible = leftBoundaryIncluded), Line(points = {{50, 30}, {70, 30}, {70, -52}, {50, -52}}, color = {0, 0, 0}, visible = rightBoundaryIncluded), Line(points = {{90, 30}, {70, 30}, {70, -52}, {90, -52}}, color = {0, 0, 0}, visible = not rightBoundaryIncluded), Line(points = {{-90, 30}, {-70, 30}, {-70, -52}, {-90, -52}}, color = {0, 0, 0}, visible = not leftBoundaryIncluded), Text(extent = {{-28, -10}, {22, -34}}, lineColor = {175, 175, 175}, pattern = LinePattern.Dash, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, textString = "%u")}),
          Diagram(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Text(extent = {{-86, 38}, {-72, 22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{-66, -2}, {-52, -18}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{-56, -42}, {-42, -58}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{14, 38}, {28, 22}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{24, -2}, {38, -18}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{54, -42}, {68, -58}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{-6, 38}, {8, 22}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = ""), Text(extent = {{74, 38}, {88, 22}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = ""), Rectangle(extent = {{-80, 20}, {0, 0}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Rectangle(extent = {{20, 20}, {80, 0}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Text(extent = {{-68, 14}, {-14, 6}}, lineColor = {0, 0, 0}, textString = "Time period"), Text(extent = {{22, 14}, {76, 6}}, lineColor = {0, 0, 0}, textString = "Time period"), Line(points = {{-90, 0}, {-80, 0}, {-80, 20}, {0, 20}, {0, 0}, {20, 0}, {20, 20}, {80, 20}, {80, 0}, {96, 0}}, color = {0, 0, 0}, arrow = {Arrow.None, Arrow.Filled}), Rectangle(extent = {{-60, -20}, {0, -40}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Rectangle(extent = {{30, -20}, {80, -40}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Line(points = {{-90, -40}, {-60, -40}, {-60, -20}, {0, -20}, {0, -40}, {30, -40}, {30, -20}, {80, -20}, {80, -40}, {96, -40}}, color = {0, 0, 0}, arrow = {Arrow.None, Arrow.Filled}), Text(extent = {{-56, -26}, {-2, -34}}, lineColor = {0, 0, 0}, textString = "Time period"), Text(extent = {{26, -26}, {80, -34}}, lineColor = {0, 0, 0}, textString = "Time period"), Rectangle(extent = {{-50, -60}, {0, -80}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Rectangle(extent = {{60, -60}, {80, -80}}, lineColor = {0, 0, 0}, fillColor = {215, 215, 215}, fillPattern = FillPattern.Solid, pattern = LinePattern.None), Line(points = {{-90, -80}, {-50, -80}, {-50, -60}, {0, -60}, {0, -80}, {60, -80}, {60, -60}, {80, -60}, {80, -80}, {96, -80}}, color = {0, 0, 0}, arrow = {Arrow.None, Arrow.Filled}), Text(extent = {{-50, -66}, {4, -74}}, lineColor = {0, 0, 0}, textString = "Time period"), Line(points = {{0, 22}, {0, -84}}, color = {0, 0, 0}, pattern = LinePattern.Dot), Line(points = {{80, 24}, {80, -82}}, color = {0, 0, 0}, pattern = LinePattern.Dot), Text(extent = {{106, 96}, {120, 80}}, lineColor = {0, 0, 0}, textString = "", fontName = "Symbol"), Text(extent = {{122, 96}, {170, 84}}, lineColor = {0, 0, 0}, textString = "Opening event"), Text(extent = {{124, 76}, {172, 64}}, lineColor = {0, 0, 0}, textString = "Closing event"), Text(extent = {{108, 76}, {122, 60}}, lineColor = {238, 46, 47}, fontName = "Symbol", textString = ""), Polygon(points = {{-120, 90}, {-120, 70}, {-100, 80}, {-120, 90}}, lineColor = {162, 29, 33}, fillColor = {162, 29, 33}, fillPattern = FillPattern.Solid), Line(points = {{-90, 80}, {92, 80}}, color = {0, 0, 0}, pattern = LinePattern.Dash, arrow = {Arrow.None, Arrow.Filled}), Ellipse(extent = {{-92, 82}, {-88, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-82, 82}, {-78, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-72, 82}, {-68, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-62, 82}, {-58, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-52, 82}, {-48, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-42, 82}, {-38, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-32, 82}, {-28, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-22, 82}, {-18, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-12, 82}, {-8, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{-2, 82}, {2, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{8, 82}, {12, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{18, 82}, {22, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{28, 82}, {32, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{38, 82}, {42, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{48, 82}, {52, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{58, 82}, {62, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{68, 82}, {72, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Ellipse(extent = {{78, 82}, {82, 78}}, lineColor = {0, 0, 0}, fillColor = {0, 0, 0}, fillPattern = FillPattern.Solid), Text(extent = {{-38, 72}, {46, 60}}, lineColor = {0, 0, 0}, textString = "Discrete  time clock (optional)")}),
          Documentation(info = "<html>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Syntax</span></b> </p>
<blockquote><b>y</b> = <b>Periods</b>(<b>u1</b> = opening_event [, <b>u2</b> = closing_event or duration] [, <b>clock</b> = clock] [, <b>tl</b> = master_time_locator]); </blockquote>
<p><b><span style=\"font-family: MS Shell Dlg 2;\">Description</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Each instance of this block creates a <b>Periods</b>. </span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">A time locator is composed of several time periods on the continuous physical clock that may overlap (cf. illustration in the Diagram Layer).</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">A time period is a time interval betwen to events. A time period is opened at each opening event. All opened time periods are closed when a closing event occurs.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">An event occurs when a <a href=\"modelica://CRML.ETL.Types.Boolean4\">Boolean4</a> becomes true.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The opening event occurs when <b>u1</b> becomes true inside the master time period specified by the while block connected at input<b> tl</b>. If no master time period is specified, then the opening event occurs whenever <b>u1</b> becomes true.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">There are two ways of specitying the closing event according to parameter </span><code><b>durationSpecified</b></code><span style=\"font-family: MS Shell Dlg 2;\">:</span></p>
<ul>
<li><span style=\"font-family: MS Shell Dlg 2;\">If </span><span style=\"font-family: Courier New;\">durationSpecified</span><span style=\"font-family: MS Shell Dlg 2;\"> is false, then the closing event occurs when <b>u2</b> becomes true.</span></li>
<li><span style=\"font-family: MS Shell Dlg 2;\">If </span><span style=\"font-family: Courier New;\">durationSpecified</span><span style=\"font-family: MS Shell Dlg 2;\"> is true, then the closing event occurs <i>n</i> seconds (if </span><span style=\"font-family: Courier New;\">discreteClockSpecified</span><span style=\"font-family: MS Shell Dlg 2;\">=false) or <i>n</i> clock ticks (if </span><span style=\"font-family: Courier New;\">discreteClockSpecified</span><span style=\"font-family: MS Shell Dlg 2;\">=true) after <b>u1</b> becomes true, <i>n</i> being the value at input <b>u2</b>.</span></li>
</ul>
<p><br><span style=\"font-family: MS Shell Dlg 2;\">Therefore, the type of <b>u2</b> is <a href=\"modelica://CRML.ETL.Types.Boolean4\">Boolean4</a> if </span><span style=\"font-family: Courier New;\">durationSpecified=</span><span style=\"font-family: MS Shell Dlg 2;\">false, Real if </span><span style=\"font-family: Courier New;\">durationSpecified=</span><span style=\"font-family: MS Shell Dlg 2;\">true and </span><span style=\"font-family: Courier New;\">clockSpecified=</span><span style=\"font-family: MS Shell Dlg 2;\">false, and Integer if </span><span style=\"font-family: Courier New;\">durationSpecified=</span><span style=\"font-family: MS Shell Dlg 2;\">true and </span><span style=\"font-family: Courier New;\">discreteClockSpecified=</span><span style=\"font-family: MS Shell Dlg 2;\">true.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">Additionally, all time periods are closed when the master time period closes.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">If <b>u2</b> is not connected, then there is no closing event from <b>u2</b> (but closing events may come from <b>tl</b> is <b>tl</b> is connected).</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The boundaries of the time periods are open or closed according to parameters </span><code><b>leftBoundaryIncluded</b> and <b>rightBoundaryIncluded.</b></code></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The maximum number of overlapping time periods is set by the parameter<b> </b></span><span style=\"font-family: Courier New;\"><a href=\"modelica://CRML.ETL.Types\">nMaxOverlap</a></span><span style=\"font-family: MS Shell Dlg 2;\">. If the number of overlapping time periods exceeds </span><span style=\"font-family: Courier New;\"><a href=\"modelica://CRML.ETL.Types\">nMaxOverlap</a></span><span style=\"font-family: MS Shell Dlg 2;\">, an error is raised.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">A time period is represented by a Boolean that is true when the time period is open, and false when the time period is closed.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The time periods that constitute the continuous time locator are stored in vector y</span><span style=\"font-family: Courier New;\">.timePeriod[]</span><span style=\"font-family: MS Shell Dlg 2;\">.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The master time period can be created using the <a href=\"modelica://CRML.ETL.TimeLocators.While\">While</a> block.</span></p>
<p><span style=\"font-family: MS Shell Dlg 2;\">The events <b>u1</b> and <b>u2</b> can be generated by converting Boolean signals to <a href=\"modelica://CRML.ETL.Types.Boolean4\">Boolean4</a> signals with the block <a href=\"modelica://CRML.Blocks.Logical4.BooleanToBoolean4\">BooleanToBoolean4</a>.</span></p>
<p><br><b><span style=\"font-family: MS Shell Dlg 2;\">Example</span></b> </p>
<p><span style=\"font-family: MS Shell Dlg 2;\">This block is demonstrated with the following <a href=\"modelica://ReqSysPro.Examples.TimeLocators.Continuous.After\">example</a>:</span></p>
</html>"));
      end Periods;