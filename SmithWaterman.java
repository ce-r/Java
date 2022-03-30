import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


//Linear Gap Model: 
//Gap = -4
//Match = 5
//Mismatch = -4

class SmithWaterman {
    public static void printAdjMat(Result[][] adjMat){
        for(int i=0; i<adjMat.length; i++){
            for(int j=0; j<adjMat[0].length; j++){
                Integer v = adjMat[i][j].getCellValue();
                System.out.printf(String.format("%3d", v));
                //String a = adjMat[i][j].getArrow();
                //System.out.printf(String.format("%3s", a));
            }
            System.out.println();
        }
        System.out.println();
    }

    //the character arrow representation is used to do traceback on the scoring matrix
    //for the maximum path, always asking the UpLeft diag element which direction to go
    public static void printSeqsArrows(Result[][] scoreMatrix, String p, String q){
        Stack<String> subSeq1 = new Stack<String>(), subSeq2 = new Stack<String>();
        String[] shortList1 = new String[3], shortList2 = new String[3], shortList3 = new String[3];
        ArrayList<String[]> compVals = new ArrayList<>();
        int lasti = scoreMatrix.length-1;
        
        int triali = lasti, previ = triali-1;//sec to last row
        int currMax = Collections.max(new Result().getRowList(scoreMatrix[triali]));
        int prevMax = Collections.max(new Result().getRowList(scoreMatrix[previ]));
        
        while(triali>=0){//row with "max"
            if(currMax > prevMax)
                break;
            triali--; previ--;
            currMax = Collections.max(new Result().getRowList(scoreMatrix[triali]));
            prevMax = Collections.max(new Result().getRowList(scoreMatrix[previ]));
        }

        //skipping row means we add indel to col space seq
        //conversely, add indel to row space seq when skipping col
        int i = triali;
        int j = new Result().getRowList(scoreMatrix[triali]).indexOf(currMax);

        //ASSUMING MAX will be a matching PAIR, gives us first 
        //matching pair from the rightmost end of the seqs
        subSeq1.push(String.valueOf(p.charAt(i-1)));
        subSeq2.push(String.valueOf(q.charAt(j-1)));

        while(i>0 || j>0){//going up, left or diag 
            //System.out.println("index i: " + i + " " + "index j: " + j);
            int ii=0, jj=0;
            //UP-LEFT
            shortList1[0] = scoreMatrix[i-1][j-1].getArrow();
            shortList1[1] = String.valueOf(i-1); shortList1[2] = String.valueOf(j-1);
            compVals.add(shortList1);//0

            //LEFT
            shortList2[0] = scoreMatrix[i][j-1].getArrow();
            shortList2[1] = String.valueOf(i); shortList2[2] = String.valueOf(j-1);
            compVals.add(shortList2);//1

            //UP
            shortList3[0] = scoreMatrix[i-1][j].getArrow();
            shortList3[1] = String.valueOf(i-1); shortList3[2] = String.valueOf(j);
            compVals.add(shortList3);//2


            //compVals(Diag Elmt, Left-Col Elmt, Up-Row Elmt) -- the diag elemt is directing the path
            if(compVals.get(0)[0]=="UL"){//diag cell
                //                          (ith row of p)                                    (jth col of q)
                if(p.charAt(Integer.parseInt(compVals.get(0)[1])-1)==q.charAt(Integer.parseInt(compVals.get(0)[2])-1)){
                    subSeq1.push(String.valueOf(p.charAt(Integer.parseInt(compVals.get(0)[1])-1)));//p.charAt(prevRow-1)
                    subSeq2.push(String.valueOf(q.charAt(Integer.parseInt(compVals.get(0)[2])-1)));//q.charAt(prevCol-1)
                }
                else {
                    subSeq1.push("*" + String.valueOf(p.charAt(Integer.parseInt(compVals.get(0)[1])-1)) + "*");//diagonal cell but no match
                    subSeq2.push('*' + String.valueOf(q.charAt(Integer.parseInt(compVals.get(0)[2])-1)) + '*');
                }
                ii = Integer.parseInt(compVals.get(0)[1]); jj = Integer.parseInt(compVals.get(0)[2]);
            }
            else if(compVals.get(0)[0]=="L"){//prev col cell
                if(p.charAt(Integer.parseInt(compVals.get(1)[1])-1)==q.charAt(Integer.parseInt(compVals.get(1)[2])-1)){
                    subSeq1.push(String.valueOf(p.charAt(Integer.parseInt(compVals.get(1)[1])-1)));
                    //subSeq2.push(String.valueOf(q.charAt(Integer.parseInt(compVals.get(1)[2])-1)));
                }
                else {
                    subSeq1.push("-");
                    //subSeq2.push(String.valueOf(q.charAt(Integer.parseInt(compVals.get(1)[2])-1)));
                }
                subSeq2.push(String.valueOf(q.charAt(Integer.parseInt(compVals.get(1)[2])-1)));
                ii = Integer.parseInt(compVals.get(1)[1]); jj = Integer.parseInt(compVals.get(1)[2]);
            }
            else if(compVals.get(0)[0]=="U"){//prev row cell
                if(p.charAt(Integer.parseInt(compVals.get(2)[1])-1)==q.charAt(Integer.parseInt(compVals.get(2)[2])-1)){
                    //subSeq1.push(String.valueOf(p.charAt(Integer.parseInt(compVals.get(2)[1])-1)));
                    subSeq2.push(String.valueOf(q.charAt(Integer.parseInt(compVals.get(2)[2])-1)));
                }
                else {
                    //subSeq1.push(String.valueOf(p.charAt(Integer.parseInt(compVals.get(2)[1])-1)));
                    subSeq2.push("-");
                }
                subSeq1.push(String.valueOf(p.charAt(Integer.parseInt(compVals.get(2)[1])-1)));
                ii = Integer.parseInt(compVals.get(2)[1]); jj = Integer.parseInt(compVals.get(2)[2]);
            }
            i = ii; j = jj;
            compVals.clear();
        }

        System.out.print("seq1: ");
        while(!subSeq1.empty())
            System.out.print(subSeq1.pop() + " ");
        System.out.println();

        System.out.print("seq2: ");
        while(!subSeq2.empty())
            System.out.print(subSeq2.pop() + " ");
        System.out.println();
    }


    public static Result[][] sw(String X, String Y){
        int m = X.length();
        int n = Y.length();
        Result[][] res = new Result[m+1][n+1];

        for(int i = 0; i<res.length; i++)
            res[i][0] = new Result(0, "");

        for(int j = 1; j<res[0].length; j++)
            res[0][j] = new Result(0, "");

        int prevDiag;
        int prevRow;
        int prevCol; 
        int gap = -4;//-1;//
        int match = 5;//4;//
        int mismatch = -4;//-2;//
        String strArrow;
        Result resObj;
        List<Integer> lst = new ArrayList<>();

        for(int i=1; i<m+1; i++){
            for(int j=1; j<n+1; j++){
                prevDiag = res[i-1][j-1].getCellValue();
                prevRow = res[i-1][j].getCellValue() + gap;
                prevCol = res[i][j-1].getCellValue() + gap;

                if (X.charAt(i-1) == Y.charAt(j-1)){
                    prevDiag += match;
                }
                else {
                    prevDiag += mismatch;
                }
                lst.add(prevDiag);
                lst.add(prevRow);
                lst.add(prevCol);

                int max = Collections.max(lst);
                if(max == prevDiag) {
                    strArrow = "UL";//'\u2198';
                    resObj = new Result(max, strArrow);
                }
                else if(max == prevRow){
                    strArrow = "U";//'\u2193';
                    resObj = new Result(max, strArrow);
                }
                else {
                    strArrow = "L";//'\u2192';
                    resObj = new Result(max, strArrow);
                }
                
                if(max < 0)
                    resObj.setCellValue(0);
                res[i][j] = resObj;
                lst.clear();
            }
        }

        return res;
    }
      
    public static void main(String[] args){
        //String X = "ACCGGTCGACTGCGCGGAAGCCGGCCGAA";
        //String Y = "GTCGTTCGGAATGCCGTTGCTCTGTAAA";

        String P = "GCAGAGCACG";//"KVLEFGY";//
        String Q = "GCTGGAAGGCAT";//"EQLLKALEFKL";//
        //String Q = "EQLLKALEFKL";
        //sString P = "KVLEFGY";
        Result[][] r = sw(P, Q);//Q, P
        printAdjMat(r);
        printSeqsArrows(r, P, Q);
        //WE ASSUME ABSOLUTE MAX in matrix
    }
}
