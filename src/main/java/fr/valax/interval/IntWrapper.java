package fr.valax.interval;

public class IntWrapper {

    private int value;

    public IntWrapper() {}

    public IntWrapper(int v) {
        value = v;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public void add(int v) {
        value += v;
    }

    public void sub(int v) {
        value -= v;
    }

    public void mul(int v) {
        value *= v;
    }

    public void div(int v) {
        value /= v;
    }

    public void set(int v) {
        value = v;
    }

    public int get() {
        return value;
    }
}
