import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.util.*;

public class GroupOneApp {

    static Scanner input = new Scanner(System.in);
    static JSONParser parser = new JSONParser();
    final static String fileName = "jsonData.json";

    public static void writeEvent(String uuid, String date, String time, String description, String hEmail) {
        JSONObject obj = new JSONObject();
        obj.put("UUID", uuid);
        obj.put("date", date);
        obj.put("time", time);
        obj.put("desc", description);
        obj.put("host", hEmail);
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            reader.close();
            JSONArray events = (JSONArray) jsobj.get("events");
            events.add(obj);
            jsobj.put("events", events);
            FileWriter writer = new FileWriter(fileName);
            writer.write(jsobj.toJSONString());
            writer.flush();
            writer.close();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public static void getEvents() {
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            JSONArray events = (JSONArray) jsobj.get("events");
            System.out.println(events.toString());
            reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void clearEvents() {
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            JSONArray participants = (JSONArray) jsobj.get("participants");
            reader.close();
            FileWriter writer = new FileWriter(fileName);
            JSONObject obj = new JSONObject();
            obj.put("events", new JSONArray());
            obj.put("participants", participants);
            writer.write(obj.toJSONString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void writeParticipant(String uuid, String event, String name, String email) {
        JSONObject obj = new JSONObject();
        obj.put("UUID", uuid);
        obj.put("event", event);
        obj.put("name", name);
        obj.put("email", email);
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            reader.close();
            JSONArray events = (JSONArray) jsobj.get("participants");
            events.add(obj);
            jsobj.put("participants", events);
            FileWriter writer = new FileWriter(fileName);
            writer.write(jsobj.toJSONString());
            writer.flush();
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public static void getParticipants(String regex) {
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            JSONArray participants = (JSONArray) jsobj.get("participants");
            //TODO: return everything from participants where the EVENT UUID matches supplied regex
            System.out.println(participants);
            reader.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void clearParticipants() {
        try {
            FileReader reader = new FileReader(fileName);
            JSONObject jsobj = (JSONObject) parser.parse(reader);
            JSONArray events = (JSONArray) jsobj.get("events");
            reader.close();
            FileWriter writer = new FileWriter(fileName);
            JSONObject obj = new JSONObject();
            obj.put("events", events);
            obj.put("participants", new JSONArray());
            writer.write(obj.toJSONString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void processCmds(String[] cArgs) throws IOException, CloneNotSupportedException{
        if(cArgs == null || cArgs[0] == null || cArgs.length == 0){
            System.out.println("null command");
        }

        String command = cArgs[0];

        if(command.trim().equalsIgnoreCase("createEvent")){
            //TODO: input validation
            String uuid = promptUser("UUID: (leave empty for random) ");
            uuid = uuid == "" ? UUID.randomUUID().toString() : uuid;
            String date = promptUser("Event Date: ");
            String time = promptUser("Event Time: ");
            String desc = promptUser("Description: ");
            String email = promptUser("Host Email: ");
            writeEvent(uuid, date, time, desc, email);
        }

        else if(command.trim().equalsIgnoreCase("getEvents")) {
            getEvents();
            //TODO: do we want to supply a regex here?
        }

        else if (command.trim().equalsIgnoreCase("clearEvents")) {
            clearEvents();
        }

        else if(command.trim().equalsIgnoreCase("createParticipant")){
            //TODO: input validation
            String uuid = promptUser("Participant UUID: (leave empty for random) ");
            uuid = uuid == "" ? UUID.randomUUID().toString() : uuid;
            String date = promptUser("Event UUID: ");
            String time = promptUser("Participant Name: ");
            String email = promptUser("Participant Email: ");
            writeParticipant(uuid, date, time, email);
        }

        else if(command.trim().equalsIgnoreCase("getParticipants")){
            String regex = promptUser("Regex for event UUID: ");
            getParticipants(regex);
        }

        else if (command.trim().equalsIgnoreCase("clearParticipants")) {
            clearParticipants();
        }
    }

    public static String promptUser(String prompt) {
        System.out.println(prompt);
        return(input.nextLine());
    }


    public static void main(String[] args) throws Exception {
        do{
            //TODO: put syntax in readme or here
            String[] split = promptUser("Use createEvent and getEvents to create or view existing events.\nUse createParticipant and getParticipants to create or view existing participants. \nUse \"stop\" to kill the program").split(" ");
            String command = split[0];
            if(command.trim().equalsIgnoreCase("stop")){
                break;
            } else {
                processCmds(split);
            }
        } while(true);
        System.out.println("Program terminated");
        input.close();
    }
}
