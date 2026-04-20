within CRML.ETL.Types;

function cvBooleanToBoolean3 "Conversion from Boolean to Boolean3"
        import CRML.ETL.Types.Boolean3;
        input Boolean x;
        output Boolean3 y;
      algorithm
        y := if x then Boolean3.true3 else Boolean3.false3;
      end cvBooleanToBoolean3;