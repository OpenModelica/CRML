within CRML.ETL.Types;

record TimeLocator "Description of a time locator"
        Boolean timePeriod "Represents the different time periods of the time locator";
        Boolean timeSlot "Represents the different time slots of the time locator";
        Boolean4 clock "Clock ticks";
        Boolean isLeftBoundaryIncluded "true if left boundary belongs to the CTL";
        Boolean isRightBoundaryIncluded "true if right boundary belongs to the CTL";
        Integer indexMax "Number of allocated time periods";
        annotation(
          Documentation(info = "<html>

</html>"));
      end TimeLocator;