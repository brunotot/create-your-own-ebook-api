package brunotot.createyourownebookapi.domain;

import lombok.Getter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class TraversableTree<T extends TraversableTree> {
    public final List<T> children;

    protected TraversableTree(final List<T> children) {
        this.children = children;
    }

    protected abstract Predicate<T> recursiveEscapePredicate();

    public boolean hasChildren() {
        return this.children.size() > 0;
    }

    public int getRecursiveLength() {
        return this.recursiveLength((T) this, 0);
    }

    public void forEach(final Consumer<T> consumer) {
        this.forEach(consumer, (T) this);
    }

    private int recursiveLength(final T section, final int counter) {
        var counterLocal = counter;
        if (this.recursiveEscapePredicate().test(section)) {
            counterLocal++;
        }
        for (var sectionChild : section.getChildren()) {
            counterLocal = this.recursiveLength((T) sectionChild, counterLocal);
        }
        return counterLocal;
    }

    private void forEach(final Consumer<T> consumer, final T section) {
        if (this.recursiveEscapePredicate().test(section)) {
            consumer.accept(section);
        }
        for (var sectionChild : section.getChildren()) {
            this.forEach(consumer, (T) sectionChild);
        }
    }
}
