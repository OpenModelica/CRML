package crml.compiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.ParseCancellationException;

import crml.compiler.omc.CompileSettings;
import crml.compiler.omc.ModelicaSimulationException;
import crml.compiler.omc.OMCUtil;
import crml.compiler.omc.OMCmsg;
import crml.compiler.translation.Value;
import crml.compiler.translation.crmlVisitorImpl;
import crml.language.util.Parser;
import crml.language.util.Parser.ParserResult;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;



/**
 * Main entry point for the crmlc compiler
 * @author Lena Buffoni
 * @version 1.0
 */
public class CRMLC {

   private static final Logger logger = LogManager.getLogger();	

   public static void main( String[] args ) throws Exception {
    CommandLineArgs cmd = CommandLineArgs.parse(true, args);
    if(cmd==null){
      return;
    }

    CompileSettings cs = cmd.projectToOMCSettings();
    
    logger.trace("Output dir: " + cmd.outputDir);
    // remove quotes if they exist
    //cmd.outputDir = cmd.outputDir.replace("\"", "");
      
    File out_dir = cmd.outputDir.toFile();
    out_dir.mkdir();

    logger.trace("Directory for generated .mo files: [" + out_dir.getPath() + "] absolute path: [" + out_dir.getAbsolutePath() + "]");

    for(String f : cmd.files){
       // remove quotes if they exist
       Path path = Path.of(f.replace("\"", ""));
       File file = path.toFile();
       String [] testFiles;
       if (file.isDirectory()){
          testFiles=file.list();
           for (String test : testFiles) {
            if(test.endsWith(".crml")) {
            logger.trace("Translating test: " + test);
              parse_file(Path.of(test), cmd.outputDir, cmd.stacktrace, cmd.printAST , 
                cmd.generateExternal, cmd.within, cmd.causal);
              if(cmd.simulate!=null) {
                OMCmsg msg;
                try {
                  msg = OMCUtil.compile(test, path, cs);
                  if(msg.msg.contains("false"))
                  logger.error("Unable to load Modelica model " + test + 
                      "\n omc fails with the following message: \n" + msg);
                }
                catch (ModelicaSimulationException e) {
                  logger.error("Unable to simulate: " + file + "\n");
                }
              } 
            }
          }
        } else if (file.isFile()){
        logger.trace("Translating file: " + file);
          String stripped_file_name = Utilities.stripNameEndingAndPath(path);
          Path outputDir = cmd.outputDir.resolve(stripped_file_name);
          parse_file(path, outputDir, cmd.stacktrace, 
            cmd.printAST, cmd.generateExternal, cmd.within, cmd.causal);
         if(cmd.simulate!=null){
                OMCmsg msg;
                try {
                  msg = OMCUtil.compile(file.getPath(), cmd.outputDir, cs);// TODO: Why does it get path for Stripped file name?
                  if(msg.msg.contains("false"))
                  logger.error("Unable to load Modelica model " + file + 
                      "\n omc fails with the following message: \n" + msg);
                }
                catch (ModelicaSimulationException e) {
                  logger.error("Unable to simulate: " + file + "\n");
                }
              } 
        } else
        logger.error("Translation error : " + path +  " is not a correct path");
      }
    
  }

  public static void parse_file (
      Path fullName, 
      Path gen_dir, Boolean testMode, Boolean printAST,
      Boolean generateExternal,
      String within,
      Boolean causal) throws Exception {
  
    try {
      File in_file = fullName.toFile();
      
      Parser parser = new Parser();
      ParserResult model = parser.parse(in_file);
      
      if (model.ast() == null)
        logger.error("Unable to parse: " + fullName);

      if (printAST){
        logger.trace("\nThe AST for the program: \n" + model.toPrettyTree());
      }
       
      List<String> external_var = new ArrayList<String>();
      crmlVisitorImpl visitor;
     

      if (generateExternal)
        visitor = new crmlVisitorImpl(model.parser(), external_var, causal);
      else
        visitor = new crmlVisitorImpl(model.parser(), causal);

      try {
        Value result = visitor.visit(model.ast());
  
        if (result != null) {  	
          File out_file = gen_dir.resolve(Utilities.stripNameEndingAndPath(fullName) + ".mo").toFile();
          out_file.getParentFile().mkdirs();   	
          
          System.out.println("File : " + out_file.toString() + " within : " + within);
          writeToFile(out_file, "\n", "within " + within + ";", result.toModelica());
          logger.trace("Translated: " + fullName);
          logger.trace("Output Modelica file: " + out_file.getAbsolutePath());

          if(generateExternal && !external_var.isEmpty()){
            File ext_file = new File(gen_dir + java.io.File.separator + 
              Utilities.stripNameEndingAndPath(fullName)+ "_external.txt");
            writeToFile(ext_file, "\n", external_var.toArray());
            logger.trace("External variables saved in: " + ext_file);
          }           
        }
        else {
          logger.error("Unable to translate: " + fullName + "\n");
          if (printAST){
            logger.trace("\nThe AST for the program: \n" + model.toPrettyTree());
          }
          if(testMode)
            throw new Exception("Translation error");
          
        }
        
      } catch (ParseCancellationException e) {
        logger.error("Translation error: "+ e, e);
        if (printAST){
          logger.trace("\nThe AST for the program: \n" + model.toPrettyTree());
        }
        if (testMode) throw e;
      }
      catch(Exception e) {
        logger.error("Uncaught error: " + e, e);
        if (printAST){
          logger.trace("\nThe AST for the program: \n" + model.toPrettyTree());
        }
        if (testMode) throw e;
      }
    }
    catch(Exception e)
    {
      logger.error("Uncaught error: " + e, e);
      if (testMode) throw e;
    }
  }

  private static void writeToFile(File output, String separator, Object... blocks) throws IOException{
    BufferedWriter writer = new BufferedWriter(new FileWriter(output));
    for(Object block : blocks){
      if(block!=null){
        writer.write(block.toString() + separator);
      }
    }
    writer.close();
  }
}
