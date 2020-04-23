/*
 * Created by dev on: 13.04.2020
 */
package GA_Bin;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author abdul
 */
public class Main {
    
    public static File file;
    public static FileWriter fileWriter;
    
    //you can adjust the algorithm by changing the following parameters

    public static final int POP_SIZE = 100; //Population
    public static final int GENERATION = 100; //Number of generations the GA needs to run
    public static final double MUTATION_RATE = 0.01; //Mutation rate

    //Number of rules to be generated
    public static final int NUM_RULES = 10;

    //Both updated once the data is loaded from the datasets
    public static int COND_LEN = 0; //length of the conditions in each dataset
    public static int GENE_SIZE = 0; //size of the genes for every solution
    public static Individual fittestIndividual; //save the individual fitness
    public static Individual currentBest; //he best individual fitness
    
    //Used to store the perfomance hostory for the fittest individual
    public static int[] currentBestArray = new int[GENERATION];
    public static int[] fittestArray = new int[GENERATION];
    
    //Use to calculate the average
    public static int[] averageArray = new int[GENERATION];
    public static int average;
    
    //To save the population adn the matting pool in each generation
    public static Individual[] population;
    public static Individual[] matingPool;
    
    //list of bits that is going to be used
    public static ArrayList<String> bits = new ArrayList(Arrays.asList("0", "1", "#"));
    public static ArrayList<Data> dataSet;

    public static void main(String[] args) {
        /*Change the filename depending on the dataset being used (data1.txt or 
        data2.txt*/
        String fileName = "data1.txt";
        
        
        //Load the data from the dataset file
        dataSet = getDataSet(fileName);
        
        //Setting up the output file
        String outputFileName = "";
        if (fileName.indexOf(".") > 0) {
            outputFileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        
        createFile(outputFileName+".csv");
        
        //log the time the algorithm started
        long startTime = System.currentTimeMillis();
        
        //Placeholder of the fittest individual
        fittestIndividual = new Individual(GENE_SIZE, NUM_RULES, COND_LEN);
        fittestIndividual.generateRulebase();   
        
        //Setup the mating pool
        matingPool = setupArray(POP_SIZE, NUM_RULES, GENE_SIZE, COND_LEN);
        
        //used to run the GA multiple times without doing it manually
        //set to 1 because we want to run the GA once
        for (int a = 0; a < 1; a++) {
            currentBest = new Individual(GENE_SIZE, NUM_RULES, COND_LEN);
            currentBest.generateRulebase();

            //Generate a population
            generatePopulation();

            //Initial calculation of the fitness
            calculateFitness(population);

            for (int i = 0; i < GENERATION; i++) {
                average = 0; //reset the average
                currentBestArray[i] = currentBest.getFitness();
                
                tournamentSelect(population);

                //Calculate the fitness of the mating pool
                calculateFitness(matingPool);

                //Perfrom crossover on the mating pool
                crossover(matingPool);

                //Mutation
                mutation(matingPool);

                //Calculate the fitness of the mating pool
                calculateFitness(matingPool);

                //Evaluate the current population
                for (Individual individual : matingPool) {
                    if (individual.getFitness() > currentBest.getFitness()) {
                        currentBest = new Individual(individual);
                    }
                    average = average + individual.getFitness();
                }
                

                //Replace the population with the new generation
                for (int j = 0; j < POP_SIZE; j++) {
                    population[j] = new Individual(matingPool[j]);
                }
                
                averageArray[i] = average / POP_SIZE;
            }
            
            //Check whether the current best is fitter than the global fittest individual
            if (currentBest.fitness > fittestIndividual.fitness) {
                fittestIndividual = new Individual(currentBest);
                //copy the fittest individuals performance
                fittestArray = Arrays.copyOf(currentBestArray, currentBestArray.length);
            }
        }

        System.out.println("Best fitness generated for the dataset: " + fittestIndividual.fitness);
        
        //output the rules
        printRules(fittestIndividual.getRulebase());
        
        //Save the performance data into the output file
        for (int i = 0; i < fittestArray.length; i++) {
            addToFile(i, fittestArray[i], averageArray[i]);
        }
        
        //close the file
        close();
        
        //Evaluate the fitness agaisnt the dataset
        calculateFitness(fittestIndividual);
        double accuracyPercentage = ((double) 100 / dataSet.size()) * fittestIndividual.fitness;
        System.out.format("%.2f%% accuracy on the datase set.\n", accuracyPercentage);
        
        //Log the duration it took
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed in " + String.format("%02d min, %02d.%02d sec",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
                TimeUnit.MILLISECONDS.toMillis(duration) - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration))
        ));
    }
    
    //Function to generate the population
    public static void generatePopulation() {
        population = setupArray(POP_SIZE, NUM_RULES, GENE_SIZE, COND_LEN);

        for (Individual individual : population) {
            for (int i = 0; i < individual.getGene().length; i++) {
                individual.gene[i] = bits.get(new Random().nextInt(2));
            }
            individual.generateRulebase();
        }
    }
    
    //Function to print the final rulebase
    public static void printRules(Rule[] rules) {
        System.out.println("\nRulebase generated\n------------------");
        int i =1;
        for (Rule rule : rules) {
            String cond = "";
            for (int j = 0; j < rule.cond.length; j++) {
                cond = cond + rule.cond[j];
            }
            String output = rule.output;
            System.out.println("Rule" + i + " : " + cond + " = " + output);
            i++;
        }
        System.out.println("\n");
    }

    //Function to calculate the fitness
    public static void calculateFitness(Individual[] individuals) {
        for (Individual individual : individuals) {
            individual.fitness = 0;
            for (int i = 0; i < dataSet.size(); i++) {
                for (Rule rule : individual.rulebase) {
                    boolean match = true;
                    int[] data = dataSet.get(i).getVariables();

                    for (int j = 0; j < data.length; j++) {

                        String variable = String.valueOf(data[j]);
                        //String variable = "" + data[j];
                        String[] rulebase = rule.cond;

                        if ((rulebase[j].equals(variable) != true) && (rulebase[j].equals("#") != true)) {
                            match = false;
                        }
                    }

                    if (match) {
                        String output = String.valueOf(dataSet.get(i).getOutput());
                        if (rule.output.equals(output)) {
                            individual.fitness++;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    //Overloading the funtion to evaluate the accuracy
    public static void calculateFitness(Individual individual) {
        individual.fitness = 0;
        for (int i = 0; i < dataSet.size(); i++) {

            for (Rule rule : individual.rulebase) {
                boolean match = true;
                int[] data = dataSet.get(i).getVariables();

                for (int j = 0; j < data.length; j++) {

                    String variable = String.valueOf(data[j]);
                    //String variable = "" + data[j];
                    String[] rulebase = rule.cond;

                    if ((rulebase[j].equals(variable) != true) && (rulebase[j].equals("#") != true)) {
                        match = false;
                    }
                }

                if (match) {
                    String output = String.valueOf(dataSet.get(i).getOutput());
                    if (rule.output.equals(output)) {
                        individual.fitness++;
                    }
                    break;
                }
            }
        }
    }

    //Function that perform single point crossover
    public static void crossover(Individual[] parent) {
        Individual[] children = new Individual[POP_SIZE];
        for (int i = 0; i < POP_SIZE; i += 2) {
            Individual child1 = new Individual(GENE_SIZE, NUM_RULES, COND_LEN);
            child1.generateRulebase();
            Individual child2 = new Individual(GENE_SIZE, NUM_RULES, COND_LEN);
            child2.generateRulebase();

            int crossOverPoint = new Random().nextInt(GENE_SIZE);
            for (int j = 0; j < crossOverPoint; j++) {
                child1.gene[j] = parent[i].gene[j];
                child2.gene[j] = parent[i + 1].gene[j];
            }

            for (int j = crossOverPoint; j < GENE_SIZE; j++) {
                child1.gene[j] = parent[i + 1].gene[j];
                child2.gene[j] = parent[i].gene[j];
            }

            child1.generateRulebase();
            child2.generateRulebase();
            children[i] = new Individual(child1);
            children[i + 1] = new Individual(child2);
        }

        matingPool = Arrays.copyOf(children, parent.length);
    }

    //Funtion to perfrom mutation on the individuals depending on the mutation rate set
    public static void mutation(Individual[] individuals) {
        for (int i = 0; i < POP_SIZE; i++) {
            for (int j = 0; j < GENE_SIZE; j++) {
                double randomNum = Math.random();
                if (randomNum < MUTATION_RATE) {
                    randomNum = Math.random();
                    ArrayList<String> bitsList = (ArrayList<String>) bits.clone();
                    for (int g = 0; g < bitsList.size(); g++) {
                        if (individuals[i].gene[j].equals(bitsList.get(g))) {
                            String bit = bitsList.get(g);
                            bitsList.remove(g);
                            int index;
                            if (randomNum < 0.5) {
                                index = 0;
                            } else {
                                index = 1;
                            }
                            individuals[i].gene[j] = bitsList.get(new Random().nextInt(2));
                            bitsList.add(bit);
                        }
                    }
                }
            }
            individuals[i].generateRulebase();
        }
        matingPool = Arrays.copyOf(individuals, individuals.length);
    }
    
    //Function that performs selection from the mating pool
    public static void tournamentSelect(Individual[] individulas) {
        int length = individulas.length;
        int p1, p2;
        Individual[] array = setupArray(length, NUM_RULES, GENE_SIZE, COND_LEN);
        for (int i = 0; i < individulas.length; i++) {
            p1 = new Random().nextInt(length);
            p2 = new Random().nextInt(length);

            if (individulas[p1].getFitness() >= individulas[p2].getFitness()) {
                array[i] = new Individual(individulas[p1]);
            } else {
                array[i] = new Individual(individulas[p2]);
            }
        }

        matingPool = Arrays.copyOf(array, length);
    }

    //Function to set up the population and mating pools
    public static Individual[] setupArray(int popSize, int numRules, int geneSize, int conditionLength) {
        Individual[] array = new Individual[popSize];
        for (int i = 0; i < array.length; i++) {
            array[i] = new Individual(geneSize, numRules, conditionLength);
            array[i].generateRulebase();
        }
        return array;
    }
    
    //Functions that handle the output file
    public static void createFile(String name)
    {
        file = new File(name);
        try
        {
            if(file.exists())
            {
                file.delete();
                file.createNewFile();
            }else file.createNewFile();
            
            fileWriter = new FileWriter(file);
            fileWriter.append("Generation, ");
            fileWriter.append("Fitness, ");
            fileWriter.append("Average\n");
            
        }catch (IOException e) 
        {
            System.out.println(e);
        }
        
    }
    
    public static void addToFile(int generation, int fitness, int avgFitness)
    {
        try
        {
            fileWriter.append(generation+", ");
            fileWriter.append(fitness+", ");
            fileWriter.append(avgFitness+"\n");
        }catch(IOException e)
        {
            System.out.println(e);
        }
    }
    
    public static void close()
    {
        try
        {
            fileWriter.close();
        }catch(IOException e)
        {
            System.out.println(e);
        }
    }
    
    //Funtion that reads the dataset and populate the arraylist
    public static ArrayList<Data> getDataSet(String fileName) {
        ArrayList<Data> dataset = new ArrayList<>();
        Scanner input = new Scanner(Main.class.getResourceAsStream(fileName));
        input.nextLine();
        String line = null;
        int size = 0;

        while (input.hasNextLine()) {
            String str = null;

            line = input.nextLine();
            line = line.replace(" ", "");

            size = line.length() - 1;
            Data data = new Data(size);

            for (int i = 0; i < line.length() - 1; i++) {
                data.variables[i] = Character.getNumericValue(line.charAt(i));
            }
            data.setOutput(Character.getNumericValue(line.charAt(size)));

            dataset.add(data);
        }

        dataset.forEach((data) -> {
            System.out.println(data.printVariables() + " " + data.getOutput());
        });
        System.out.println("Data loaded");
        COND_LEN = size;
        GENE_SIZE = (COND_LEN + 1) * NUM_RULES;
        return dataset;
    }

}
