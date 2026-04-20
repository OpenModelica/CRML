within CRML.Blocks.Logical4;

block And4_n "And operator on N Boolean4: u1 and u2 and ... and uN"
        import CRML.ETL.Types.Boolean4;
        parameter Integer N = CRML.ETL.Types.nMaxOverlap;
      protected
        Boolean4 z4[N];
      public
        ETL.Connectors.Boolean4Input[N] u annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.Boolean4Output y annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
      equation
        z4[1] = u[1];
        for i in 2:N loop
          z4[i] = Logical4.and4(z4[i - 1], u[i]);
        end for;
        y = z4[N];
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "and")}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-90, 40}, {90, -40}}, lineColor = {0, 0, 0}, textString = "and")}));
      end And4_n;