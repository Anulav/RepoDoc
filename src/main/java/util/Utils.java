package util;

public class Utils {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static void printBanner(){
        System.out.println(new StringBuilder().append(ANSI_BLUE).append(" ____     ___  ____    ___   ___     ___      __ \n")
                .append("|    \\   /  _]|    \\  /   \\ |   \\   /   \\    /  ]\n")
                .append("|  D  ) /  [_ |  o  )|     ||    \\ |     |  /  / \n")
                .append("|    / |    _]|   _/ |  O  ||  D  ||  O  | /  /  \n")
                .append("|    \\ |   [_ |  |   |     ||     ||     |/   \\_ \n")
                .append("|  .  \\|     ||  |   |     ||     ||     |\\     |\n")
                .append("|__|\\_||_____||__|    \\___/ |_____| \\___/  \\____|\n")
                .append("                                            v 1.0       ").append(ANSI_RESET).toString());
    }

    public static void printError(String message){
        System.out.println(ANSI_RED+"Something went wrong: ");
        System.out.println(message+ANSI_RESET);
    }

    public static void outputFileLink(String filePath){
        System.out.println(ANSI_GREEN+"Your file's at: "+ filePath+ANSI_RESET);
    }

    public static final String BORDER = "<==========================================================>";
    public static void started(){
        System.out.println(BORDER);
        System.out.println(" Started Crawling");
        System.out.println(BORDER);
    }
    public static void stop(){
        System.out.println(BORDER);
        System.out.println(" Done. Exiting :) ");
        System.out.println(BORDER);
    }
    public static void stopErr(){
        System.out.println(BORDER);
        System.out.println("Sorry. Exiting :( ");
        System.out.println(BORDER);
    }
}
