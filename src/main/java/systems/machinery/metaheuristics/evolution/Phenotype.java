package systems.machinery.metaheuristics.evolution;

import java.util.List;

/**
 * A phenotype for a species to be evolved in a GenePool.
 * @param <T> the value type that each "gene" can take.
 */
public interface Phenotype<T>
{
    /**
     * The shape is used to specify which values may appear at which positions. It is an array of arrays -
     * the first outer array (shape()[0]) lists all possible values that can occur in slot 0 of the solution.
     * @return the shape of the specimen
     */
    List<List<T>> shape();

    /**
     * Calculates the fitness of the provided specimen
     * @param specimen A candidate to evaluate. All specimens will be of the same length as shape()
     * @return a non-negative score that can be used to rank the specimen. Higher is better.
     */
    double fitness(List<T> specimen);
}
