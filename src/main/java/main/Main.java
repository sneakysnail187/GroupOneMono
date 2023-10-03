package main;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
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

    private static DbClient dbClient;

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

    private static boolean invalidLength(String text, String name, int maxLength){
        if(text.length() <= maxLength){
            return false;
        }else{
            System.out.printf("Error: %s cannot exceed %d characters\n", name, maxLength);
            return true;
        }
    }

    private static boolean invalidEmail(String email){
        if(EMAIL_PATTERN.matcher(email).matches()){
            return false;
        }else{
            System.out.println("Error: invalid email address");
            return true;
        }
    }

    private static UUID getOrCreateUUID(String uuidValue){
        return uuidValue == null || uuidValue.isEmpty() ? UUID.randomUUID() : UUID.fromString(uuidValue);
    }

    @Command(name = "event", mixinStandardHelpOptions = true, version="1.0")
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

        @Option(names = {"-ei", "--event-id"})
        String eventID;

        @Override
        public Integer call() throws SQLException {
            LocalDateTime dateTime = getLocalDateTime(date, time, ampm);
            if(dateTime == null){
                return 1;
            }

            if(invalidLength(title, "title", EVENT_TITLE_MAX_LENGTH)
                    || invalidLength(description, "description", EVENT_DESC_MAX_LENGTH)
                    || invalidEmail(hostEmail)){
                return 1;
            }

            dbClient.addEvent(new Event(getOrCreateUUID(eventID), dateTime, title, description, hostEmail));
            return 0;
        }
    }

    @Command(name = "participant", mixinStandardHelpOptions = true, version="1.0")
    private static class ParticipantCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String eventID;

        @Parameters(index = "1")
        String name;

        @Parameters(index = "2")
        String email;

        @Option(names = {"-pi", "--participant-id"})
        String participantID;

        @Override
        public Integer call() throws SQLException {
            if(invalidLength(name, "name", PARTICIPANT_NAME_MAX_LENGTH) || invalidEmail(email)){
                return 1;
            }

            dbClient.addParticipant(
                    new Participant(getOrCreateUUID(participantID), UUID.fromString(eventID), name, email)
            );
            return 0;
        }

    }

    @Command(name = "list-events", mixinStandardHelpOptions = true, version="1.0")
    private static class ListEventsCommand implements Callable<Integer> {

        @Override
        public Integer call() throws SQLException, Event.HandledIllegalValueException {
            List<Event> events = dbClient.getEvents();
            StringBuilder sb = new StringBuilder();

            for(Event event : events){
                sb.append(String.format(
                        "%s on %s at %s | Host Email: %s | Event ID: %s | '%s' \n\n",
                        event.title(),
                        event.eventDateTime().toLocalDate().format(
                                DateTimeFormatter.ISO_LOCAL_DATE
                        ),
                        //event.eventDateTime().getHour(), event.eventDateTime().getMinute(),
                        event.eventDateTime().toLocalTime().format(
                                DateTimeFormatter.ofPattern("hh:mm a")
                        ),
                        event.hEmail(),
                        event.uuid(),
                        event.description()
                ));
            }

            System.out.println(sb);
            return 0;
        }

    }

    @Command(name = "list-participants", mixinStandardHelpOptions = true, version="1.0")
    private static class ListParticipantsCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String eventID;

        @Override
        public Integer call() throws SQLException, Event.HandledIllegalValueException {
            List<Participant> participants = dbClient.getParticipants(eventID);
            StringBuilder sb = new StringBuilder();

            for(Participant participant : participants){
                sb.append(String.format(
                        "%s | Email : %s | Participant ID: %s",
                        participant.name(),participant.email(), participant.uuid()
                ));
            }

            System.out.println(sb);
            return 0;
        }

    }

    public static void main(String[] args) throws IOException, SQLException {
        dbClient = new DbClient();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String line;

        System.out.print("$> ");
        while((line = input.readLine()) != null){
            String[] words = line.split("\\s");
            String[] commandArgs = new String[words.length - 1];
            System.arraycopy(words, 1, commandArgs, 0, commandArgs.length);

            switch(words[0]){
                case "event" -> new CommandLine(new EventCommand()).execute(commandArgs);
                case "participant" -> new CommandLine(new ParticipantCommand()).execute(commandArgs);
                case "list-events" -> new CommandLine(new ListEventsCommand()).execute(commandArgs);
                case "list-participants" -> new CommandLine(new ListParticipantsCommand()).execute(commandArgs);
                default -> System.out.println("Error: unknown command");
            }
            System.out.print("$> ");
        }
    }

}
