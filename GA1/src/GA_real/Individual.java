/*
 * Created by dev on: 15.04.2020
 */
package GA_real;

/**
 *
 * @author abdul
 */
public class Individual {
    
    public String[] gene;
    public int NumRules;
    public int condLength;
    public int fitness = 0;
    public Rule[] rulebase;
    

    public Individual() {
    }

    public Individual(int geneSize, int NumRules, int condLength) {
        this.gene = new String[geneSize];
        this.NumRules = NumRules;
        this.condLength = condLength;
    }

    public Individual(Individual individual) {
        this.gene = individual.gene;
        this.NumRules = individual.NumRules;
        this.condLength = individual.condLength;
        this.rulebase = individual.rulebase;
        this.fitness = individual.fitness;
    }
    
    public void generateRulebase() {
        int fitness = 0;
        int k = 0;

         this.rulebase = new Rule[NumRules];
        for (int i = 0; i < NumRules; i++) {
            rulebase[i] = new Rule(condLength);
            for (int j = 0; j < condLength; j++) {
                rulebase[i].cond[j] = gene[k++];        
            }
            rulebase[i].output = gene[k++];
        }
    }

    public int getFitness() {
        return fitness;
    }

    public String[] getGene() {
        return gene;
    }

    public Rule[] getRulebase() {
        return rulebase;
    }
    
    

    
    
    
}
