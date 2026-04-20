within CRML.Blocks.Logical3;

function or3 "Boolean3 or operator"
        import CRML.ETL.Types.Boolean3;
        input Boolean3 x1;
        input Boolean3 x2;
        output Boolean3 y;
      algorithm
        y := Logical3.not3(Logical3.and3(Logical3.not3(x1), Logical3.not3(x2)));
      end or3;