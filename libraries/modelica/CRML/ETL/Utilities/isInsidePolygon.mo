within CRML.ETL.Utilities;

function isInsidePolygon "Determines whether a point is inside a polygon"
        input Real point[2] "Point";
        input Real polygon[:, 2] "Polygon";
        input Real tolerance = 0 "Tolerance";
        output Real insidePolygon "> 0.5: point is inside polygon. > -0.5 and < 0.5: point is outside polygon: < -0.5: undecided";
      protected
        parameter Real pi = Modelica.Constants.pi;
        parameter Integer N = size(polygon, 1);
        // Number of polygon vertices
        Real int[N];
        // Vector of intersection points on ray
        Integer int_i[N];
        // Vector of indices of the sorted vector
        Real alpha[2*N];
        // Vector of polygon edge and vertex angles
        Real theta[2*N];
        // Vector of consecutive polygon edge and vertex angles
        Integer n;
        // Number of intersection points
        Real x0_R;
        // Ray origin abscissa
        Real y0_R;
        // Ray origin ordinate
        Real a_R;
        // Ray direction abscissa
        Real b_R;
        // Ray direction ordinate
        Real theta_max;
        // Largest polygon angle
        Real alpha_R;
        // Ray angle
        Integer i;
        // Counter
        Integer j;
        // Counter
        Integer k;
        // Counter
        Integer r;
        // Counter
      algorithm
        /* Polygon should have at least 3 vertices */
        assert(N >= 3, "Polygon should have at least 3 vertices");
        /* General line equation is:
      x = x0 + t*a
      y = y0 + t*b
  
      (x, y) are the coordinates of the line points
      t is the curvilinar absicssa of the line points
      
      (x0, y0) is the origin of the line
      (a, b) is the direction of the line
      a = cos(alpha) and b = sin(alpha) where alpha is the edge angle from the x-axis */
        /* Get largest angle of all polygon directions */
        (alpha[1:N], r) := polygonEdgeAngles(polygon, tolerance);
        if (r == -1) then
          insidePolygon := -1;
          return;
        end if;
        (alpha[N + 1:2*N], r) := polygonVertexAngles(polygon, point, tolerance);
        if (r == -1) then
          insidePolygon := -1;
          return;
        end if;
        alpha := sort(alpha);
        for i in 1:2*N loop
          j := if i < 2*N then i + 1 else 1;
          theta[i] := alpha[j] - alpha[i];
        end for;
        (theta_max, j) := vectorMax(theta);
        /* Compute ray equation. The point is ray origin. 
  Ray direction must be different from all edge directions. 
  Ray direction is the bissector of the largest polygon angle */
        k := if j < 2*N then j + 1 else 1;
        alpha_R := (alpha[j] + alpha[k])/2;
        x0_R := point[1];
        y0_R := point[2];
        a_R := cos(alpha_R);
        b_R := sin(alpha_R);
        /* Compute intersections between ray and polygon */
        (int, n) := intersectionsPolygonLine(polygon, x0_R, y0_R, a_R, b_R, tolerance);
        if (n == -1) then
          insidePolygon := -1;
          return;
        end if;
        /* Add point to the vector of intersections */
        n := n + 1;
        int[n] := 0;
        /* Sort the vector of intersections */
        (int, int_i) := sort(int, 1, n);
        /* Count number of intersections on ray before point */
        i := 0;
        while int_i[i + 1] < n loop
          i := i + 1;
        end while;
        /* Point is inside polygon if number of intersections on ray before 
  point is odd */
        insidePolygon := mod(i, 2);
        annotation(
          Diagram(graphics = {Ellipse(extent = {{-24, -6}, {-22, -8}}, lineColor = {28, 108, 200}, fillColor = {28, 108, 200}, fillPattern = FillPattern.Solid), Polygon(points = {{-92, -4}, {-64, 24}, {-60, -14}, {-42, 24}, {-24, -34}, {60, -44}, {2, 8}, {44, 18}, {8, 84}, {-74, 82}, {-92, -4}}, lineColor = {238, 46, 47}, fillColor = {28, 108, 200}, fillPattern = FillPattern.None), Text(extent = {{-56, 80}, {-2, 74}}, lineColor = {238, 46, 47}, textString = "Polygon"), Text(extent = {{-50, -10}, {4, -16}}, lineColor = {28, 108, 200}, textString = "Point")}),
          Documentation(info = "<html>
<h4>Syntax</h4>
<p style=\"margin-left: 30px;\">z = <b>isInsidePolygon </b>(point, polygon, eps); </p>
<h4>Description</h4>
<p>Determines whether <b>point</b> is inside <b>polygon</b>. </p>
<p><b>point</b> is given by its x and y coordinates.</p>
<p><b>polygon</b> is given by a vector of points, each point being given by its x and y coordinates.</p>
<p>If <b>point</b> is inside <b>polygon</b>, then <b>z</b> &gt; + 0.5. If <b>point</b> is outside <b>polygon</b>, then - 0.5 &lt; <b>z</b> &lt; + 0.5. If it cannot be decided whether point is inside or outside polygon, then <b>z</b> &lt; - 0.5.</p>
<p>The result is undecided when <b>point</b> is too close to <b>polygon</b>, the tolerance being given by <b>eps</b>. The default is <b>eps</b> = 1e-4.</p>
<h4>Example</h4>
<p style=\"margin-left: 30px;\">z = isInsidePolygon (<code>{-23,&nbsp;-7}, {{-92,-4},{-64,24},{-60,-14},{-42,24},{-24,-34},{60,-44},{2,8},{44,18},{8,84},{-74,82}});</code></p>
<pre>returns z &gt; 0.5; (cf. graphics in diagram layer)</pre>
</html>"));
      end isInsidePolygon;