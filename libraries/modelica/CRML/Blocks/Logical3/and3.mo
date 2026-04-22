within CRML.Blocks.Logical3;

function and3 "Boolean3 and operator"
        import CRML.ETL.Types.Boolean3;
        input Boolean3 x1;
        input Boolean3 x2;
        output Boolean3 y;
      algorithm
        y := Logical3.TruthTables.and3[Integer(x1), Integer(x2)];
      end and3;