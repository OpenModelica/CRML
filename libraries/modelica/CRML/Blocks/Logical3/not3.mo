within CRML.Blocks.Logical3;

function not3 "Boolean3 not operator"
        import CRML.ETL.Types.Boolean3;
        input Boolean3 x;
        output Boolean3 y;
      algorithm
        y := Logical3.TruthTables.not3[Integer(x)];
      end not3;