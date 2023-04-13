package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        // Read contents of input file
        String code = SpecsIo.read(inputFile);

        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();

        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);
        if(parserResult.getReports().size() > 0){

            //TestUtils.noErrors(parserResult.getReports()); //imprime as exceções
            Integer errorNumber = parserResult.getReports().size();
            String errorMessage = "A total of " + errorNumber + " errors have occurred";
            System.out.println(errorMessage);
            for(int i = 0; i < parserResult.getReports().size(); i++){
                System.out.println("Errors:" + parserResult.getReports());
            }


        }
        else{
            System.out.println(parserResult.getRootNode().toTree()); //dar print da árvore
            MySymbolTable mySymbolTable = new MySymbolTable(parserResult.getRootNode());

            System.out.println(mySymbolTable.print());
            //System.out.println(mySymbolTable.getImports());

        }

        //fazer um for para correr os reports






        // Check if there are parsing errors
        //





        // ... add remaining stages

        /* Instantiate JasminBackender
        var jasminBackend = new JasminBackender();

        var backendResult = jasminBackend.toJasmin(ollirResult);

        // Generate .class file
        backendResult.compile(path.toFile());
         */
    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length != 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", "false");
        config.put("registerAllocation", "-1");
        config.put("debug", "false");

        return config;
    }

}
