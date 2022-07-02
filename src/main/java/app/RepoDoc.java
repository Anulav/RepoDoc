package app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import util.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RepoDoc {

    private static final DateTimeFormatter formatter;
    private static final Map<String, String> indexMap;
    private static final List<String> indexStrings;
    private static int pageNo;
    private static PDDocument newFile;
    private static PDType0Font font;
    private static List<String> ignored;
    private static List<String> ignoredFolder;
    private static boolean isAGitDirectory;
    private static String filePath, outputFolderPath, delimiter;
    private static URL pathToTTF;

    static {
        formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss").withZone(ZoneId.systemDefault());
        ;
        delimiter = "-----END OF FILE-----";
        indexMap = new HashMap<>();
        pageNo = 0;
        indexStrings = new ArrayList<>();
        isAGitDirectory = false;
    }


    public static void main(String[] args) throws IOException {
        try {
            Utils.printBanner();
            readConfig();
            Utils.started();
            newFile = new PDDocument();
            font = PDType0Font.load(newFile, new File(pathToTTF.toURI()));
            File inputFile = new File(filePath);
            initialize(inputFile);
            if (isAGitDirectory) {
                listAll(inputFile);
                printIndex();
                File outputFile = new File(outputFolderPath + "/" + inputFile.getName()
                        + "_" + formatter.format(Instant.now()) + ".pdf");
                newFile.save(outputFile);
                Utils.outputFileLink(outputFile.getPath());
                Utils.stop();
            } else {
                Utils.printError("Not a Git Repository");
                Utils.stopErr();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.printError(ex.getMessage());
            Utils.stopErr();
        }
    }

    public static void listAll(File inputFile) {
        if (inputFile.isFile()) {
            if (!fileCheck(inputFile)) {
                System.out.println(inputFile.getName() + "-> " + inputFile.getPath());
                addToIndex(inputFile, assignPageNoForIndex());
                try {
                    print(inputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return;
        } else {

            System.out.println(inputFile.getName() + "-> " + inputFile.getPath());
            addToIndex(inputFile, null);
        }
        for (File file : inputFile.listFiles()) {
            if (!file.isFile() && isIgnoredFolder(file)) {
                continue;
            }
            listAll(file);
        }
    }

    public static void print(File file) throws IOException {
        PDPage page = new PDPage();
        PDPageContentStream contentStream = new PDPageContentStream(newFile, page);
        contentStream.beginText();
        contentStream.setFont(font, 8);

        //Setting the leading
        contentStream.setLeading(14.5f);

        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        contentStream.showText("PageNo" + "-" + assignPageNo());
        contentStream.newLine();
        contentStream.showText("(" + file.getName() + ")");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            contentStream.newLine();
            while ((line = br.readLine()) != null) {
                if (count == 50) {
                    contentStream.endText();
                    contentStream.close();
                    newFile.addPage(page);
                    page = new PDPage();
                    contentStream = new PDPageContentStream(newFile, page);
                    contentStream.beginText();
                    contentStream.setFont(font, 8);
                    //Setting the leading
                    contentStream.setLeading(14.5f);
                    //Setting the position for the line
                    contentStream.newLineAtOffset(10, 780);
                    contentStream.showText("PageNo-" + assignPageNo());
                    contentStream.newLine();
                    count = 0;

                }
                contentStream.showText(line);
                contentStream.newLine();
                count++;
            }
            contentStream.showText(delimiter);
            contentStream.newLine();
            contentStream.endText();
            contentStream.close();
            newFile.addPage(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isIgnoredFile(File file) {
        for (String ignoredFiles : ignored) {
            if (file.getName().contains(ignoredFiles)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIgnoredFolder(File file) {
        for (String ignoredFiles : ignoredFolder) {
            if (file.getName().contains(ignoredFiles)) {
                return true;
            }
        }
        return false;
    }

    public static void addToIndex(File file, Integer pageNo) {
        String fileIndex = (file.isDirectory()) ? "[" + file.getName() + "]" : file.getName();
        String index = PushIntoIndex(file);
        String str;
        if (pageNo != null) {
            str = String.format("%-90s%5d", index + fileIndex, pageNo).replace(" ", ".")
                    .replace("*", " ");
        } else {
            str = String.format("%-90s", index + fileIndex).replace("*", " ");
        }
        indexStrings.add(str);
    }

    private static String getParentFolderName(File file) {
        String[] seps = file.getParent().split("\\\\");
        return seps.length > 1 ? seps[seps.length - 1] : "contextRoot";
    }

    public static void printIndex() throws IOException {
        PDPage page = new PDPage();
        PDPageContentStream contentStream = new PDPageContentStream(newFile, page);
        contentStream.beginText();
        contentStream.setFont(font, 12);

        //Setting the leading
        contentStream.setLeading(14.5f);

        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        contentStream.showText("PageNo-" + assignPageNo());
        contentStream.newLine();
        contentStream.setFont(font, 16);
        contentStream.showText("Index");
        contentStream.setFont(font, 12);
        contentStream.newLine();
        int count = 0;
        for (String indexLine : indexStrings) {
            contentStream.showText(indexLine);
            if (count == 45) {
                contentStream.endText();
                contentStream.close();
                newFile.addPage(page);
                page = new PDPage();
                contentStream = new PDPageContentStream(newFile, page);
                contentStream.beginText();
                contentStream.setFont(font, 12);
                //Setting the leading
                contentStream.setLeading(14.5f);
                //Setting the position for the line
                contentStream.newLineAtOffset(10, 780);
                contentStream.showText("PageNo-" + assignPageNo());
                contentStream.newLine();
                count = 0;

            }
            contentStream.newLine();
            count++;
        }
        contentStream.showText(delimiter);
        contentStream.newLine();
        contentStream.endText();
        contentStream.close();
        newFile.addPage(page);
    }

    public static int assignPageNo() {
        return ++pageNo;
    }

    public static int assignPageNoForIndex() {
        return pageNo + 1;
    }

    public static PDPageContentStream addNewPage(PDPageContentStream contentStream, PDPage page) throws IOException {
        contentStream.endText();
        contentStream.close();
        newFile.addPage(page);
        page = new PDPage();
        contentStream = new PDPageContentStream(newFile, page);
        contentStream.beginText();
        contentStream.setFont(font, 8);
        //Setting the leading
        contentStream.setLeading(14.5f);
        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        contentStream.showText(delimiter + assignPageNo());
        contentStream.newLine();
        return contentStream;
    }

    public static void initialize(File file) {
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory() && files[i].getName().equals(".git")) {
                    isAGitDirectory = true;
                    return;
                }
            }
        }
    }

    public static void readConfig() throws IOException, URISyntaxException {
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream("conf.properties");
        pathToTTF = RepoDoc.class.getClassLoader().getResource("GNU-Unifont-Full/unifont-14_0_04.ttf");

        Properties p = new Properties();
        p.load(input);
        ignoredFolder = Arrays.asList(p.getProperty("IgnoredFolder").split(","));
        ignored = Arrays.stream(p.getProperty("IgnoredFilesExtension").split(",")).map(e -> "." + e).toList();
        filePath = p.getProperty("gitFolderPath");
        outputFolderPath = p.getProperty("outputFolderPath");
        System.out.println("File extensions ignored: " + ignored);
        System.out.println("Folders to be ignored: " + ignoredFolder);
    }

    public static boolean fileCheck(File file) {
        String fileName = file.getName();
        for (String extension : ignored) {
            if (fileName.contains(extension))
                return true;
        }
        return false;
    }

    public static String PushIntoIndex(File file) {
        String parentIndex = indexMap.get(file.getParent());
        if (parentIndex == null) { //levelOne
            parentIndex = "+";
            indexMap.put(file.getParent(), parentIndex);

        }
        insertSelfEntryIntoIndexMap(file, parentIndex);
        return indexMap.get(file.getPath());
    }

    public static void insertSelfEntryIntoIndexMap(File file, String parentIndex) {
        String pairString = parentIndex;
        pairString = pairString.substring(0, pairString.length() - 1) + "|";
        if (file.isFile()) {
            pairString += "--";
        } else {
            pairString += "*+";
        }
        indexMap.put(file.getParent(), parentIndex);
        indexMap.put(file.getPath(), pairString);
    }
}

