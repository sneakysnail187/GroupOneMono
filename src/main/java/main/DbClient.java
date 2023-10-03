package main;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.Event.HandledIllegalValueException;

public class DbClient {
    private static final String DB_URL = "jdbc:sqlite:test.db";
    private Connection conn;
    
    public DbClient() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        this.createDatabase();
    }

    public void close() throws SQLException {
        if (conn != null)
            conn.close();
    }

    public void createDatabase() throws SQLException {
        conn.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS events (\n"
            + "  id text PRIMARY KEY,\n"
            + "  date date,\n"
            + "  time time,\n"
            + "  title varchar(255),\n"
            + "  description varchar(600),\n"
            + "  host_email text\n"
            + ");"
        );
        conn.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS participants (\n"
            + "  id text PRIMARY KEY,\n"
            + "  event_id text,\n"
            + "  name varchar(600),\n"
            + "  email text,\n"
            + "  FOREIGN KEY (event_id) REFERENCES events(id)\n"
            + ");"
        );
    }

    public List<Event> getEvents() throws SQLException {
        try {
            List<Event> output = new ArrayList<Event>();
            ResultSet results = conn.createStatement().executeQuery(
                    "SELECT * FROM events"
            );
            while (results.next()) {
                output.add(Event.create(
                        results.getString("id"),
                        (new SimpleDateFormat("yyyy-MM-dd")).format(results.getDate("date")),
                        (new SimpleDateFormat("hh:mm a")).format(results.getTime("time")),
                        results.getString("title"),
                        results.getString("description"),
                        results.getString("host_email")
                ));
            }
            return output;
        } catch(HandledIllegalValueException e){
            //unreachable branch
            assert false;
        }
        return Collections.emptyList();
    }

    public List<Participant> getParticipants(String eventId) throws SQLException {
        try {
            List<Participant> output = new ArrayList<Participant>();
            ResultSet results = conn.createStatement().executeQuery(
                    "SELECT * FROM participants WHERE event_id=\"" + eventId + "\""
            );
            while (results.next()) {
                output.add(Participant.create(
                        results.getString("id"),
                        eventId,
                        results.getString("name"),
                        results.getString("email")
                ));
            }
            return output;
        } catch(HandledIllegalValueException e){
            //unreachable
            assert false;
        }
        return Collections.emptyList();
    }

    public void addEvent(Event e) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO events(id, date, time, title, description, host_email) VALUES(?,?,?,?,?,?)"
        );
        pstmt.setString(1, e.uuid().toString());
        pstmt.setDate(2, Date.valueOf(e.eventDateTime().toLocalDate()));
        pstmt.setTime(3, Time.valueOf(e.eventDateTime().toLocalTime()));
        pstmt.setString(4, e.title());
        pstmt.setString(5, e.description());
        pstmt.setString(6, e.hEmail());

        pstmt.executeUpdate();
    }

    public void addParticipant(Participant p) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO participants(id, event_id, name, email) VALUES(?,?,?,?)"
        );
        pstmt.setString(1, p.uuid().toString());
        pstmt.setString(2, p.eventId().toString());
        pstmt.setString(3, p.name());
        pstmt.setString(4, p.email());

        pstmt.executeUpdate();
    }

    // for testing
    public void deleteAll() throws SQLException {
        conn.createStatement().execute(
            "DELETE FROM events; DELETE FROM participants;"
        );
    }


    public static void main(String[] args) {
        DbClient db;
        try {
            db = new DbClient();
            db.deleteAll();
            Event event = Event.create(
                "1234-12-12",
                "01:23 PM",
                "title",
                "description",
                "title@description.com"
            );
            Participant guy = Participant.create(
                event.uuid().toString(),
                "Luis Segovia",
                "luis@segovia.com"
            );
            db.addEvent(event);
            db.addParticipant(guy);
            System.out.println(db.getEvents());
            System.out.println(db.getParticipants(event.uuid().toString()));
            db.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
