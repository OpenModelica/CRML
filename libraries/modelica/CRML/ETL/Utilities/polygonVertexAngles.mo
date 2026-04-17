within CRML.ETL.Utilities;

function polygonVertexAngles "Returns the vector of polygon vertex angles from a given point"
        input Real polygon[:, 2] "Polygon";
        input Real point[2] "Point";
        input Real tolerance = 0 "Tolerance";
        output Real alpha[size(polygon, 1)] "Vector of polygon vertex angles";
        output Integer n "Number of polygon vertices. If -1, the result is undecided";
      protected
        parameter Real pi = Modelica.Constants.pi;
        Real p0[2];
        // Vertex origin
        Real p1[2];
        // Vertex end
        Real x0;
        // Vertex origin abscissa
        Real y0;
        // Vertex origin ordinate
        Real x1;
        // Vertex end abscissa
        Real y1;
        // Vertex end ordinate
        Real a;
        // Vertex direction abscissa
        Real b;
        // Vertex direction ordinate
        Real d;
        // Vertex length
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
        /* Compute angles of all vertices from given point */
        p0 := point;
        x0 := p0[1];
        y0 := p0[2];
        for i in 1:size(polygon, 1) loop
          p1 := polygon[i, :];
          x1 := p1[1];
          y1 := p1[2];
          /* Vertex angle */
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
      end polygonVertexAngles;