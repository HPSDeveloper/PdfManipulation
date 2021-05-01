package ch.hps.pdf.main;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.*;

public class SplitPdf {
    private static String DATE_PATTERN = "yyyy.MM.dd";
    private static String BASE_DIR = "C:\\tmp\\zahlungen\\";

    public static void main(String[] args) throws IOException {

        String dateInString =new SimpleDateFormat(DATE_PATTERN).format(new Date());

        //Loading an existing PDF document
        File file = new File(BASE_DIR + "IMG_20210501_0001.pdf");
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
        List<SplitDef> splitDefs = ImmutableList.of(new SplitDef("Busse", 1), new SplitDef("Abo Klaex", 3), new SplitDef("Musikschule Kristin", 4));
        convertToEndPageNuberRefs(splitDefs, size);
        return splitDefs;
    }

    private static void convertToEndPageNuberRefs(List<SplitDef> splitDefs, int size) {
        for (int i = 0; i < splitDefs.size(); i++) {
            splitDefs.get(i).setPageIdx((i + 1) < splitDefs.size() ? splitDefs.get(i + 1).getPageIdx() - 1 : size);
        }
    }

    private static class SplitDef {
        public String getDocName() {
            return docName;
        }

        public int getPageIdx() {
            return pageIdx;
        }

        private String docName;

        public void setPageIdx(int pageIdx) {
            this.pageIdx = pageIdx;
        }

        private int pageIdx;

        public SplitDef(String docName, int pageIdx) {
            this.docName = docName;
            this.pageIdx = pageIdx;
        }


    }
}

