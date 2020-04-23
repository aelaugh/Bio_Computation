/*
 * Created by dev on: 14.04.2020
 */
package GA_real;

/**
 *
 * @author abdul
 */
public class Data {
    public int size;
    public int[] variables;
    public int output;

    public Data(int size) {
        this.size = size;
        this.variables = new int[size];
    }

    public void setOutput(int output) {
        this.output = output;
    }

    public int getOutput() {
        return output;
    }

    public int[] getVariables() {
        return variables;
    }
    
   
    public String printVariables() {
        String str = "";
        for (int gene : variables) {
            str = str + gene;
        }
        return str;
    }

}
