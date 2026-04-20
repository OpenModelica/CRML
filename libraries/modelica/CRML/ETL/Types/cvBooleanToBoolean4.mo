within CRML.ETL.Types;

function cvBooleanToBoolean4 "Conversion from Boolean to Boolean4"
        import CRML.ETL.Types.Boolean4;
        input Boolean x;
        output Boolean4 y;
      algorithm
        y := if x then Boolean4.true4 else Boolean4.false4;
      end cvBooleanToBoolean4;