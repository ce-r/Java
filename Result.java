import java.util.ArrayList;

class Result {
    int val;
    String arrow;
    ArrayList<Integer> rowList = new ArrayList<Integer>();

    public Result(){}

    public Result(int cellValue, String direction) {
        this.val = cellValue;
        this.arrow = direction;
    }

    @Override
    public String toString() {
        return this.val + " " + this.arrow;
    }
    public int getCellValue(){
        return this.val;
    }
    public String getArrow(){
        return this.arrow;
    }
    public ArrayList<Integer> getRowList(Result[] arrOfResObj){
        for(Result r : arrOfResObj)
            rowList.add(r.getCellValue());
        return this.rowList;
    }
    public void setCellValue(int newCV){
        this.val = newCV;
    }
    public void setArrow(String newDir){
        this.arrow = newDir;
    }
}
