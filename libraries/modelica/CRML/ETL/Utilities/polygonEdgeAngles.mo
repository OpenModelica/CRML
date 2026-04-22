within CRML.ETL.Utilities;

function polygonEdgeAngles "Returns the vector of polygon edge angles"
        input Real polygon[:, 2] "Polygon";
        input Real tolerance = 0 "Tolerance";
        output Real alpha[size(polygon, 1)] "Vector of polygon edge angles";
        output Integer n "Number of polygon angles. If -1, the result is undecided";
      protected
        parameter Real pi = Modelica.Constants.pi;
        Real p0[2];
        // Edge origin
        Real p1[2];
        // Edge end
        Real x0;
        // Edge origin abscissa
        Real y0;
        // Edge origin ordinate
        Real x1;
        // Edge end abscissa
        Real y1;
        // Edge end ordinate
        Real a;
        // Edge direction abscissa
        Real b;
        // Edge direction ordinate
        Real d;
        // Edge length
        Integer i;
        // Counter
        Integer j;
        // Counter
      algorithm
        /* Polygon should have at least 3 vertices */
        assert(size(polygon, 1) >= 3, "Polygon should have at least 3 vertices");
        /* General line equation is:
      x = x0 + t*a
      y = y0 + t*b
  
      (x, y) are the coordinates of the line points
      t is the curvilinar absicssa of the line points
      
      (x0, y0) is the origin of the line
      (a, b) is the direction of the line
      a = cos(alpha) and b = sin(alpha) where alpha is the edge angle from the x-axis */
        /* Compute angles of all edges. An edge is given by two consecutive 
  polygon vertices. The consecutive point of the last point is the first point*/
        for i in 1:size(polygon, 1) loop
          /* Edge origin */
          j := i;
          p0 := polygon[j, :];
          x0 := p0[1];
          y0 := p0[2];
          /* Edge end */
          j := if j < size(polygon, 1) then j + 1 else 1;
          p1 := polygon[j, :];
          x1 := p1[1];
          y1 := p1[2];
          /* Edge angle */
          a := x1 - x0;
          b := y1 - y0;
          d := sqrt(a^2 + b^2);
          if d < tolerance then
            n := -1;
            return;
          end if;
          alpha[i] := if b >= 0 then acos(a/d) else pi - acos(a/d);
        end for;
        n := size(polygon, 1);
      end polygonEdgeAngles;