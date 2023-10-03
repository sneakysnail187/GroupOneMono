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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static DbClient dbClient;

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

        @Option(names = {"-ei", "--event-id"})
        String eventID;

        @Override
        public Integer call() throws SQLException {
            try {

                if(Objects.nonNull(eventID)) {
                    dbClient.addEvent(Event.create(eventID, date, "%s %s".formatted(time, ampm), title, description, hostEmail));
                } else {
                    dbClient.addEvent(Event.create(date, "%s %s".formatted(time, ampm), title, description, hostEmail));
                }
            } catch(Event.HandledIllegalValueException e){
                System.out.println("Failed to create event: " + e.getMessage());
            }
            return 0;
        }
    }

    @Command(name = "participant", mixinStandardHelpOptions = true)
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
            try {
                if (participantID != null) {
                    dbClient.addParticipant(
                            Participant.create(participantID, eventID, name, email)
                    );
                } else {
                    dbClient.addParticipant(
                            Participant.create(eventID, name, email)
                    );
                }
            } catch (Event.HandledIllegalValueException e){
                System.out.println("Failed to create participant: " + e.getMessage());
            }
            return 0;
        }

    }

    @Command(name = "list-events", mixinStandardHelpOptions = true)
    private static class ListEventsCommand implements Callable<Integer> {

        @Override
        public Integer call() throws SQLException {
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

    @Command(name = "list-participants", mixinStandardHelpOptions = true)
    private static class ListParticipantsCommand implements Callable<Integer> {

        @Parameters(index = "0")
        String eventID;

        @Override
        public Integer call() throws SQLException{
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
