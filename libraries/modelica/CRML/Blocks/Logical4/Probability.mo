within CRML.Blocks.Logical4;
block Probability "Probability that input is true"
        import CRML.ETL.Types.Boolean4;
        import Modelica.Utilities.Files;
        import Modelica.Utilities.Streams;
        import Modelica.Utilities.Strings;
        parameter Real p = 1.96 "Quantile of normal distribution with probability p";
        parameter String fileName "Name of the file that stores the results for the next run. Name must be unique within model. File extension must be .csv.";
        parameter String dirName "Name of the directory that stores the results for the next run (without the trailing ' / ')";
        parameter String decimalSeparator = "," "Decimal separator in the result file";
        parameter Boolean reset "Reset file for a new set of runs";
      protected
        parameter Real infRange = 1 "Infinite range corresponding to the complete uncertainty on the probability estimate";
        parameter String input_file = dirName + "/" + fileName "Full name of the input file that stores the results of the previous run";
        parameter String output_file = input_file + "_temp" "Full name of the temporary output file that stores the results of the current run";
        Integer n(start = 0, fixed = true) "Number of runs";
        Integer nevents(start = 0, fixed = true) "Number of expected events";
        Integer X_n "Random variable for the predicate of u";
        Integer sigma_X_n(start = 0, fixed = true) "Sum of X_n over n";
        Real var_n(start = 0, fixed = true) "(X_n - y)^2";
        Real sigma_var_n(start = 0, fixed = true) "Sum of (X_n - y)^2 over n";
        Real variance(start = 0, fixed = true) "Variance";
        Integer i(start = 0, fixed = true) "Event counter";
        String line "Line in file";
        Integer ln(start = 0, fixed = true) "Index for the current line number in file";
        Integer p1(start = 0, fixed = true) "Index for the current position in line";
        Integer p2(start = 0, fixed = true) "Index for the current position in line";
        Boolean header_processed(start = false, fixed = true) "File header is processed";
        String dir, ker, ext;
        Boolean firstRun = not Files.exist(input_file) or reset;
      public
        ETL.Connectors.Boolean4Input u "Random Boolean input" annotation(
          Placement(transformation(extent = {{-120, -10}, {-100, 10}}), iconTransformation(extent = {{-120, -10}, {-100, 10}})));
        ETL.Connectors.RealOutput y(start = 0, fixed = true) "Estimate of the probability that input is true at the current time" annotation(
          Placement(transformation(extent = {{100, -10}, {120, 10}})));
        ETL.Connectors.RealOutput range(start = infRange, fixed = true) "Half-width uncertainty range of y" annotation(
          Placement(transformation(extent = {{100, -90}, {120, -70}})));
        ETL.Connectors.BooleanInput event annotation(
          Placement(transformation(extent = {{-120, -90}, {-100, -70}}), iconTransformation(extent = {{-120, -90}, {-100, -70}})));
algorithm
        /* Start of simulation */
        when initial() then
          /* Check that directory exists */
          assert(Files.exist(dirName), "Directory <" + dirName + "> given by parameter dirName does not exist");
          /* Check that file name is correct. It should not contain the directory name
       and should have the .csv extension */
          (dir, ker, ext) := Files.splitPathName(fileName);
          assert(dir == "", "Parameter fileName must not include the path of a directory (directory path found is <" + dir + ">)");
          assert(ext == ".csv", "The extension of parameter fileName must be .csv (extension found is <" + ext + ">)");
          /* Remove temporary output file in case previous run aborted without executing the exit handler */
          Files.removeFile(output_file);
        end when;
        /* Compute outputs at each event */
        when event then
          /* Increment event counter */
          i := i + 1;
          /* Process file header */
          if not header_processed then
            if not firstRun then
              /* Read number of lines in input file. Number of expected events is equal to number of lines minus one (the header).*/
              nevents := Streams.countLines(input_file) - 1;
              /* Skip header in input file */
              ln := ln + 1;
            end if;
            /* Write header to output file */
            Streams.print("Time; Number of runs; sigma_X_n; sigma_var_n; Probability; Half-width confidence range;", output_file);
            header_processed := true;
          end if;
          /* Initialize from input file for second and following runs */
          if not firstRun then
            /* Check that event counter does not exceed number of expected events */
            assert(i <= nevents, "Number of events in current run (" + String(i) + ") exceeds number of events in previous run (" + String(nevents) + ")");
            /* Read next line in input file */
            ln := ln + 1;
            line := Streams.readLine(input_file, ln);
            /*  Read time in line */
            p1 := 1;
            p2 := Strings.find(line, ";", p1);
            /*  Read number of runs in line */
            p1 := p2 + 1;
            p2 := Strings.find(line, ";", p1);
            n := Strings.scanInteger(Strings.substring(line, p1, p2 - 1));
            /*  Read sigma_X_n in line */
            p1 := p2 + 1;
            p2 := Strings.find(line, ";", p1);
            sigma_X_n := Strings.scanInteger(Strings.substring(line, p1, p2 - 1));
            /*  Read sigma_var_n in line */
            p1 := p2 + 1;
            p2 := Strings.find(line, ";", p1);
            sigma_var_n := Strings.scanReal(Strings.replace(Strings.substring(line, p1, p2 - 1), decimalSeparator, "."));
            /* Initialize from scratch for first run */
          else
            p1 := 1;
            p2 := 1;
            ln := 1;
            line := "";
            n := 0;
            nevents := 0;
            sigma_X_n := 0;
            sigma_var_n := 0;
          end if;
          /* Compute new probability and probability error estimates if u is true or false */
          if u == Boolean4.true4 or u == Boolean4.false4 then
            n := n + 1;
            X_n := (if u == Boolean4.true4 then 1 else 0);
            sigma_X_n := sigma_X_n + X_n;
            y := sigma_X_n/n;
            var_n := (X_n - y)^2;
            sigma_var_n := sigma_var_n + var_n;
            variance := if n >= 2 then sigma_var_n/(n - 1) else (infRange/p)^2;
            range := p*sqrt(variance/n);
          end if;
          /* Write results in temporary output file */
          line := String(time) + ";";
          line := line + String(n) + ";";
          line := line + String(sigma_X_n) + ";";
          line := line + Strings.replace(String(sigma_var_n), ".", decimalSeparator) + ";";
          line := line + Strings.replace(String(y), ".", decimalSeparator) + ";";
          line := line + Strings.replace(String(range), ".", decimalSeparator) + ";";
          Streams.print(line, output_file);
        end when;
        /* End of simulation: exit handler */
        when terminal() then
          /* Check that event counter is equal to number of expected events */
          if not firstRun then
            assert(i == nevents, "Number of events in current run (" + String(i) + ") is different from number of events in previous run (" + String(nevents) + ")");
          end if;
          /* Close input file */
          Streams.close(input_file);
          /* Close output file */
          Streams.close(output_file);
          /* Copy temporary output file to result file and remove temporary output file */
          Files.move(oldName = output_file, newName = input_file, replace = true);
        end when;
        annotation(
          Icon(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-160, 160}, {158, -160}}, lineColor = {0, 0, 0}, textString = "ℙ", fontName = "Cambria Math"), Text(extent = {{-218, 140}, {198, 100}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%fileName")}),
          Diagram(coordinateSystem(preserveAspectRatio = false), graphics = {Rectangle(extent = {{-100, 100}, {100, -100}}, fillColor = {255, 213, 170}, lineThickness = 5, fillPattern = FillPattern.Solid, borderPattern = BorderPattern.Raised, lineColor = {0, 0, 0}), Text(extent = {{-160, 160}, {158, -160}}, lineColor = {0, 0, 0}, textString = "ℙ", fontName = "Cambria Math"), Text(extent = {{-218, 140}, {198, 100}}, lineColor = {0, 0, 255}, lineThickness = 0.5, fillColor = {255, 255, 255}, fillPattern = FillPattern.Solid, textString = "%fileName")}));
end Probability;
