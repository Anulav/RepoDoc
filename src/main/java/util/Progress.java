package util;

public class Progress {
    public static final String BORDER = "<==========================================================>";
    public static void started(){
        System.out.println(BORDER);
        System.out.println("Started Crawling");
        System.out.println(BORDER);
    }
    public static void stop(){
        System.out.println(BORDER);
        System.out.println("Done. Exiting :) ");
        System.out.println(BORDER);
    }
    public static void stopErr(){
        System.out.println(BORDER);
        System.out.println("Sorry. Exiting :( ");
        System.out.println(BORDER);
    }
}
