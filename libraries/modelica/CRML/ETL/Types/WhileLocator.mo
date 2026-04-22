within CRML.ETL.Types;

record WhileLocator "Description of a while locator"
        Boolean timePeriod "Represents the different time periods of the time locator";
        Boolean4 clock "Clock ticks";
        Boolean isLeftBoundaryIncluded "true if left boundary belongs to the CTL";
        Boolean isRightBoundaryIncluded "true if right boundary belongs to the CTL";
        annotation(
          Documentation(info = "<html>

</html>"));
      end WhileLocator;