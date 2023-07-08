package ch.hps.pdf.main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class SplitPdf {
    private static String DATE_PATTERN = "yyyy.MM.dd";
    private static String BASE_DIR = "C:\\tmp\\zahlungen\\";
    private static String IN_FILE_NAME = "IMG_20210501_0001.pdf";

    public static void main(String[] args) throws IOException {

        String dateInString = new SimpleDateFormat(DATE_PATTERN).format(new Date());

        //Loading an existing PDF document
        File file = new File(BASE_DIR + IN_FILE_NAME);
        PDDocument doc = PDDocument.load(file);

        //Instantiating Splitter class
        Splitter splitter = new Splitter();

        //splitting the pages of a PDF document
        List<PDDocument> pages = splitter.split(doc);
        System.out.println("PDF splitted");

        List<SplitDef> splitDefs = getSplitMap(pages.size());
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
            File file1 = new File(BASE_DIR + "out\\"  + dateInString + "_" + mergedDocCntr + "_" + splitDef.getDocName() + ".pdf");
            file1.createNewFile();
            document.save(BASE_DIR + "out\\" + dateInString + "_"  + mergedDocCntr + "_" + splitDef.getDocName()  + ".pdf");
            mergedDocCntr++;
        }
        System.out.println("Documents merged");

    }

    private static List<SplitDef> getSplitMap(int size) {
        List<SplitDef> splitDefs = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(BASE_DIR + replaceExtension(IN_FILE_NAME, "txt")))) {
            stream.forEach(line -> {
                System.out.println(line);
                splitDefs.add(SplitDef.fromString(line));
            });
        } catch (IOException e) {
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

