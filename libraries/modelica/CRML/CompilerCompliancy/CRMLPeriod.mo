within CRML.CompilerCompliancy;

record CRMLPeriod
      Boolean isLeftBoundaryIncluded "If true, the left boundaries of the time periods are included";
      Boolean isRightBoundaryIncluded "If true, the right boundaries of the time periods are included";
    public
      CRMLEvent start_event;
      CRMLEvent close_event;
      Boolean is_open;
    end CRMLPeriod;