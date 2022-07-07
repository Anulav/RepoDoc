package repodoc;

 public class Utils {
     private static final String ANSI_BLUE = "\u001B[34m";
     private static final String ANSI_RESET = "\u001B[0m";
     private static final String ANSI_RED = "\u001B[31m";
     private static final String ANSI_GREEN = "\u001B[32m";

     static void printBanner(){
        System.out.println(new StringBuilder().append(ANSI_BLUE).append(" ____     ___  ____    ___   ___     ___      __ \n")
                .append("|    \\   /  _]|    \\  /   \\ |   \\   /   \\    /  ]\n")
                .append("|  D  ) /  [_ |  o  )|     ||    \\ |     |  /  / \n")
                .append("|    / |    _]|   _/ |  O  ||  D  ||  O  | /  /  \n")
                .append("|    \\ |   [_ |  |   |     ||     ||     |/   \\_ \n")
                .append("|  .  \\|     ||  |   |     ||     ||     |\\     |\n")
                .append("|__|\\_||_____||__|    \\___/ |_____| \\___/  \\____|\n")
                .append("                                            v 1.0       ").append(ANSI_RESET).toString());
    }

     static void printError(String message){
        System.out.println(ANSI_RED+"Something went wrong: ");
        System.out.println(message+ANSI_RESET);
    }

     static void outputFileLink(String filePath){
        System.out.println(ANSI_GREEN+"Your file's at: "+ filePath+ANSI_RESET);
    }

     static final String BORDER = "<==========================================================>";
     static void started(){
        System.out.println(BORDER);
        System.out.println(" Started Crawling");
        System.out.println(BORDER);
    }
     static void stop(){
        System.out.println(BORDER);
        System.out.println(" Done. Exiting :) ");
        System.out.println(BORDER);
    }
     static void stopErr(){
        System.out.println(BORDER);
        System.out.println("Sorry. Exiting :( ");
        System.out.println(BORDER);
    }
}
