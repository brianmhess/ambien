package hessian.ambien;

public class Pair<S,T> {
    private S first;
    private T second;

    Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    S getFirst() {
        return first;
    }

    T getSecond() {
        return second;
    }

    S getKey() {
        return getFirst();
    }

    T getValue() {
        return getSecond();
    }
}
