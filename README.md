# PdfManipulation
Codes that should help with PDF manipulation tasks

## Feature 1: Split a given PDF file at given locations

Split up a given PDF file at given page numbers.
In order to do so: Place the source PDF into the 'in' directory. 
Create a *'split definition' file* with the same base name as the PDF but extension ".txt"
Within this file write one line for each targeted PDF split section. 
The line starts with the desired extracted sections name followed by a ";" then followed by the start page of the extracted section within the source PDF file.
All lines must follow this format. No empty lines allowed.

