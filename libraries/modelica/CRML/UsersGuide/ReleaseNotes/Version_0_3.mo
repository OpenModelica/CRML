within CRML.UsersGuide.ReleaseNotes;

class Version_0_3 "Version 0.3"
        annotation(
          DocumentationClass = true,
          Documentation(info = "<html>
<p><b><span style=\"font-size: 10pt; color: #008000;\">Version 0.3 (in progress, 2024)</span></b></p>
<p>This is the third beta release of the library. </p>
<p>Minor changes:</p>
<ul>
<li>Updated package &quot;Blocks.Events&quot;: declaration of x as public (and not protected) for setting start value</li>
<li>Updated package &quot;Examples&quot;: deletion of dependency with the Modelica_StateGraph2 library in &quot;traffic light&quot; example</li>
<li>New package &quot;Units&quot;: deletion of dependency with the Modelica Standard Library for units declaration</li>
</ul>
<p>Major changes:</p>
<ul>
<li>Updated package &quot;ETL.Requirements&quot;: addition of &quot;DecideOver&quot;, &quot;EvaluateOver&quot; and &quot;CheckOver&quot; ETL custom operators</li>
<li>New package &quot;ETL.Blocks&quot;: definition of ETL custom operators on Clocks and Events</li>
<li>Under progress</li>
</ul>
</html>"),Icon(graphics = {Ellipse(lineColor = {75, 138, 73}, fillColor = {75, 138, 73}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{-100, -100}, {100, 100}}), Polygon(origin = {-10.167, -15}, fillColor = {255, 255, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, points = {{-15.833, 20.0}, {-15.833, 30.0}, {14.167, 40.0}, {24.167, 20.0}, {4.167, -30.0}, {14.167, -30.0}, {24.167, -30.0}, {24.167, -40.0}, {-5.833, -50.0}, {-15.833, -30.0}, {4.167, 20.0}, {-5.833, 20.0}}, smooth = Smooth.Bezier), Ellipse(origin = {1.5, 56.5}, fillColor = {255, 255, 255}, pattern = LinePattern.None, fillPattern = FillPattern.Solid, extent = {{-12.5, -12.5}, {12.5, 12.5}})}));
      end Version_0_3;