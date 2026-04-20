within CRML.ETL.Utilities;

function intersectionsPolygonLine "Returns the vector of intersections between a polygon and a line"
        input Real polygon[:, 2] "Polygon";
        input Real x0_R "Line origin abscissa";
        input Real y0_R "Line origin ordinate";
        input Real a_R "Line direction abscissa";
        input Real b_R "Line direction ordinate";
        input Real tolerance = 0 "Tolerance";
        output Real int[size(polygon, 1)] "Vector of intersection points on line";
        output Integer n "Number of intersection points. If -1, the result is undecided";
      protected
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
        Real t;
        // Edge curvilinear abscissa
        Real t_R;
        // Ray curvilinear abscissa
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
        /* Initialize vector of intersections */
        n := 0;
        /* Compute the intersections between the polygon and the ray */
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
          /* The curvilinear abscissa of the egde end is set to 1 */
          a := x1 - x0;
          b := y1 - y0;
          /* Intersection between ray and edge. The denominators of the
    two following expressions are never zero because ray direction is always
    different from edge direction */
          t := ((a_R*(y0_R - y0) - b_R*(x0_R - x0))/(a_R*b - b_R*a));
          t_R := ((a*(y0 - y0_R) - b*(x0 - x0_R))/(a*b_R - b*a_R));
          /* If t is in [0 + tolerance, 1 - tolerance], then the ray intersects the edge. 
    Add intersection to the vector of intersections on ray. 
    If t is outside [0 - tolerance, 1 + tolerance], then the ray does not intersect the edge. 
    Otherwise, it is undecided whether the ray intersects the edge or not */
          if abs(t) < tolerance or abs(1 - t) < tolerance then
            n := -1;
            return;
          end if;
          if t >= tolerance and t <= 1 - tolerance then
            n := n + 1;
            int[n] := t_R;
          end if;
        end for;
      end intersectionsPolygonLine;