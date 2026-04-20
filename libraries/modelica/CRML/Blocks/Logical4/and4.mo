within CRML.Blocks.Logical4;

function and4 "Boolean4 and operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x1;
        input Boolean4 x2;
        output Boolean4 y;
      algorithm
        y := TruthTables.and4[Integer(x1), Integer(x2)];
      end and4;