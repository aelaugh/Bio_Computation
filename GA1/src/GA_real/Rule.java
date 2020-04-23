/*
 * Created by dev on: 15.04.2020
 */
package GA_real;


/**
 *
 * @author abdul
 */
public class Rule {
    public int condLength;
    public String[] cond;
    public String output;

    public Rule(int condLength) {
        this.condLength = condLength;
        this.cond = new String[condLength];
        this.output = "";
    }

    public String[] getCond() {
        return cond;
    }
    
    
}
