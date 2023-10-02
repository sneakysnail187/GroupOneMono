package main.java.main;

import java.io.FileReader;
import java.io.FileWriter;

public class EventWriter {
    public static void writeEvent(Event e) {
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
            //do something
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
}
