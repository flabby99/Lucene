package modify;

import java.io.*;

public class CranRel{
    public static void main(String args[]) throws IOException {
        //this will close the resources automatically
        //even if an exception rises
        if(args.length != 2) {
            System.out.println("Usage is: cranrel_input_location cranrel_output_location");
            System.exit(-1);
        }
        StringBuilder stringBuilder;
        try (BufferedReader fr = new BufferedReader(new FileReader(args[0]));
             BufferedWriter fw = new BufferedWriter(new FileWriter(args[1]))) {
            String line;
            while((line = fr.readLine()) != null) {
                int offset = line.indexOf(' ') + 1;
                stringBuilder = new StringBuilder(line).insert(offset, "0 ");
                fw.write(stringBuilder.toString());
                fw.newLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
