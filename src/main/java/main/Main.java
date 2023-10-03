package main;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

public class Main {

    @Command(name = "event", mixinStandardHelpOptions = true)
    private static class EventCommand implements Callable<Integer> {

        @Parameters(index = "0..*")
        String commandArgs;

        @Override
        public Integer call() throws Exception {
            System.out.println(commandArgs.length());
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while((line = input.readLine()) != null){
            String[] words = line.split("\\s");
            String[] commandArgs = new String[words.length - 1];
            System.arraycopy(words, 1, commandArgs, 0, commandArgs.length);

            switch(words[0]){
                case "event":
                    new CommandLine(new EventCommand()).execute(commandArgs);
                    break;
                default:
                    System.out.println("Error: Unknown Command");
            }
        }
    }

}
