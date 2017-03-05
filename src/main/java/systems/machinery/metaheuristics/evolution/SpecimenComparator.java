package systems.machinery.metaheuristics.evolution;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

final class SpecimenComparator<T> implements Comparator<List<T>>
{
    private final IntelligentDesign<T> design;

    public SpecimenComparator(final IntelligentDesign<T> design)
    {
        this.design = Objects.requireNonNull(design);
    }

    @Override
    public int compare(final List<T> o1, final List<T> o2)
    {
        final double difference = design.fitness(o2) - design.fitness(o1);
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
