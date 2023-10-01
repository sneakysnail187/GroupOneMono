import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.UUID;

public class GroupOneApp {

    private static class Event {
        private final UUID uuid;
        private String time;
        private String title;
        private String description;
        private String date;
        private String hEmail;

        public Event(UUID UID, String d, String tim, String titl, String descr, String mail){
            if(UID == null){uuid = UUID.randomUUID();}
            else{uuid = UID;}

            date = d;
            time = tim;
            title = titl;
            description = descr;
            hEmail = mail;
        }
    }

    public static List<String> getCommands(String fileName) throws FileNotFoundException{
        if(fileName == null) return new ArrayList<String>(0);

        File file = new File(fileName);
        if(! (file.exists() && file.canRead())) {
            System.err.println("Cannot access file! Non-existent or read access restricted");
            return new ArrayList<String>(0);
        }

        List<String> commandLines = new ArrayList<String>(32);
        Scanner scanner = new Scanner(file);
        while(scanner.hasNextLine()) {
            commandLines.add(scanner.nextLine());
        }
        scanner.close();
        return commandLines;
    }

    public static void processCmds(String[] cArgs) throws IOException, CloneNotSupportedException{
        if(cArgs == null || cArgs[0] == null || cArgs.length == 0){
            System.out.println("null command");
        }

        String command = cArgs[0];

        if(command.trim().equalsIgnoreCase("setState")){
            ArrayList<Character> c = new ArrayList<>(9);
            int index = 0;
            for(int i = 1; i < 4; i++){
                List<Character> temp = cArgs[i].chars().mapToObj((x) -> Character.valueOf((char)x)).collect(Collectors.toList());
                for(char t : temp){
                    c.add(index, t);
                    index++;
                }
            }
        }

        else if(command.trim().equalsIgnoreCase("printState")){
        }

        else if(command.trim().equalsIgnoreCase("move")){
        }

        else if(command.trim().equalsIgnoreCase("randomizeState")){
            int n = 0;

            n = Integer.parseInt(cArgs[1]);
            //p.randomizeState(n);
            //a.current.data.randomizeState(n);
            //b.root.data.randomizeState(n);
        }

        else if(command.trim().equalsIgnoreCase("solveAstar")){
            //new solveAstar(p, cArgs[1].trim());
        }

        else if(command.trim().equalsIgnoreCase("solveBeam")){
            int k = 0;
            
            k = Integer.parseInt(cArgs[1]);
            //b.solveBeam(k);
        }

        else if(command.trim().equalsIgnoreCase("maxNodes")){
            int m = 0;

            m = Integer.parseInt(cArgs[1]);
        }
    }

    public static void main(String[] args) throws Exception {

        Scanner input = new Scanner(System.in);
        String next;
        String[] one;
        
        do{
            System.out.println("Type \"load [filename]\" to fetch commands from a text file or \"stop\" to kill the program");
            next = input.nextLine();
            one = next.split(" ");
            String command = one[0];

            if(command.trim().equalsIgnoreCase("load")){
                List<String> cmdLines = getCommands(one[1]);
                for(String c : cmdLines){
                    String[] cArgs = c.split(" ");
                    processCmds(cArgs);
                }
            }

            else if(command.trim().equalsIgnoreCase("stop")){
                break;
            }

            else{
                processCmds(one);
            }
        } while(true);
        System.out.println("Program terminated");
        input.close();
    }
}
