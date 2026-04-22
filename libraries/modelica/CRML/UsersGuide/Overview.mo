within CRML.UsersGuide;

class Overview "Overview of CRML"
      annotation(
        Documentation(info = "<html>
<p>CRML(Common Requirement Modeling Language) is a language for the simulation of temporal requirements on cyber-physical systems.</p>
<p>This library is a Modelica implementation of CRML. It requires the use of the Modelica language version 3.3 or later to support the notion of clock. </p>
<p>For more information, please consult the following references:</p>
<ul>
<li><span style=\"font-family: Arial;\">Bouskela D., Nguyen T. and Jardin A. (2017), &ldquo;Toward a Rigorous Approach for Verifying Cyber-Physical Systems Against Requirements,&rdquo; Canadian J. of Electrical and Computer Engineering, Vol. 40-2, pp. 66-73. </span></li>
<li><span style=\"font-family: Arial;\">Bouskela D., Buffoni L., Jardin A., Molnar V., Pop A. and Zavada A. (2023), &quot;The Common Requirement Modeling Language&quot;, Proceedings of the 15th International Modelica Conference 2023, Aachen, October 9-11.</span></li>
</ul>
<p>Regarding the RandomFailure block in package Blocks.Events, please consult the following reference:</p>
<ul>
<li><span style=\"font-family: Arial;\">Bouissou M. and Buffoni L. (2020), &ldquo;Generic Method to Transform a Modelica Simulation into a Dynamic Reliability Model,&rdquo; IMdR, Institut pour la ma&icirc;trise des Risques, 22e Congr&egrave;s de Ma&icirc;trise des Risques et S&ucirc;ret&eacute; de Fonctionnement. </span></li>
</ul>
</html>"),
        Icon(graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(origin = {-10.167, -15}, fillColor = {255, 255, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-15.833, 20.0}, {-15.833, 30.0}, {14.167, 40.0}, {24.167, 20.0}, {4.167, -30.0}, {14.167, -30.0}, {24.167, -30.0}, {24.167, -40.0}, {-5.833, -50.0}, {-15.833, -30.0}, {4.167, 20.0}, {-5.833, 20.0}}, smooth = Smooth.Bezier), Ellipse(origin = {1.5, 56.5}, fillColor = {255, 255, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{-12.5, -12.5}, {12.5, 12.5}})}));
    end Overview;