package org.stegosuite;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.stegosuite.ui.cli.Cli;
import org.stegosuite.ui.gui.Gui;
import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Model.UsageMessageSpec.*;

@Command(name = "stegosuite", version = "stegosuite 0.9.1", mixinStandardHelpOptions = true,
        subcommands = {CommandLine.HelpCommand.class}, usageHelpAutoWidth = true,
        header = "Steganography tool to hide information in image files",
        synopsisHeading = "%nUsage: ",
        optionListHeading = "%nOptions:%n",
        commandListHeading = "%nCommands:%n",
        exitCodeListHeading = "%nExit Codes:%n",
        exitCodeList = {
                "@|bold 0|@:Successful program execution",
                "@|bold 1|@:Internal software error: an exception occurred when invoking " +
                        "the business logic of this command.",
                "@|bold 2|@:Usage error: user input for the command was incorrect, " +
                        "e.g., the wrong number of arguments",
        },
        description = "Stegosuite is a steganography tool to easily hide information in image files. It allows the " +
                "embedding of text messages and multiple files of any type. In addition, the embedded data is encrypted " +
                "using AES. Currently supported  file  types are BMP, GIF, JPG and PNG.")
public class Stegosuite implements Runnable {

    final static String SECTION_KEY_EXAMPLE_HEADING = "exampleHeading";
    final static String SECTION_KEY_EXAMPLE_DETAILS = "example";

    @Spec
    static
    CommandSpec spec;

    @Command(name = "stegosuite", version = "stegosuite 0.9.1", mixinStandardHelpOptions = true,
            subcommands = {CommandLine.HelpCommand.class, GuiSubCommand.class,
                    EmbedSubCommand.class, ExtractSubCommand.class, CapacitySubCommand.class},
            usageHelpAutoWidth = true,
            header = "Steganography tool to hide information in image files",
            synopsisHeading = "%nUsage: ",
            optionListHeading = "%nOptions:%n",
            commandListHeading = "%nCommands:%n",
            exitCodeListHeading = "%nExit Codes:%n",
            exitCodeList = {
                    "@|bold 0|@:Successful program execution",
                    "@|bold 1|@:Internal software error: an exception occurred when invoking " +
                            "the business logic of this command.",
                    "@|bold 2|@:Usage error: user input for the command was incorrect, " +
                            "e.g., the wrong number of arguments",
            },
            description = "Stegosuite is a steganography tool to easily hide information in image files. It allows the " +
                    "embedding of text messages and multiple files of any type. In addition, the embedded data is encrypted " +
                    "using AES. Currently supported  file  types are BMP, GIF, JPG and PNG.")
    static class ParentCommand implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
           // throw new ParameterException(spec.commandLine(), "Specify a subcommand");
            return null;
        }
    }

    @Command(name = "gui",
            header = "Starts the GUI",
            synopsisHeading = "%nUsage: ",
            description = "This command starts the GUI, which is written using SWT.",
            usageHelpAutoWidth = true,
            sortOptions = false,
            parameterListHeading = "%nParameters:%n",
            optionListHeading = "%nOptions:%n"
    )
    static class GuiSubCommand implements Callable<Integer> {
        @Parameters(arity = "0..1", paramLabel = "<image>",
                description = "The image file to process.")
        String image;
        @Option(names = {"-d", "--debug"}, description = "Shows debug information.")
        boolean debug;

        @Override
        public Integer call() throws Exception {
            if (debug) {
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                        .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }
            if (image == null) {
                new Gui(null);
            } else {
                new Gui(image);
            }
            return 0;
        }
    }

    @Command(name = "embed",
            header = "Embeds data into image",
            synopsisHeading = "%nUsage: ",
            description = "This command embeds data into an image file using the provided key as seed for " +
                    "randomization and encryption. It is possible to embed a text message and multiple files " +
                    "of any type, as long as the image has enough capacity to embed data. The capacity depends " +
                    "on the size of the image and its type. It can be checked with @|bold stegosuite capacity|@.",
            usageHelpAutoWidth = true,
            sortOptions = false,
            exitCodeListHeading = "%nExit Codes:%n",
            exitCodeList = {
                    "@|bold 0|@:Successful program execution",
                    "@|bold 1|@:Internal software error: An exception occurred when invoking " +
                            "the business logic of this command.",
                    "@|bold 2|@:Usage error: User input for the command was incorrect, " +
                            "e.g., the wrong number of arguments.",
            },
            parameterListHeading = "%nParameters:%n",
            optionListHeading = "%nOptions:%n"
    )
    static class EmbedSubCommand implements Callable<Integer> {
        @Parameters(arity = "1", paramLabel = "<image>",
                description = "Path to the image file to process.")
        File image;

        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;
        @Option(names = {"-m", "--message"}, paramLabel = "<message>", description = "The text message to be embedded into the image.")
        String message;
        @Option(names = {"-f", "--files"}, paramLabel = "<file>", split = ",", description = "Paths to the files to be embedded into the image.")
        List<File> files;
        @Option(names = {"-o", "--output"}, paramLabel = "<outputPath>", description = "Specifies path to the generated image file.")
        File output;
        @Option(names = {"-d", "--debug"}, description = "Shows debug information.")
        boolean debug;
        @Option(names = {"--disable-noise-detection"}, hidden = true,
                description = "Disables the automatic avoidance of homogeneous areas in the image.")
        boolean noNoise;




        static class Exclusive {
            @Option(names = {"-k", "--key"}, paramLabel = "<key>", description = "The secret key used for encryption and hiding.")
            String key;
            @Option(names = {"--keyfile"}, paramLabel = "<keyfile>", description = "Path to a file which contains the secret key. Reads its first line.")
            File keyfile;
        }

        @Override
        public Integer call() throws Exception {
            if (debug) {
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                        .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }
            Cli cli = new Cli();
            if (exclusive.keyfile != null) {
                try {
                    return cli.embed(image, message, files, Files.readAllLines(Paths.get(exclusive.keyfile.getPath())).get(0), noNoise, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return cli.embed(image, message, files, exclusive.key, noNoise, output);
            }
        }
    }

    @Command(name = "extract",
            header = "Extracts data from image",
            synopsisHeading = "%nUsage: ",
            description = "This command extracts data from an image file using the provided key. " +
                    "It will only work, if stegosuite was used to embed data into the image file and if the key " +
                    "is identical to the one used during embedding.",
            usageHelpAutoWidth = true,
            sortOptions = false,
            exitCodeListHeading = "%nExit Codes:%n",
            exitCodeList = {
                    "@|bold 0|@:Successful program execution",
                    "@|bold 1|@:Internal software error: An exception occurred when invoking " +
                            "the business logic of this command.",
                    "@|bold 2|@:Usage error: User input for the command was incorrect, " +
                            "e.g., the wrong number of arguments.",
            },
            parameterListHeading = "%nParameters:%n",
            optionListHeading = "%nOptions:%n")
    static class ExtractSubCommand implements Callable<Integer> {
        @Parameters(arity = "1", paramLabel = "<image>",
                description = "Path to the image file to process.")
        File image;

        @ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        @Option(names = {"-d", "--debug"}, description = "Shows debug information.")
        boolean debug;
        @Option(names = {"--disable-noise-detection"}, hidden = true,
                description = "Disables the automatic avoidance of homogeneous areas in the image.")
        boolean noNoise;

         static class Exclusive {
            @Option(names = {"-k", "--key"}, paramLabel = "<key>", description = "The secret key used for encryption and hiding.")
            String key;
            @Option(names = {"--keyfile"}, paramLabel = "<keyfile>", description = "Path to a file which contains the secret key. Reads its first line.")
            File keyfile;
        }

        @Override
        public Integer call() throws Exception {
            if (debug) {
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                        .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }
            Cli cli = new Cli();
            if (exclusive.keyfile != null) {
                try {
                    return cli.extract(image, Files.readAllLines(Paths.get(exclusive.keyfile.getPath())).get(0), noNoise);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return cli.extract(image, exclusive.key, noNoise);
            }
        }
    }

    @Command(name = "capacity",
            header = "Shows the maximum amount of embeddable data",
            description = "This command shows the maximum amount of embeddable data of an image file. " +
                    "It depends on the size of the image (number of pixels) and its type.",
            synopsisHeading = "%nUsage: ",
            usageHelpAutoWidth = true,
            exitCodeListHeading = "%nExit Codes:%n",
            exitCodeList = {
                    "@|bold 0|@:Successful program execution",
                    "@|bold 1|@:Internal software error: An exception occurred when invoking " +
                            "the business logic of this command.",
                    "@|bold 2|@:Usage error: User input for the command was incorrect, " +
                            "e.g., the wrong number of arguments.",
            },
            parameterListHeading = "%nParameters:%n",
            optionListHeading = "%nOptions:%n")
    static class CapacitySubCommand implements Callable<Integer> {
        @Parameters(arity = "1", paramLabel = "<image>",
                description = "Path to the image file to process.")
        File image;
        @Option(names = {"-d", "--debug"}, description = "Shows debug information.")
        boolean debug;
        @Option(names = {"--disable-noise-detection"}, hidden = true,
                description = "Disables the automatic avoidance of homogeneous areas in the image.")
        boolean noNoise;

        @Override
        public Integer call() throws Exception {
            if (debug) {
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                        .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }
            Cli cli = new Cli();
            return cli.capacity(image, noNoise);
        }
    }

    public static void main(String[] args) {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

//        CommandLine cmd = new CommandLine(new Stegosuite());
        CommandLine cmd = new CommandLine(new ParentCommand());


        cmd.setUsageHelpLongOptionsMaxWidth(26);
        cmd.getHelpSectionMap().remove(SECTION_KEY_DESCRIPTION);
        cmd.getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST_HEADING);
        cmd.getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST);

        //example stegosuite
        Map<String, String> example = new LinkedHashMap<>();
        example.put("@|bold stegosuite help embed|@", "Displays help for @|bold stegosuite embed|@");

        cmd.getHelpSectionMap().put(SECTION_KEY_EXAMPLE_HEADING,
                help -> help.createHeading("%nExample:%n"));
        cmd.getHelpSectionMap().put(SECTION_KEY_EXAMPLE_DETAILS,
                help -> help.createTextTable(example).toString());
        List<String> keys = new ArrayList<>(cmd.getHelpSectionKeys());
        int index = keys.indexOf(SECTION_KEY_EXIT_CODE_LIST_HEADING);
        keys.add(index, SECTION_KEY_EXAMPLE_HEADING);
        keys.add(index + 1, SECTION_KEY_EXAMPLE_DETAILS);
        cmd.setHelpSectionKeys(keys);
        //----

        //example stegosuite embed
        Map<String, String> example_embed = new LinkedHashMap<>();
        example_embed.put("@|bold stegosuite embed|@ @|yellow -k|@ @|italic my_secret_key|@ @|yellow -m|@ @|italic \"My secret message\"|@ @|yellow /path/to/image_file.png|@", "");

        cmd.getSubcommands().get("embed").getHelpSectionMap().put(SECTION_KEY_EXAMPLE_HEADING,
                help -> help.createHeading("%nExample:%n"));
        cmd.getSubcommands().get("embed").getHelpSectionMap().put(SECTION_KEY_EXAMPLE_DETAILS,
                help -> help.createTextTable(example_embed).toString());
        List<String> keys_embed = new ArrayList<>(cmd.getSubcommands().get("embed").getHelpSectionKeys());
        cmd.getSubcommands().get("embed").setHelpSectionKeys(keys_embed);
        //----

        //example stegosuite extract
        Map<String, String> example_extract = new LinkedHashMap<>();
        example_extract.put("@|bold stegosuite extract|@ @|yellow -k|@ @|italic my_secret_key|@ @|yellow /path/to/image_file.png|@", "");

        cmd.getSubcommands().get("extract").getHelpSectionMap().put(SECTION_KEY_EXAMPLE_HEADING,
                help -> help.createHeading("%nExample:%n"));
        cmd.getSubcommands().get("extract").getHelpSectionMap().put(SECTION_KEY_EXAMPLE_DETAILS,
                help -> help.createTextTable(example_extract).toString());
        List<String> keys_extract = new ArrayList<>(cmd.getSubcommands().get("extract").getHelpSectionKeys());
        cmd.getSubcommands().get("extract").setHelpSectionKeys(keys_extract);
        //----


        cmd.getSubcommands().get("embed").getHelpSectionMap().remove(SECTION_KEY_DESCRIPTION);
        cmd.getSubcommands().get("embed").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST_HEADING);
        cmd.getSubcommands().get("embed").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST);

        cmd.getSubcommands().get("extract").getHelpSectionMap().remove(SECTION_KEY_DESCRIPTION);
        cmd.getSubcommands().get("extract").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST_HEADING);
        cmd.getSubcommands().get("extract").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST);

        cmd.getSubcommands().get("capacity").getHelpSectionMap().remove(SECTION_KEY_DESCRIPTION);
        cmd.getSubcommands().get("capacity").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST_HEADING);
        cmd.getSubcommands().get("capacity").getHelpSectionMap().remove(SECTION_KEY_EXIT_CODE_LIST);

        cmd.getSubcommands().get("gui").getHelpSectionMap().remove(SECTION_KEY_DESCRIPTION);

        cmd.setExecutionStrategy(new RunAll());
        if (args.length == 0) {
            cmd.usage(System.out);
        } else {
            int exitCode = cmd.execute(args);
            System.exit(exitCode);
        }
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Specify a subcommand");
    }
}
