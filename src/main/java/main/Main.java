package main;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final int EVENT_TITLE_MAX_LENGTH = 255;
    private static final int EVENT_DESC_MAX_LENGTH = 600;
    private static final int PARTICIPANT_NAME_MAX_LENGTH = 600;

    private static final Pattern DATE_PATTERN = Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^([0-9]{2}):([0-9]{2})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^\\w+@\\w+\\.\\w+$");

    private static LocalDateTime getLocalDateTime(String date, String time, String ampm){
        Matcher dateMatcher = DATE_PATTERN.matcher(date);
        if(!dateMatcher.matches()){
            System.out.println("Error: date must be in format YYYY-MM-DD");
            return null;
        }

        int year = Integer.parseInt(dateMatcher.group(1));
        int month = Integer.parseInt(dateMatcher.group(2));
        int dayOfMonth = Integer.parseInt(dateMatcher.group(3));

        Matcher timeMatcher = TIME_PATTERN.matcher(time);
        if(!timeMatcher.matches()){
            System.out.println("Error: time must be in format HH:MM");
            return null;
        }

        int hour = Integer.parseInt(timeMatcher.group(1));
        int minute = Integer.parseInt(timeMatcher.group(2));

        boolean isPM = ampm.equalsIgnoreCase("PM");
        if(!isPM && !ampm.equalsIgnoreCase("AM")){
            System.out.println("Error: unexpected value for AM/PM");
            return null;
        }

        if(isPM){
            hour += 12;
        }

        return LocalDateTime.of(year, month, dayOfMonth, hour, minute);
    }

    private static boolean validateLength(String text, String name, int maxLength){
        if(text.length() <= maxLength){
            return true;
        }else{
            System.out.printf("Error: %s cannot exceed %d characters\n", name, maxLength);
            return false;
        }
    }

    private static boolean validateEmail(String email){
        if(EMAIL_PATTERN.matcher(email).matches()){
            return true;
        }else{
            System.out.println("Error: invalid email address");
            return false;
        }
    }

    @Command(name = "event", mixinStandardHelpOptions = true)
    private static class EventCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String date;

        @Parameters(index = "1")
        String time;

        @Parameters(index = "2")
        String ampm;

        @Parameters(index = "3")
        String title;

        @Parameters(index = "4")
        String description;

        @Parameters(index = "5")
        String hostEmail;

        @Option(names = {"-u", "--uuid"})
        String uuidValue;

        @Override
        public Integer call(){
            LocalDateTime dateTime = getLocalDateTime(date, time, ampm);
            if(dateTime == null){
                return 1;
            }

            if(!validateLength(title, "title", EVENT_TITLE_MAX_LENGTH)
                    || !validateLength(description, "description", EVENT_DESC_MAX_LENGTH)
                    || !validateEmail(hostEmail)){
                return 1;
            }

            return 0;
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
                    System.out.println("Error: unknown command");
            }
        }
    }

}
