package org.machinery.metaheuristics.evolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A class used for configuring an evolving population using genetic algorithms.
 * @param <T> the value type that each "gene" can take.
 */
public final class GenePool<T>
{

    private final IntelligentDesign<T> design;
    private final int populationSize;
    private final float mutationProbability;
    private final double cullThreshold;
    private final boolean doesCull;
    private final int eliteChildren;
    private final Random gen;


    /**
     * Create a GenePool for evolving solutions.
     *
     * @param design              this is a specification for the problem, which we'll use to construct candidate specimen and
     *                            mutate them.
     * @param populationSize      this is how many specimens we should include in each population. Should be an even number.
     * @param mutationProbability this is the probability that an individual value within a specimen is randomly mutated
     * @param cullThreshold       if present, specifies a minimum fitness. Any specimen below that threshold will not be
     *                            selected for mating
     * @param eliteChildren       if present, specifies a number of top ranked children who are copied directly into the next
     *                            generation. They may also be selected for breeding. This is applied before cullThreshold
     *                            and should also be even.
     */
    public GenePool(final IntelligentDesign<T> design, final int populationSize, final float mutationProbability,
                    final Optional<Double> cullThreshold, final Optional<Integer> eliteChildren)
    {
        this.design = Objects.requireNonNull(design);
        if (populationSize % 2 != 0)
        {
            throw new IllegalArgumentException("Population size must be an even number, got " + populationSize);
        }
        this.populationSize = populationSize;
        this.mutationProbability = mutationProbability;

        // We don't want to continually unbox the optional, but there's no sane default value for a fitness score
        // without constraining consumers of this class to using non-negative fitness scores.
        // Instead, we'll unpack the information into a boolean and primitive.
        if (cullThreshold.isPresent())
        {
            this.cullThreshold = cullThreshold.get();
            this.doesCull = true;
        }
        else
        {
            this.cullThreshold = 0;
            this.doesCull = false;
        }

        // Elite children is easy because having 0 is the same as disabling the feature.
        if (eliteChildren.isPresent())
        {
            this.eliteChildren = eliteChildren.get();
            if (this.eliteChildren % 2 != 0)
            {
                throw new IllegalArgumentException("Elite children must be an even number, got " + this.eliteChildren);
            }
        }
        else
        {
            this.eliteChildren = 0;
        }
        gen = new Random();
    }

    private List<List<T>> evolve(final List<List<T>> currentPopulation)
    {
        final List<List<T>> next = new ArrayList<>(design.shape().size());
        if (eliteChildren > 0)
        {
            copyElite(currentPopulation, next);
        }
        List<List<T>> parents;
        if (doesCull)
        {
            // May make sense to do this during the same iteration as elite. For now we'll do a second iteration.
            parents = cull(currentPopulation);
        }
        else
        {
            parents = currentPopulation;
        }
        // It's possible to cull *everything*
        if (parents.isEmpty())
        {
            parents = initialPopulation();
        }
        breed(parents, next);
        return next;
    }

    private void copyElite(final List<List<T>> current, final List<List<T>> next)
    {
        // Rather than sorting, lets do a single pass, and collect them in a priority queue. This can be
        // expanded with heuristics based on the number of the children compared to the size of the population in
        // the future.
        final PriorityQueue<List<T>> elite = new PriorityQueue<>(eliteChildren, new SpecimenComparator<>(design));
        for (final List<T> specimen : current)
        {
            if (elite.size() < eliteChildren)
            {
                elite.add(specimen);
            }
            else
            {
                if (design.fitness(elite.peek()) < design.fitness(specimen))
                {
                    elite.remove();
                    elite.add(specimen);
                }
            }
        }
        for (final List<T> eliteChild : elite)
        {
            next.add(eliteChild);
        }
    }

    private List<List<T>> cull(final List<List<T>> current)
    {
        final List<List<T>> survivors = new ArrayList<>();
        for (final List<T> specimen : current)
        {
            if (design.fitness(specimen) > cullThreshold)
            {
                survivors.add(specimen);
            }
        }
        return survivors;
    }

    private List<List<T>> initialPopulation()
    {
        final List<List<T>> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; ++i)
        {
            population.add(randomSpecimen());
        }
        return population;
    }

    private List<T> randomSpecimen()
    {
        final List<List<T>> shape = design.shape();
        final List<T> specimen = new ArrayList<>(shape.size());
        for (List<T> candidates : shape)
        {
            final T selected = candidates.get(gen.nextInt(candidates.size()));
            specimen.add(selected);
        }
        return specimen;
    }

    private void breed(final List<List<T>> parents, final List<List<T>> next)
    {
        final int parentsSize = parents.size();
        final int remaining = populationSize - next.size();
        for (int i = 0; i < remaining; ++i)
        {
            mate(parents.get(gen.nextInt(parentsSize)), parents.get(gen.nextInt(parentsSize)), next);
        }

    }

    // Mutates population, adding two new children created from mating parent1 and parent2
    private void mate(final List<T> parent1, final List<T> parent2, final List<List<T>> population)
    {
        final List<List<T>> shape = design.shape();
        final int size = shape.size();
        final List<T> child1 = new ArrayList<>(size);
        final List<T> child2 = new ArrayList<>(size);
        final int crossOver = gen.nextInt(size);

        for (int i = 0; i < crossOver; ++i)
        {
            final List<T> candidates = shape.get(i);
            child1.add(maybeMutate(parent1.get(i), candidates));
            child2.add(maybeMutate(parent2.get(i), candidates));
        }
        for (int i = crossOver; i < size; ++i)
        {
            final List<T> candidates = shape.get(i);
            child1.add(maybeMutate(parent2.get(i), candidates));
            child2.add(maybeMutate(parent1.get(i), candidates));
        }
        population.add(child1);
        population.add(child2);
    }

    private T maybeMutate(final T inherited, final List<T> candidates)
    {
        if (gen.nextDouble() < mutationProbability)
        {
            return candidates.get(gen.nextInt(candidates.size()));
        }
        return inherited;
    }

    private final class EvolutionOperator implements UnaryOperator<List<List<T>>>
    {
        @Override
        public List<List<T>> apply(List<List<T>> currentPopulation)
        {
            return evolve(currentPopulation);
        }
    }

    /**
     * Returns an ordered stream which, when iterated, should show a population evolving towards higher
     * fitness scores.
     * @param initialPopulation the first generation
     * @return an ordered stream, starting with a generated initial population, converging on a population
     * with higher fitness scores.
     */
    public Stream<List<List<T>>> stream(final List<List<T>> initialPopulation)
    {
        return Stream.iterate(initialPopulation, new EvolutionOperator());
    }

    /**
     * Generates an initial population according to the configured design and returns an ordered stream
     * which, when iterated, should show a population evolving towards higher fitness scores.
     * @return an ordered stream, starting with a generated initial population, converging on a population
     * with higher fitness scores.
     */
    public Stream<List<List<T>>> stream()
    {
        return stream(initialPopulation());
    }
}
