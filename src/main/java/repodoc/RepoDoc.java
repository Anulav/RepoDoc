package repodoc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.*;
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
    private static PDDocument outputPDF;
    private static PDType0Font font;
    private static List<String> ignored;
    private static List<String> ignoredFolder;
    private static boolean isAGitDirectory;
    private static String filePath, outputFolderPath, END_OF_FILE, START_OF_FILE;
    private static URL pathToTTF;
    private static List<PDPage> contentPages, indexPages;//Separate Lists for making Index @front.
    private static List<Bookmarks> bookmarkPages;

    static {
        formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss").withZone(ZoneId.systemDefault());
        END_OF_FILE = "-----END OF FILE-----";
        START_OF_FILE = "-----START OF FILE-----";
        indexMap = new HashMap<>();
        pageNo = 0;
        indexStrings = new ArrayList<>();
        isAGitDirectory = false;
        contentPages = new ArrayList<>();
        indexPages = new ArrayList<>();
        bookmarkPages = new ArrayList<>();
    }


     public static void main(String[] args) {
        try {
            Utils.printBanner();
            readConfig();
            Utils.started();
            outputPDF = new PDDocument();
            PDDocumentOutline documentOutline = new PDDocumentOutline();
            outputPDF.getDocumentCatalog().setDocumentOutline(documentOutline);
            font = PDType0Font.load(outputPDF, new File(pathToTTF.toURI()));
            File inputFile = new File(filePath);
            initialize(inputFile);
            if (isAGitDirectory) {
                listAll(inputFile);
                printIndex();
                String fileName = inputFile.getName();
                File outputFile = new File(outputFolderPath + "/" + fileName
                        + "_" + formatter.format(Instant.now()) + ".pdf");
                addAllPages(indexPages, contentPages);
                addBookMarks(fileName, outputPDF, indexPages.size());
                outputPDF.save(outputFile);
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

    private static void addBookMarks(String fileName, PDDocument newFile, int offset) {
        PDDocumentOutline documentOutline = new PDDocumentOutline();
        newFile.getDocumentCatalog().setDocumentOutline(documentOutline);
        PDOutlineItem pagesOutline = new PDOutlineItem();
        pagesOutline.setTitle(fileName);
        documentOutline.addLast(pagesOutline);
        offset -= 1;
        // Collections.sort(bookmarkPages);
        for (Bookmarks singleBookmark : bookmarkPages) {
            PDPageDestination pageDestination = new PDPageFitWidthDestination();
            int pageNoPlusOffset = singleBookmark.getPageNo() + offset;
            pageDestination.setPage(newFile.getPage(pageNoPlusOffset));

            PDOutlineItem bookmark = new PDOutlineItem();
            bookmark.setDestination(pageDestination);
            bookmark.setTitle(singleBookmark.getNameOfPage());
            pagesOutline.addLast(bookmark);
        }
        pagesOutline.openNode();
        documentOutline.openNode();
        newFile.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);
    }

    private static void addAllPages(List<PDPage> indexPages, List<PDPage> contentPages) {
        indexPages.forEach(outputPDF::addPage);
        contentPages.forEach(outputPDF::addPage);
    }

     private static void listAll(File inputFile) {
        if (inputFile.isFile()) {
            if (!fileCheck(inputFile)) {
                System.out.println(inputFile.getName() + "-> " + inputFile.getPath());
                Integer pageNo = assignPageNoForIndex();
                addToIndex(inputFile, pageNo);
                bookmarkPages.add(new Bookmarks(inputFile.getName(), pageNo));
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

     private static void print(File file) throws IOException {
        PDPage page = new PDPage();

        PDPageContentStream contentStream = new PDPageContentStream(outputPDF, page);
        contentStream.beginText();
        contentStream.setFont(font, 8);

        //Setting the leading
        contentStream.setLeading(14.5f);

        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        contentStream.showText("PageNo" + "-" + assignPageNo());
        contentStream.newLine();
        contentStream.showText("<<" + file.getName() + ">>");
        contentStream.newLine();
        contentStream.showText(START_OF_FILE);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            contentStream.newLine();
            while ((line = br.readLine()) != null) {
                if (count == 50) {
                    contentStream.endText();
                    contentStream.close();
                    //outputPDF.addPage(page);
                    contentPages.add(page);
                    page = new PDPage();
                    contentStream = new PDPageContentStream(outputPDF, page);
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
            contentStream.showText(END_OF_FILE);
            contentStream.newLine();
            contentStream.endText();
            contentStream.close();
            //outputPDF.addPage(page);
            contentPages.add(page);
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

     private static void addToIndex(File file, Integer pageNo) {
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

     private static void printIndex() throws IOException {
        PDPage page = new PDPage();
        PDPageContentStream contentStream = new PDPageContentStream(outputPDF, page);
        contentStream.beginText();
        contentStream.setFont(font, 12);

        //Setting the leading
        contentStream.setLeading(14.5f);

        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        // contentStream.showText("PageNo-" + assignPageNo());
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
                //outputPDF.addPage(page);
                indexPages.add(page);
                page = new PDPage();
                contentStream = new PDPageContentStream(outputPDF, page);
                contentStream.beginText();
                contentStream.setFont(font, 12);
                //Setting the leading
                contentStream.setLeading(14.5f);
                //Setting the position for the line
                contentStream.newLineAtOffset(10, 780);
                contentStream.newLine();
                count = 0;

            }
            contentStream.newLine();
            count++;
        }
        contentStream.showText(END_OF_FILE);
        contentStream.newLine();
        contentStream.endText();
        contentStream.close();
        indexPages.add(page);
    }

     private static int assignPageNo() {
        return ++pageNo;
    }

     private static int assignPageNoForIndex() {
        return pageNo + 1;
    }

     private static PDPageContentStream addNewPage(PDPageContentStream contentStream, PDPage page) throws IOException {
        contentStream.endText();
        contentStream.close();
        outputPDF.addPage(page);
        page = new PDPage();
        contentStream = new PDPageContentStream(outputPDF, page);
        contentStream.beginText();
        contentStream.setFont(font, 8);
        //Setting the leading
        contentStream.setLeading(14.5f);
        //Setting the position for the line
        contentStream.newLineAtOffset(10, 780);
        contentStream.showText(END_OF_FILE + assignPageNo());
        contentStream.newLine();
        return contentStream;
    }

     private static void initialize(File file) {
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (File value : files) {
                if (value.isDirectory() && value.getName().equals(".git")) {
                    isAGitDirectory = true;
                    return;
                }
            }
        }
    }

     private static void readConfig() throws IOException {
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

     private static boolean fileCheck(File file) {
        String fileName = file.getName();
        for (String extension : ignored) {
            if (fileName.contains(extension))
                return true;
        }
        return false;
    }

     private static String PushIntoIndex(File file) {
        String parentIndex = indexMap.get(file.getParent());
        if (parentIndex == null) { //levelOne
            parentIndex = "+";
            indexMap.put(file.getParent(), parentIndex);

        }
        selfEntryIntoIndexMap(file, parentIndex);
        return indexMap.get(file.getPath());
    }

     private static void selfEntryIntoIndexMap(File file, String parentIndex) {
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

