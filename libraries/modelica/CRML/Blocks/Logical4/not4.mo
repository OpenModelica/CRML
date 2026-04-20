within CRML.Blocks.Logical4;

function not4 "Boolean4 not operator"
        import CRML.ETL.Types.Boolean4;
        input Boolean4 x;
        output Boolean4 y;
      algorithm
        y := TruthTables.not4[Integer(x)];
      end not4;