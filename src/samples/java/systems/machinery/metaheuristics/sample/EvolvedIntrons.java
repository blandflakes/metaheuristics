package systems.machinery.metaheuristics.sample;

import systems.machinery.metaheuristics.evolution.GenePool;
import systems.machinery.metaheuristics.evolution.Phenotype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class demonstrates searching for a solution using GenePool. The problem statement comes from a Bioinformatics
 * contest hosted by Stepik in 2017. Input looks like this:
 * TAGCGCGT
 * 3
 * AC
 * CGCG
 * GT
 *
 * The first line is a DNA sequence. The next line is the number of reads (n), and the following n lines
 * are one read each.
 * The goal is essentially to find a substring of the DNA sequence that contains all of the reads. The output in the
 * contest looks like this:
 * ACGT
 * 1
 * -1
 * 3
 *
 * Here, the first line is the proposed solution, and each line after that is the location within the original sequence
 * (1-indexed) of the corresponding read from the input.
 */
public final class EvolvedIntrons
{

    private static final class IntronsPhenotype implements Phenotype<IntronsPhenotype.Expressed>
    {
        // This enum lets us avoid autoboxing with the generic types.
        enum Expressed
        {
            YES,
            NO
        }
        private final int length;
        private final List<List<Expressed>> shape;
        private final char[] sequence;
        private final List<String> reads;

        private IntronsPhenotype(final String sequence, final List<String> reads)
        {
            this.sequence = sequence.toCharArray();
            this.length = sequence.length();
            this.shape = new ArrayList<>(this.length);
            final List<Expressed> possibleValues = new ArrayList<>(2);
            possibleValues.add(Expressed.NO);
            possibleValues.add(Expressed.YES);
            for (int i = 0; i < this.length; ++i)
            {
                this.shape.add(possibleValues);
            }
            this.reads = reads;
        }

        @Override
        public List<List<Expressed>> shape()
        {
            return shape;
        }

        String decode(final List<Expressed> specimen)
        {
            // First, decode the specimen.
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; ++i)
            {
                if (specimen.get(i) == Expressed.YES)
                {
                    builder.append(sequence[i]);
                }
            }
            return builder.toString();
        }

        @Override
        public double fitness(final List<Expressed> specimen)
        {
            final String decoded = decode(specimen);
            int numReads = 0;
            for (final String read : reads)
            {
                if (decoded.contains(read))
                {
                    ++numReads;
                }
            }
            return numReads / (double) reads.size() * 100;
        }
    }

    private static List<IntronsPhenotype.Expressed> findBest(final IntronsPhenotype phenotype, final List<List<IntronsPhenotype.Expressed>> generation)
    {
        double maxScore = -1;
        List<IntronsPhenotype.Expressed> bestSpecimen = null;
        for (final List<IntronsPhenotype.Expressed> specimen : generation)
        {
            final double fitness = phenotype.fitness(specimen);
            if (fitness > maxScore)
            {
                bestSpecimen = specimen;
                maxScore = fitness;
            }
        }
        return bestSpecimen;
    }

    public static void main(final String[] args)
    {
        final String sequence;
        final List<String> reads;
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            sequence = reader.readLine();
            final int numReads = Integer.parseInt(reader.readLine());
            reads = new ArrayList<>(numReads);
            for (int i = 0; i < numReads; ++i)
            {
                reads.add(reader.readLine());
            }
        }
        catch (IOException e)
        {
            System.err.println("Unable to open reader to get input!");
            e.printStackTrace();
            System.exit(1);
            return;
        }
        final IntronsPhenotype phenotype = new IntronsPhenotype(sequence, reads);
        final GenePool<IntronsPhenotype.Expressed> genePool =
                new GenePool<>(phenotype, 10000, 0.25f, Optional.of(40d), Optional.of(2));
        // let's do 10,000 generations
        final Stream<List<List<IntronsPhenotype.Expressed>>> stream = genePool.stream().limit(100000);
        final Iterator<List<List<IntronsPhenotype.Expressed>>> generationIterator = stream.iterator();

        List<List<IntronsPhenotype.Expressed>> lastGeneration;
        do
        {
            lastGeneration = generationIterator.next();
        }
        while (generationIterator.hasNext());
        // So now we have the last generation, let's find the best answer
        // Iterate so that this is a fair comparison.
        final List<IntronsPhenotype.Expressed> bestSpecimen = findBest(phenotype, lastGeneration);
        System.out.println("Best shot: " + bestSpecimen);
        System.out.println("Decoded: " + phenotype.decode(bestSpecimen));
        System.out.println("Score: " + phenotype.fitness(bestSpecimen));
    }
}
