package crml.compiler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import crml.compiler.omc.CompileSettings;

public class CommandLineArgs {
    @Parameter(description = "Files to be compiled")
    public List<String> files = new ArrayList<>();

    @Parameter(names = {"--printAST" }, description = "If set to true will display the AST for the model.")
    public Boolean printAST = false;

    @Parameter(names = {"--external", "-e"}, description = "Generate a file containing the external variables found in the CRML model. \n Used to connect the requirement model to validation models.")
    public Boolean generateExternal = false;

    @Parameter(names = {"--stacktrace"}, description = "Print the exception stack for errors")
    public Boolean stacktrace = false;

    @Parameter(names = {"-t"}, description = "Treat the filenames as JUnit tests")
    public Boolean junit = false;

    @Parameter(names = {"-o"}, description = "Provide an output directory via -o on where to write the .mo files. If no output directory via -o is given then the .mo files are generated in the current directory.")
    public Path outputDir = Paths.get("generated");

    @Parameter(names = "--help", help = true)
    public boolean help;

    @Parameter(names = "--simulate", description = "simulate the files passed in parameters, takes the path to the additional omc files")
    public String simulate = null; 

    @Parameter(names = "--verify",  description = "verify the files passed in parameters, takes the path to the reference files")
    public String verify = null;
    
    @Parameter(names = "--within",  description = "generate the translated modelica model within a given class")
    public String within = "";

    @Parameter(names = {"--causal"}, description = "Generates explicit input prefixes in helper blocks")
    public Boolean causal = false;


    /**
     * Hide constructor
     * Only parse should be able to create an instance
     */
    private CommandLineArgs(){};

    /**
     * Parse command line paramters without valication.
     * @param args Command line arguments
     * @return 
     */
    public static CommandLineArgs parse(String... args){
        return parse(false, args);
    }
    
    /** Parse command line arguments.
     * 
     * @param validate Flag to validate configuration.
     * @param args Command line arguments
     * @return
     */
    public static CommandLineArgs parse(Boolean validate, String... args){
        CommandLineArgs config = new CommandLineArgs();
        
        JCommander jc = JCommander.newBuilder().addObject(config).build();
        jc.setProgramName("crmlc");
        jc.parse(args);

        if (config.help) {
            jc.usage();
            return null;
        }

        // incorrect arguments
        if (validate && config.files.isEmpty()){
            System.err.println(" incorrect arguments");
            jc.usage();
            return null;
        }

        return config;
    }

    //TODO: Create a function that projects the settings to OMC ComplieSettings.
    public CompileSettings projectToOMCSettings(){
        CompileSettings settigns = new CompileSettings();

        if(simulate != null)
            settigns.verifModelFolder = Paths.get(simulate);
    
        if(verify != null)
            settigns.referenceResFolder = Paths.get(verify);

        return settigns;
    }

//    private <T> T withDefault(T value, T defaultValue){
//        if(value!=null){
//            return value;
//        } else {
//            return defaultValue;
//        }
//    }
}
