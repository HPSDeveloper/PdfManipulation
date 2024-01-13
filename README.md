# PdfManipulation
Programs that should help with PDF manipulation tasks

## Feature 1: Split a given PDF file at given locations

### Usage:

Copy the resulting Jar file (you can use that one in the 'product' folder) into a directory with PDF files and double click it.
Result: Each single page of all PDF files in that folder will be copied into a sub folder 'splittedPdfs'.

If you don't want to split the PDF between every page but at dedicated pages, then place a file with the same name as the source PDF, but with name extension 'txt' instead of 'pdf', into the base folder.
Each line of that TXT file must contain a text and a number separated by a ';'. The text is the file name part, that will be given to the extracted PDF and the number is the starting page of the extraction.
The ending page of the extraction is defined by the next line. (You have to define a subsequent pdf extract in order to exclude those pages from your preceding result document.)


