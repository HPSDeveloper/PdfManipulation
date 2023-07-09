package ch.hps.pdf.main;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Reads all PDF files in the current working directory and writes ist pages to different
 * new files into the working directories 'splittedPdfs' subfolder.
 * If for such source PDF with the same base name a txt file exists, that defines lines like
 * <Target PDF Name>;<Start_Page_Nbr>
 * then this definition as start number and target PDF file name (extended).
 */
public class SplitPdf {
    private static String DATE_PATTERN = "yyyyMMddhhmmss";
    private static String BASE_DIR = null;
    private static String OUT_DIR_NAME = "splittedPdfs";

    public static void main(String[] args) throws IOException {
        BASE_DIR = System.getProperty("user.dir");
        Files.createDirectories(Paths.get(BASE_DIR + "/" + OUT_DIR_NAME));
        File[] pdfsToSplit = getPdfsToSplitFromCurrentDirectory();
        for(File pdfToSplit : pdfsToSplit){
            splitFile(pdfToSplit);
        }
    }

    private static void splitFile(File pdfToSplit) throws IOException {
        String dateInString = new SimpleDateFormat(DATE_PATTERN).format(new Date());

        //Loading an existing PDF document
        PDDocument doc = PDDocument.load(pdfToSplit);

        //Instantiating Splitter class
        Splitter splitter = new Splitter();

        //splitting the pages of a PDF document
        List<PDDocument> pages = splitter.split(doc);
        System.out.println("PDF splitted");

        List<SplitDef> splitDefs = getSplitMap(pages.size(), pdfToSplit);
        int pageNbr=1;
        int mergedDocCntr=1;
        for(SplitDef splitDef : splitDefs){
            //Instantiating PDFMergerUtility class
            PDFMergerUtility mergerUtility = new PDFMergerUtility();

            PDDocument document = new PDDocument();
            while (pageNbr <= splitDef.getPageIdx()){
                //adding the source files
                mergerUtility.appendDocument(document, pages.get(pageNbr - 1 ));
                pageNbr++;
            }
            mergerUtility.mergeDocuments();
            File outFile = new File(BASE_DIR + "/" + OUT_DIR_NAME + "/"  + pdfToSplit.getName().replaceAll("\\.pdf$", "") + "_" + dateInString + "_" + mergedDocCntr + "_" + splitDef.getDocName() + ".pdf");
            outFile.createNewFile();
            document.save(outFile.getAbsolutePath());
            mergedDocCntr++;
        }
        System.out.println("Documents merged");
    }

    private static File[] getPdfsToSplitFromCurrentDirectory() throws IOException {
        File dir = new File(".");
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".pdf"); // && Files.exists(Paths.get(replaceExtension(dir + "/" + name, "txt")));
            }
        });

    }

    private static List<SplitDef> getSplitMap(int size, File pdfToSplit) {
        List<SplitDef> splitDefs = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get( replaceExtension(pdfToSplit.getAbsolutePath(), "txt")))) {
            stream.forEach(line -> {
                System.out.println(line);
                splitDefs.add(SplitDef.fromString(line));
            });
        } catch (NoSuchFileException e) {
            for(int i = 1; i <= size; i++){
                splitDefs.add(SplitDef.fromString("Page_" + i + ";" + i));
            }
        }catch (IOException e){
            e.printStackTrace();
        }

//        List<SplitDef> splitDefs = ImmutableList.of(new SplitDef("Busse", 1), new SplitDef("Abo Klaex", 3), new SplitDef("Musikschule Kristin", 4));
        convertToEndPageNuberRefs(splitDefs, size);
        return splitDefs;
    }

    private static String replaceExtension(String inFileName, String extension) {
        return com.google.common.io.Files.getNameWithoutExtension(inFileName) + "." + extension;
    }

    private static void convertToEndPageNuberRefs(List<SplitDef> splitDefs, int size) {
        for (int i = 0; i < splitDefs.size(); i++) {
            splitDefs.get(i).setPageIdx((i + 1) < splitDefs.size() ? splitDefs.get(i + 1).getPageIdx() - 1 : size);
        }
    }

    /**
     * Defines the page number within the source document where the extraction into the new document should start
     * plus the title (extended) of the new document.
     */
    private static class SplitDef {
        String getDocName() {
            return docName;
        }

        int getPageIdx() {
            return pageIdx;
        }

        private String docName;

        void setPageIdx(int pageIdx) {
            this.pageIdx = pageIdx;
        }

        private int pageIdx;

        SplitDef(String docName, int pageIdx) {
            this.docName = docName;
            this.pageIdx = pageIdx;
        }

        static SplitDef fromString(String s){
            String[] nameAndIndex = s.split(";");
            if(nameAndIndex.length == 2){
                try {
                    return new SplitDef(nameAndIndex[0], Integer.valueOf(nameAndIndex[1]));
                }catch(NumberFormatException ex){
                    throw new RuntimeException("ERROR: The split config TXT file must contain have format '<section name>;<page number>'", ex);
                }
            }
            throw new RuntimeException("ERROR: The split config TXT file must contain have format '<section name>;<page number>'");
        }
    }
}

