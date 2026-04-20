within CRML.CompilerCompliancy;

record CRMLClock
    //constant Integer buffer_size=50;  // number of events that can be logged
      CRML.ETL.Types.Boolean4 b( start= CRML.ETL.Types.Boolean4.false4);

      Real ticks[50](each start = -1, each fixed = true);
      discrete Integer counter(start=1, fixed=true);

      CRML.ETL.Types.Boolean4 out(start = CRML.ETL.Types.Boolean4.false4);

    end CRMLClock;