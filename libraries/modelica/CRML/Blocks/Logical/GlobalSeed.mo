within CRML.Blocks.Logical;

block GlobalSeed "Generates a global seed for the RandomFailure block"
        parameter Boolean useAutomaticSeed = true "true: global seed is automatically generated - false: global seed is set by user (cf. parameter fixedSeed)";
        parameter Integer fixedSeed = 67867967 "Fixed seed (active if useAutomaticSeed = false)" annotation(
          Dialog(enable = not useAutomaticSeed));
        final parameter Integer seed(fixed = false) "Global seed";
      initial equation
        seed = if not useAutomaticSeed then Modelica.Math.Random.Utilities.initializeImpureRandom(fixedSeed) else Modelica.Math.Random.Utilities.initializeImpureRandom(Modelica.Math.Random.Utilities.automaticGlobalSeed());
        //         Line(visible = not enableNoise,
        //           points={{-80,-4},{84,-4}},
        //           color={215,215,215})
        //         Line(visible = not enableNoise,
        //           points={{-80,-4},{84,-4}},
        //           color={215,215,215})
        annotation(
          defaultComponentName = "globalSeed",
          defaultComponentPrefixes = "inner",
          missingInnerMessage = "
Your model is using an outer \"globalSeed\" component but
an inner \"globalSeed\" component is not defined and therefore
a default inner \"globalSeed\" component is introduced by the tool.
To change the default setting, drag CRML.Blocks.Logical.GlobalSeed
into your model and specify the seed.
          ",
          Icon(coordinateSystem(preserveAspectRatio = false, extent = {{-100, -100}, {100, 100}}), graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 127}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-150, 150}, {150, 110}}, textString = "%name", lineColor = {0, 0, 255}), Line(visible = true, points = {{-73, -15}, {-59, -15}, {-59, 1}, {-51, 1}, {-51, -47}, {-43, -47}, {-43, -25}, {-35, -25}, {-35, 59}, {-27, 59}, {-27, 27}, {-27, 27}, {-27, -33}, {-17, -33}, {-17, -15}, {-7, -15}, {-7, -43}, {3, -43}, {3, 39}, {9, 39}, {9, 53}, {15, 53}, {15, -3}, {25, -3}, {25, 9}, {31, 9}, {31, -21}, {41, -21}, {41, 51}, {51, 51}, {51, 17}, {59, 17}, {59, -49}, {69, -49}}, color = {215, 215, 215}), Text(visible = true and not useAutomaticSeed, extent = {{-92, 10}, {86, -16}}, lineColor = {255, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%fixedSeed")}),
          Documentation(revisions = "<html>
</html>", info = "<html>
<h4>Adapted from the Modelica.Blocks.Noise library </h4>
<h4>Version 1.0 </h4>
<p>When using <a href=\"modelica://Modelica.Blocks.Noise\">t</a>he RandomFailure block on the same or a higher hierarchical level, GlobalSeed must be dragged resulting in a declaration </p>
<p><code><b>inner</b> CRML.Blocks.Events.GlobalSeed globalSeed;</code> </p>
<p>The GlobalSeed block provides global options for all RandomFailure blocks of the same or a lower hierarchical level. The following options can be selected: </p>
<table cellspacing=\"0\" cellpadding=\"2\" border=\"1\"><tr>
<td><p align=\"center\"><h4>Icon</h4></p></td>
<td><p align=\"center\"><h4>Description</h4></p></td>
</tr>
<tr>
<td><p><img src=\"modelica://Modelica/Resources/Images/Blocks/Noise/GlobalSeed_FixedSeed.png\"/> </p></td>
<td><p><b>useAutomaticSeed=false</b> (= default):</p><p>A fixed global seed is defined with Integer parameter fixedSeed. The value of fixedSeed is displayed in the icon. By default all Noise blocks use fixedSeed for initialization of their pseudo random number generators, in combination with a local seed defined for every instance separately. Therefore, whenever a simulation is performed with the same fixedSeed exactly the same noise is generated in all instances of the Noise blocks (provided the settings of these blocks are not changed as well).</p><p>This option can be used (a) to design a control system (e.g. by parameter optimization) and keep the same noise for all simulations, or (b) perform Monte Carlo Simulations where fixedSeed is changed from the environment for every simulation, in order to produce different noise at every simulation run.</p></td>
</tr>
<tr>
<td><p><img src=\"modelica://Modelica/Resources/Images/Blocks/Noise/GlobalSeed_AutomaticSeed.png\"/> </p></td>
<td><p><b>useAutomaticSeed=true</b>:</p><p>An automatic global seed is computed by using the ID of the process in which the simulation takes place and the current local time. As a result, the global seed is changed automatically for every new simulation, including parallelized simulation runs. This option can be used to perform Monte Carlo Simulations with minimal effort (just performing many simulation runs) where every simulation run uses a different noise.</p></td>
</tr>
</table>
</html>"),Diagram(graphics = {Ellipse(extent = {{-100, 100}, {100, -100}}, lineColor = {0, 0, 127}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid), Text(extent = {{-150, 150}, {150, 110}}, textString = "%name", lineColor = {0, 0, 255}), Line(visible = true, points = {{-73, -15}, {-59, -15}, {-59, 1}, {-51, 1}, {-51, -47}, {-43, -47}, {-43, -25}, {-35, -25}, {-35, 59}, {-27, 59}, {-27, 27}, {-27, 27}, {-27, -33}, {-17, -33}, {-17, -15}, {-7, -15}, {-7, -43}, {3, -43}, {3, 39}, {9, 39}, {9, 53}, {15, 53}, {15, -3}, {25, -3}, {25, 9}, {31, 9}, {31, -21}, {41, -21}, {41, 51}, {51, 51}, {51, 17}, {59, 17}, {59, -49}, {69, -49}}, color = {215, 215, 215}), Text(visible = true and not useAutomaticSeed, extent = {{-92, 10}, {86, -16}}, lineColor = {255, 0, 0}, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%fixedSeed")}));
      end GlobalSeed;