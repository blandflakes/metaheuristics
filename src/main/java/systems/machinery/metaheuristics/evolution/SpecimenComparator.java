package systems.machinery.metaheuristics.evolution;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class SpecimenComparator<T> implements Comparator<List<T>>
{
    private final Phenotype<T> phenotype;

    public SpecimenComparator(final Phenotype<T> phenotype)
    {
        this.phenotype = Objects.requireNonNull(phenotype);
    }

    @Override
    public int compare(final List<T> o1, final List<T> o2)
    {
        final double difference = phenotype.fitness(o2) - phenotype.fitness(o1);
        if (difference < 0)
        {
            return -1;
        }
        if (difference > 0)
        {
            return 1;
        }
        return 0;
    }
}
