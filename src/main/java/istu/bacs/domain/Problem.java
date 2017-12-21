package istu.bacs.domain;

import lombok.Data;

import java.util.Comparator;

@Data
public class Problem implements Comparable<Problem> {

	private String problemId;

    private ProblemDetails details;

    private Comparator<Problem> comparator = Comparator.comparing(p -> p.problemId);

    @Override
    public int compareTo(Problem other) {
        return comparator.compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        Problem problem = (Problem) other;
        return problemId.equals(problem.problemId);
    }

    @Override
    public int hashCode() {
        return problemId.hashCode();
    }
}